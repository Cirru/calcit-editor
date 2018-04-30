
(ns app.comp.page-editor
  (:require [hsl.core :refer [hsl]]
            [clojure.string :as string]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp list-> cursor-> <> span div a pre]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.bookmark :refer [comp-bookmark]]
            [app.comp.beginner-mode :refer [comp-beginner-mode on-toggle]]
            [app.comp.expr :refer [comp-expr]]
            [app.theme :refer [base-style-leaf base-style-expr]]
            [app.style :as style]
            [app.util.dom :refer [inject-style]]
            [app.comp.rename :refer [comp-rename]]
            [app.comp.draft-box :refer [comp-draft-box]]
            [app.comp.abstract :refer [comp-abstract]]
            [app.comp.theme-menu :refer [comp-theme-menu]]
            [app.comp.peek-def :refer [comp-peek-def]]))

(def initial-state {:beginner? false, :renaming? false, :draft-box? false})

(defn on-delete [bookmark] (fn [e d! m!] (d! :ir/delete-entry (dissoc bookmark :focus))))

(defn on-draft-box [state]
  (fn [e d! m!]
    (m! (update state :draft-box? not))
    (js/setTimeout
     (fn []
       (let [el (.querySelector js/document ".el-draft-box")] (if (some? el) (.focus el)))))))

(defn on-rename [state]
  (fn [e d! m!]
    (m! (update state :renaming? not))
    (js/setTimeout (fn [] (let [el (.querySelector js/document ".el-rename")] (.focus el))))))

(def style-hint {:color (hsl 0 0 100 0.6), :font-family "Josefin Sans"})

(def style-link
  {:font-family "Josefin Sans", :cursor :pointer, :font-size 14, :color (hsl 200 50 80)})

(def style-status (merge ui/row {:justify-content :space-between, :padding "0 8px"}))

(def style-watcher {:color (hsl 0 0 100 0.7), :margin-left 8})

(def style-watchers (merge ui/row {:display :inline-block}))

(defn render-status [router-data states %cursor bookmark theme]
  (let [state (:data states)]
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
      (span {:inner-text "Delete", :style style-link, :on {:click (on-delete bookmark)}})
      (=< 8 nil)
      (span {:inner-text "Rename", :style style-link, :on {:click (on-rename state)}})
      (=< 8 nil)
      (span {:inner-text "Draft-box", :style style-link, :on {:click (on-draft-box state)}}))
     (div
      {:style ui/row}
      (cursor-> :theme comp-theme-menu states theme)
      (=< 16 nil)
      (comp-beginner-mode state (on-toggle state %cursor))))))

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
       old-name (if (= :def (:kind bookmark))
                  (str (:ns bookmark) "/" (:extra bookmark))
                  (:ns bookmark))
       close-rename! (fn [mutate!] (mutate! %cursor (assoc state :renaming? false)))
       close-draft-box! (fn [mutate!] (mutate! %cursor (assoc state :draft-box? false)))
       close-abstract! (fn [mutate!] (mutate! %cursor (assoc state :abstract? false)))]
   (div
    {:style (merge ui/row ui/flex style-container)}
    (if (empty? stack)
      (div {:style style-stack} (<> span "Nothing selected" style-nothing))
      (list->
       :div
       {:style style-stack}
       (->> stack
            (map-indexed
             (fn [idx bookmark] [idx (comp-bookmark bookmark idx (= idx pointer))])))))
    (=< 8 nil)
    (div
     {:style style-editor}
     (let [others (->> (:others router-data) (vals) (map :focus) (into #{}))
           beginner? (:beginner? state)]
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
           beginner?
           readonly?
           theme
           0)
          (if (not (empty? stack)) ui-missing))))
     (let [peek-def (:peek-def router-data)] (if (some? peek-def) (comp-peek-def peek-def)))
     (render-status router-data states %cursor bookmark theme)
     (if (:renaming? state)
       (cursor-> :rename comp-rename states old-name close-rename! bookmark))
     (if (:draft-box? state)
       (cursor-> :draft-box comp-draft-box states expr focus close-draft-box!))
     (if (:abstract? state) (cursor-> :abstract comp-abstract states close-abstract!))
     (comment comp-inspect "Expr" router-data style/inspector)))))
