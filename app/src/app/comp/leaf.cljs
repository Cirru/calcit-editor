
(ns app.comp.leaf
  (:require-macros [respo.macros :refer [defcomp <> span div input a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]))

(def style-leaf
  (merge
   ui/input
   {:line-height "14px",
    :height 20,
    :margin "2px 4px",
    :padding "0 4px",
    :background-color (hsl 0 0 100 0.3),
    :min-width 12,
    :color :white}))

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(defcomp
 comp-leaf
 (leaf coord)
 (input
  {:value (:text leaf),
   :placeholder coord,
   :style (merge
           style-leaf
           {:width (let [x (text-width* (:text leaf) 14 "Menlo,monospace")]
              (println "x" x)
              x)}),
   :on {:click (on-focus coord)}}))
