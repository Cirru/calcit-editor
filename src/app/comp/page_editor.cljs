
(ns app.comp.page-editor
  (:require [hsl.core :refer [hsl]]
            [clojure.string :as string]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div a pre]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.bookmark :refer [comp-bookmark]]
            [app.comp.expr :refer [comp-expr]]
            [app.theme :refer [base-style-leaf base-style-expr]]
            [app.style :as style]
            [app.util.dom :refer [inject-style]]
            [app.comp.draft-box :refer [comp-draft-box]]
            [app.comp.abstract :refer [comp-abstract]]
            [app.comp.theme-menu :refer [comp-theme-menu]]
            [app.comp.peek-def :refer [comp-peek-def]]
            [app.util :refer [tree->cirru]]
            [app.util.dom :refer [do-copy-logics!]]
            [respo-alerts.core :refer [comp-prompt comp-confirm]]))

(defn on-draft-box [state]
  (fn [e d! m!]
    (m! (update state :draft-box? not))
    (js/setTimeout
     (fn []
       (let [el (.querySelector js/document ".el-draft-box")] (if (some? el) (.focus el)))))))

(defn on-path-gen! [bookmark]
  (fn [e d! m!]
    (case (:kind bookmark)
      :def
        (let [code ["[]" (:ns bookmark) ":refer" ["[]" (:extra bookmark)]]]
          (do-copy-logics! d! (pr-str code) (str "Copied path of " (:extra bookmark))))
      :ns
        (let [the-ns (:ns bookmark)
              code ["[]" the-ns ":as" (last (string/split the-ns "."))]]
          (do-copy-logics! d! (pr-str code) (str "Copied path of " the-ns)))
      (d! :notify/push-message [:warn "No op."]))))

(defn on-rename-def [new-name bookmark d!]
  (when (not (string/blank? new-name))
    (let [[ns-text def-text] (string/split new-name "/")]
      (d!
       :ir/rename
       {:kind (:kind bookmark),
        :ns {:from (:ns bookmark), :to ns-text},
        :extra {:from (:extra bookmark), :to def-text}}))))

(defn on-reset-expr [bookmark]
  (fn [e d! m!]
    (let [kind (:kind bookmark), ns-text (:ns bookmark)]
      (d!
       :ir/reset-at
       (case kind
         :ns {:ns ns-text, :kind :ns}
         :proc {:ns ns-text, :kind :proc}
         :def {:ns ns-text, :kind :def, :extra (:extra bookmark)}
         (do (println "Unknown" bookmark))))
      (d! :states/clear nil))))

(def style-hint {:color (hsl 0 0 100 0.6), :font-family "Josefin Sans"})

(def style-link
  {:font-family "Josefin Sans", :cursor :pointer, :font-size 14, :color (hsl 200 50 80)})

(def style-status (merge ui/row {:justify-content :space-between, :padding "0 8px"}))

(def style-watcher {:color (hsl 0 0 100 0.7), :margin-left 8})

(def style-watchers (merge ui/row {:display :inline-block}))

(defcomp
 comp-status-bar
 (states router-data bookmark theme)
 (let [state (:data states)
       old-name (if (= :def (:kind bookmark))
                  (str (:ns bookmark) "/" (:extra bookmark))
                  (:ns bookmark))]
   (div
    {:style style-status}
    (div
     {}
     (<> span (str "Writers(" (count (:others router-data)) ")") style-hint)
     (list->
      :div
      {:style style-watchers}
      (->> (:others router-data)
           (vals)
           (map (fn [info] [(:session-id info) (<> span (:nickname info) style-watcher)]))))
     (=< 16 nil)
     (<> span (str "Watchers(" (count (:watchers router-data)) ")") style-hint)
     (list->
      :div
      {:style style-watchers}
      (->> (:watchers router-data)
           (map
            (fn [entry]
              (let [[sid member] entry] [sid (<> span (:nickname member) style-watcher)])))))
     (=< 16 nil)
     (if (= :same (:changed router-data))
       (<> (str (:changed router-data)) {:font-family ui/font-fancy, :color (hsl 260 80 70)})
       (cursor->
        :reset
        comp-confirm
        states
        {:trigger (<> "Reset" style-link), :text "Confirm reset changes to this expr?"}
        (on-reset-expr bookmark)))
     (=< 8 nil)
     (cursor->
      :delete
      comp-confirm
      states
      {:trigger (span {:inner-text "Delete", :style style-link}),
       :text (str
              "Confirm deleting current path: "
              (:ns bookmark)
              "/"
              (or (:extra bookmark) (:kind bookmark)))}
      (fn [e d! m!]
        (if (some? bookmark)
          (d! :ir/delete-entry (dissoc bookmark :focus))
          (js/console.warn "No entry to delete"))))
     (=< 8 nil)
     (cursor->
      :rename
      comp-prompt
      states
      {:trigger (span {:inner-text "Rename", :style style-link}),
       :text (str "Renaming: " old-name),
       :initial old-name}
      (fn [result d! m!] (on-rename-def result bookmark d!)))
     (=< 8 nil)
     (cursor->
      :add
      comp-prompt
      states
      {:trigger (span {:inner-text "Add", :style style-link}),
       :text (str "Add function name:"),
       :initial ""}
      (fn [result d! m!]
        (let [text (string/trim result)]
          (when-not (string/blank? text)
            (d! :ir/add-def text)
            (d! :writer/edit {:kind :def, :extra text})))))
     (=< 8 nil)
     (span {:inner-text "Draft-box", :style style-link, :on {:click (on-draft-box state)}})
     (=< 8 nil)
     (cursor->
      :replace
      comp-prompt
      states
      {:trigger (span {:inner-text "Replace", :style style-link}),
       :text "Replace in current branch:",
       :initial "from=>to",
       :validator (fn [x]
         (if (= 2 (count (string/split x "=>"))) nil "Expected {from}=>{to}")),
       :input-style {:font-family ui/font-code}}
      (fn [result d! m!]
        (let [[from to] (string/split result "=>")]
          (d! :ir/expr-replace {:bookmark bookmark, :from from, :to to}))))
     (=< 8 nil)
     (span
      {:inner-text "Exporting", :style style-link, :on {:click (on-path-gen! bookmark)}}))
    (div {:style ui/row} (cursor-> :theme comp-theme-menu states theme)))))

(def initial-state {:draft-box? false})

(def style-area {:overflow :auto, :padding-bottom 240, :padding-top 80, :flex 1})

(def style-container {:position :relative})

(def style-editor (merge ui/flex ui/column))

(def style-missing
  {:font-family "Josefin Sans", :color (hsl 10 60 50), :font-size 20, :font-weight 100})

(def style-nothing
  {:color (hsl 0 0 100 0.4), :padding "0 16px", :font-family "Josefin Sans"})

(def style-stack {:width 200, :overflow :auto, :padding-bottom 120})

(def ui-missing (div {:style style-missing} (<> span "Expression is missing!" nil)))

(defcomp
 comp-page-editor
 (states stack router-data pointer theme)
 (let [state (or (:data states) initial-state)
       bookmark (get stack pointer)
       expr (:expr router-data)
       focus (:focus router-data)
       readonly? false
       close-draft-box! (fn [mutate!] (mutate! %cursor (assoc state :draft-box? false)))
       close-abstract! (fn [mutate!] (mutate! %cursor (assoc state :abstract? false)))]
   (div
    {:style (merge ui/row ui/flex style-container)}
    (if (empty? stack)
      (div {:style style-stack} (<> "Empty" style-nothing))
      (list->
       :div
       {:style style-stack}
       (->> stack
            (map-indexed
             (fn [idx bookmark] [idx (comp-bookmark bookmark idx (= idx pointer))])))))
    (=< 8 nil)
    (if (empty? stack)
      (div {} (<> "Nothing to edit" style-nothing))
      (div
       {:style style-editor}
       (let [others (->> (:others router-data) (vals) (map :focus) (into #{}))]
         (div
          {:style style-area}
          (inject-style ".cirru-expr" (base-style-expr theme))
          (inject-style ".cirru-leaf" (base-style-leaf theme))
          (if (some? expr)
            (cursor->
             (:id expr)
             comp-expr
             states
             expr
             focus
             []
             others
             false
             false
             readonly?
             theme
             0)
            ui-missing)))
       (let [peek-def (:peek-def router-data)]
         (if (some? peek-def) (comp-peek-def peek-def)))
       (cursor-> :status comp-status-bar states router-data bookmark theme)
       (if (:draft-box? state)
         (cursor-> :draft-box comp-draft-box states expr focus close-draft-box!))
       (if (:abstract? state) (cursor-> :abstract comp-abstract states close-abstract!))
       (comment comp-inspect "Expr" router-data style/inspector))))))
