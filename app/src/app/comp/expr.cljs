
(ns app.comp.expr
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.util.keycode :as keycode]
            [app.comp.leaf :refer [comp-leaf]]
            [app.util :refer [coord-contains? simple?]]))

(def style-expr
  {:border-width "0 0 0 1px",
   :border-style :solid,
   :border-color (hsl 0 0 100 0.3),
   :min-height 24,
   :outline :none,
   :padding-left 4,
   :margin-left 16,
   :font-family "Menlo,monospce",
   :font-size 14,
   :margin 2})

(defn on-keydown [coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          shift? (.-shiftKey event)
          meta? (.-metaKey event)
          ctrl? (.-ctrlKey event)
          code (:key-code e)]
      (cond
        (and meta? (= code keycode/enter)) (d! :ir/append-leaf nil)
        (= code keycode/enter) (d! (if shift? :ir/expr-before :ir/expr-after) nil)
        (= code keycode/delete) (d! :ir/delete-node nil)
        (= code keycode/space) (d! (if shift? :ir/leaf-before :ir/leaf-after) nil)
        (= code keycode/tab)
          (do (d! (if shift? :ir/unindent :ir/indent) nil) (.preventDefault event))
        (= code keycode/up)
          (do (if (not (empty? coord)) (d! :writer/go-up nil)) (.preventDefault event))
        (= code keycode/down) (do (d! :writer/go-down nil) (.preventDefault event))
        (= code keycode/left) (do (d! :writer/go-left nil) (.preventDefault event))
        (= code keycode/right) (do (d! :writer/go-right nil) (.preventDefault event))
        :else (println "Keydown" (:key-code e))))))

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(def style-simple
  {:display :inline-block, :border-width "0 0 1px 0", :min-width 32, :padding "0 8px"})

(defcomp
 comp-expr
 (states expr focus coord others tail?)
 (let [focused? (= focus coord)
       first-id (apply min (keys (:data expr)))
       last-id (apply max (keys (:data expr)))]
   (div
    {:tab-index 0,
     :class-name (if focused? "cirru-focused" nil),
     :style (merge
             style-expr
             (if (contains? others coord) {:border-color (hsl 0 0 100 0.6)})
             (if focused? {:border-color (hsl 0 0 100 0.9)})
             (if (and (simple? expr) (not tail?)) style-simple)),
     :on {:keydown (on-keydown coord), :click (on-focus coord)}}
    (->> (:data expr)
         (sort-by first)
         (map
          (fn [entry]
            (let [[k child] entry
                  child-coord (conj coord k)
                  partial-others (->> others
                                      (filter (fn [x] (coord-contains? x child-coord)))
                                      (into #{}))]
              [k
               (if (= :leaf (:type child))
                 (cursor->
                  k
                  comp-leaf
                  states
                  child
                  focus
                  child-coord
                  (contains? partial-others child-coord)
                  (= first-id k))
                 (cursor->
                  k
                  comp-expr
                  states
                  child
                  focus
                  child-coord
                  partial-others
                  (= last-id k)))])))))))
