
(ns app.comp.expr
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.util.keycode :as keycode]
            [app.comp.leaf :refer [comp-leaf]]))

(def style-expr
  {:border-left (str "1px solid " (hsl 0 0 70)),
   :min-height 24,
   :outline :none,
   :padding-left 16,
   :font-family "Menlo,monospce",
   :font-size 14})

(defn on-keydown [e d! m!]
  (let [event (:original-event e)
        shift? (.-shiftKey event)
        meta? (.-metaKey event)
        ctrl? (.-ctrlKey event)
        code (:key-code e)]
    (cond
      (= code keycode/enter) (d! :ir/append-leaf nil)
      :else (println "Keydown" (:key-code e)))))

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(defcomp
 comp-expr
 (states expr focus coord)
 (div
  {:tab-index 0,
   :class-name (if (= focus coord) "cirru-focused" nil),
   :style style-expr,
   :on {:keydown on-keydown, :click (on-focus coord)}}
  (->> (:data expr)
       (sort-by first)
       (map
        (fn [entry]
          (let [[k child] entry]
            [k
             (if (= :leaf (:type child))
               (cursor-> k comp-leaf states child focus (conj coord k))
               (cursor-> k comp-expr states child focus (conj coord k)))]))))))
