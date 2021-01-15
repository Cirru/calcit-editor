
(ns app.comp.page-files
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> >> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.changed-files :refer [comp-changed-files]]
            [keycode.core :as keycode]
            [app.comp.file-replacer :refer [comp-file-replacer]]
            [app.util.shortcuts :refer [on-window-keydown]]
            [respo-alerts.core :refer [use-prompt use-confirm comp-select]]
            [feather.core :refer [comp-icon comp-i]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(def extension-options
  [{:value :cljs, :display "cljs"}
   {:value :cljc, :display "cljc"}
   {:value :clj, :display "clj"}])

(def style-def {:padding "0 8px", :position :relative, :color (hsl 0 0 74)})

(def style-file {:width 280, :overflow :auto, :padding-bottom 120})

(def style-input (merge style/input {:width "100%"}))

(def style-link {:cursor :pointer})

(def style-remove
  {:color (hsl 0 0 80 0.5),
   :font-size 12,
   :cursor :pointer,
   :vertical-align :middle,
   :position :absolute,
   :top 8,
   :right 8})

(defcomp
 comp-file
 (states selected-ns defs-set highlights configs)
 (let [cursor (:cursor states)
       state (or (:data states) {:def-text ""})
       duplicate-plugin (use-prompt
                         (>> states :duplicate)
                         {:initial selected-ns, :text "a namespace:"})
       add-plugin (use-prompt (>> states :add) {:text "New definition:"})]
   (div
    {:style style-file}
    (div
     {}
     (<> "File" style/title)
     (=< 8 nil)
     (comp-select
      (>> states :extension)
      (or (:extension configs) :cljs)
      extension-options
      {:text "Select extension",
       :style-trigger {:font-family ui/font-normal, :font-size 12}}
      (fn [result d!] (d! :ir/file-config {:extension result})))
     (=< 16 nil)
     (span
      {:inner-text "Draft",
       :style style/button,
       :on-click (fn [e d!] (d! :writer/draft-ns selected-ns))})
     (span
      {:inner-text "Clone",
       :style style/button,
       :on-click (fn [e d!]
         ((:show duplicate-plugin)
          d!
          (fn [result]
            (if (string/includes? result ".")
              (d! :ir/clone-ns result)
              (d! :notify/push-message [:warn (str "Not a good name: " result)])))))}))
    (div
     {}
     (span
      {:inner-text selected-ns,
       :style style-link,
       :on-click (fn [e d!] (d! :writer/edit {:kind :ns}))})
     (=< 16 nil)
     (span
      {:inner-text "proc",
       :style style-link,
       :on-click (fn [e d!] (d! :writer/edit {:kind :proc}))})
     (=< 16 nil)
     (comp-icon
      :plus
      {:font-size 14, :color (hsl 0 0 70), :cursor :pointer}
      (fn [e d!]
        ((:show add-plugin)
         d!
         (fn [result]
           (let [text (string/trim result)]
             (when-not (string/blank? text) (d! :ir/add-def [selected-ns text]))))))))
    (comment
     div
     {}
     (input
      {:value (:def-text state),
       :placeholder "filter...",
       :style style-input,
       :on-input (fn [e d!] (d! cursor (assoc state :def-text (:value e))))}))
    (=< nil 8)
    (list->
     :div
     {}
     (->> defs-set
          (filter (fn [def-text] (string/includes? def-text (:def-text state))))
          (sort)
          (map
           (fn [def-text]
             [def-text
              (let [confirm-remove-plugin (use-confirm
                                           (>> states (str :rm def-text))
                                           {:text (<< "Sure to remove def: ~{def-text} ?")})]
                (div
                 {:class-name "hoverable",
                  :style (merge
                          style-def
                          (if (contains?
                               highlights
                               {:ns selected-ns, :extra def-text, :kind :def})
                            {:color :white})),
                  :on-click (fn [e d!] (d! :writer/edit {:kind :def, :extra def-text}))}
                 (<> def-text nil)
                 (=< 16 nil)
                 (span
                  {:class-name "is-minor",
                   :style style-remove,
                   :on-click (fn [e d!]
                     ((:show confirm-remove-plugin) d! (fn [] (d! :ir/remove-def def-text))))}
                  (comp-i :x 12 (hsl 0 0 80 0.5)))
                 (:ui confirm-remove-plugin)))]))))
    (:ui duplicate-plugin)
    (:ui add-plugin))))

(def style-ns
  {:cursor :pointer,
   :vertical-align :middle,
   :position :relative,
   :padding "0 8px",
   :color (hsl 0 0 74)})

(defcomp
 comp-ns-entry
 (states ns-text selected? ns-highlights)
 (let [plugin-rm-ns (use-confirm
                     (>> states :rm-ns)
                     {:text (<< "Sure to remove namespace: ~{ns-text} ?")})
       has-highlight? (contains? ns-highlights ns-text)]
   (div
    {:class-name (if selected? "hoverable is-selected" "hoverable"),
     :style (merge style-ns (if has-highlight? {:color :white})),
     :on-click (fn [e d!] (d! :session/select-ns ns-text))}
    (let [pieces (string/split ns-text ".")]
      (span
       {}
       (<>
        (str (string/join "." (butlast pieces)) ".")
        {:color (if has-highlight? (hsl 0 0 76) (hsl 0 0 50))})
       (<> (last pieces))))
    (span
     {:class-name "is-minor",
      :style style-remove,
      :on-click (fn [e d!] ((:show plugin-rm-ns) d! (fn [] (d! :ir/remove-ns ns-text))))}
     (comp-i :x 12 (hsl 0 0 80 0.6)))
    (:ui plugin-rm-ns))))

(def style-list {:width 280, :overflow :auto, :padding-bottom 120})

(defcomp
 comp-namespace-list
 (states ns-set selected-ns ns-highlights)
 (let [cursor (:cursor states)
       state (or (:data states) {:ns-text ""})
       plugin-add-ns (use-prompt (>> states :add-ns) {:title "New namespace:"})]
   (div
    {:style style-list}
    (div
     {:style style/title}
     (<> "Namespaces")
     (=< 8 nil)
     (comp-icon
      :plus
      {:color (hsl 0 0 70), :font-size 14, :cursor :pointer}
      (fn [e d!]
        ((:show plugin-add-ns)
         d!
         (fn [result]
           (let [text (string/trim result)]
             (when-not (string/blank? text) (d! :ir/add-ns text))))))))
    (comment
     div
     {}
     (input
      {:value (:ns-text state),
       :placeholder "filter...",
       :style style-input,
       :on-input (fn [e d!] (d! cursor (assoc state :ns-text (:value e))))}))
    (=< nil 8)
    (list->
     :div
     {}
     (->> ns-set
          (filter
           (fn [ns-text]
             (string/includes?
              (string/join "." (rest (string/split ns-text ".")))
              (:ns-text state))))
          (sort)
          (map
           (fn [ns-text]
             [ns-text
              (comp-ns-entry
               (>> states ns-text)
               ns-text
               (= selected-ns ns-text)
               ns-highlights)]))))
    (:ui plugin-add-ns))))

(defn render-empty []
  (div
   {:style {:width 280, :font-family ui/font-fancy, :color (hsl 0 0 100 0.5)}}
   (<> "Empty" nil)))

(def style-inspect {:opacity 1, :background-color (hsl 0 0 100), :color :black})

(def sytle-container {:padding "0 16px"})

(defcomp
 comp-page-files
 (states selected-ns router-data)
 (let [highlights (set (map last (:highlights router-data)))
       ns-highlights (set (map :ns highlights))]
   (div
    {:style (merge ui/flex ui/row sytle-container)}
    (comp-namespace-list (>> states :ns) (:ns-set router-data) selected-ns ns-highlights)
    (=< 32 nil)
    (if (some? selected-ns)
      (comp-file
       (>> states selected-ns)
       selected-ns
       (:defs-set router-data)
       highlights
       (:file-configs router-data))
      (render-empty))
    (=< 32 nil)
    (comp-changed-files (>> states :files) (:changed-files router-data))
    (comment comp-inspect selected-ns router-data style-inspect)
    (if (some? (:peeking-file router-data))
      (comp-file-replacer (>> states :replacer) (:peeking-file router-data))))))
