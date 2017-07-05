
(ns app.comp.expr
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.util.keycode :as keycode]))

(def style-expr
  {:border-left (str "1px solid " (hsl 0 0 70)), :min-height 24, :outline :none})

(defn on-keydown [coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          shift? (.-shiftKey event)
          meta? (.-metaKey event)
          ctrl? (.-ctrlKey event)]
      (case (:key-code e)
        keycode/enter (d! :ir/insert-token nil)
        (println "Keydown" coord (:key-code e))))))

(defcomp
 comp-expr
 (expr coord)
 (div {:tab-index 0, :style style-expr, :on {:keydown (on-keydown coord)}}))
