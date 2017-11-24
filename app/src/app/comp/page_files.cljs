
(ns app.comp.page-files
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp cursor-> list-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.changed-files :refer [comp-changed-files]]
            [app.util.keycode :as keycode]
            [app.comp.file-replacer :refer [comp-file-replacer]]))

(defn on-remove-ns [ns-text] (fn [e d! m!] (d! :ir/remove-ns ns-text)))

(defn on-edit-proc [e d! m!] (d! :writer/edit {:kind :proc}))

(def sytle-container {:padding "0 16px"})

(defn on-input-def [state] (fn [e d! m!] (m! (assoc state :def-text (:value e)))))

(defn on-remove-def [def-text] (fn [e d! m!] (d! :ir/remove-def def-text)))

(def style-input (merge style/input {:width "100%"}))

(defn on-keydown-def [state]
  (fn [e d! m!]
    (let [text (string/trim (:def-text state)), code (:key-code e)]
      (if (and (= code keycode/enter) (not (string/blank? text)))
        (do (d! :ir/add-def text) (m! (assoc state :def-text "")))))))

(def style-def {:padding "0 8px", :position :relative})

(def style-remove
  {:color (hsl 0 0 80),
   :cursor :pointer,
   :vertical-align :middle,
   :position :absolute,
   :top 8,
   :right 8})

(def style-link {:cursor :pointer})

(defn on-edit-ns [e d! m!] (d! :writer/edit {:kind :ns}))

(defn on-edit-def [text] (fn [e d! m!] (d! :writer/edit {:kind :def, :extra text})))

(def style-file {:width 280, :overflow :auto, :padding-bottom 120})

(defn render-file [state selected-ns defs-set]
  (div
   {:style style-file}
   (div
    {}
    (<> span "File" style/title)
    (=< 16 nil)
    (span {:inner-text selected-ns, :style style-link, :on {:click on-edit-ns}})
    (=< 16 nil)
    (span {:inner-text "proc", :style style-link, :on {:click on-edit-proc}})
    (=< 16 nil)
    (span
     {:inner-text "Replacer",
      :style style/button,
      :on {:click (fn [e d! m!] (d! :writer/draft-ns selected-ns))}}))
   (div
    {}
    (input
     {:value (:def-text state),
      :placeholder "a def",
      :style style-input,
      :on {:input (on-input-def state), :keydown (on-keydown-def state)}}))
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
             (div
              {:class-name "hoverable",
               :style style-def,
               :on {:click (on-edit-def def-text)}}
              (<> span def-text nil)
              (=< 16 nil)
              (span
               {:class-name "ion-trash-b is-minor",
                :title "Remove def",
                :style style-remove,
                :on {:click (on-remove-def def-text)}}))]))))))

(def style-ns
  {:cursor :pointer, :vertical-align :middle, :position :relative, :padding "0 8px"})

(def style-list {:width 280, :overflow :auto, :padding-bottom 120})

(defn on-checkout [state ns-text] (fn [e d! m!] (d! :session/select-ns ns-text)))

(defn on-input-ns [state] (fn [e d! m!] (m! (assoc state :ns-text (:value e)))))

(def style-empty {:width 280})

(defn render-empty [] (div {:style style-empty} (<> span "Empty" nil)))

(def initial-state {:ns-text "", :def-text ""})

(def style-inspect {:opacity 1, :background-color (hsl 0 0 100), :color :black})

(defn on-keydown-ns [state]
  (fn [e d! m!]
    (let [text (string/trim (:ns-text state)), code (:key-code e)]
      (if (and (= code keycode/enter) (not (string/blank? text)))
        (cond
          (string/starts-with? text "mv ")
            (let [[_ from to] (string/split text " ")]
              (d! :ir/mv-ns {:from from, :to to})
              (m! (assoc state :ns-text "")))
          (string/starts-with? text "cp ")
            (let [[_ from to] (string/split text " ")]
              (d! :ir/cp-ns {:from from, :to to})
              (m! (assoc state :ns-text "")))
          :else (do (d! :ir/add-ns text) (m! (assoc state :ns-text ""))))))))

(defn render-list [state ns-set selected-ns]
  (div
   {:style style-list}
   (div {:style style/title} (<> span "Namespaces" nil))
   (div
    {}
    (input
     {:value (:ns-text state),
      :placeholder "a namespace",
      :style style-input,
      :on {:input (on-input-ns state), :keydown (on-keydown-ns state)}}))
   (=< nil 8)
   (list->
    :div
    {}
    (->> ns-set
         (filter (fn [ns-text] (string/includes? ns-text (:ns-text state))))
         (sort)
         (map
          (fn [ns-text]
            [ns-text
             (div
              {:class-name (if (= selected-ns ns-text) "hoverable is-selected" "hoverable"),
               :style (merge style-ns),
               :on {:click (on-checkout state ns-text)}}
              (span {:inner-text ns-text})
              (span
               {:class-name "ion-trash-b is-minor",
                :title "Remove ns",
                :style style-remove,
                :on {:click (on-remove-ns ns-text)}}))]))))))

(defcomp
 comp-page-files
 (states selected-ns router-data)
 (let [state (or (:data states) initial-state)]
   (div
    {:style (merge ui/flex ui/row sytle-container)}
    (render-list state (:ns-set router-data) selected-ns)
    (=< 32 nil)
    (if (some? selected-ns)
      (render-file state selected-ns (:defs-set router-data))
      (render-empty))
    (=< 32 nil)
    (cursor-> :files comp-changed-files states (:changed-files router-data))
    (comment comp-inspect selected-ns router-data style-inspect)
    (if (some? (:peeking-file router-data))
      (cursor-> :replacer comp-file-replacer states (:peeking-file router-data))))))
