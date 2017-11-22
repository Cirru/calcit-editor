
(ns app.comp.expr
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp list-> cursor-> <> span div a]]
            [respo.comp.space :refer [=<]]
            [app.util.keycode :as keycode]
            [app.comp.leaf :refer [comp-leaf]]
            [app.util :refer [coord-contains? simple? leaf? expr?]]
            [app.util.shortcuts :refer [on-window-keydown]]
            [app.theme.star-trail :refer [decide-expr-style]]))

(defn on-keydown [coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          shift? (.-shiftKey event)
          meta? (or (.-metaKey event) (.-ctrlKey event))
          code (:key-code e)]
      (cond
        (and meta? (= code keycode/enter)) (d! :ir/prepend-leaf nil)
        (= code keycode/enter)
          (if (empty? coord)
            (d! :ir/prepend-leaf nil)
            (d! (if shift? :ir/expr-before :ir/expr-after) nil))
        (= code keycode/delete) (d! :ir/delete-node nil)
        (= code keycode/space)
          (do (d! (if shift? :ir/leaf-before :ir/leaf-after) nil) (.preventDefault event))
        (= code keycode/tab)
          (do (d! (if shift? :ir/unindent :ir/indent) nil) (.preventDefault event))
        (= code keycode/up)
          (do (if (not (empty? coord)) (d! :writer/go-up nil)) (.preventDefault event))
        (= code keycode/down) (do (d! :writer/go-down nil) (.preventDefault event))
        (= code keycode/left) (do (d! :writer/go-left nil) (.preventDefault event))
        (= code keycode/right) (do (d! :writer/go-right nil) (.preventDefault event))
        (and meta? (= code keycode/c)) (d! :writer/copy nil)
        (and meta? (= code keycode/x)) (d! :writer/cut nil)
        (and meta? (= code keycode/v)) (d! :writer/paste nil)
        (and meta? (= code keycode/b)) (d! :ir/duplicate nil)
        (and meta? (= code keycode/d))
          (do
           (d! :manual-state/abstract nil)
           (.preventDefault event)
           (js/setTimeout
            (fn []
              (let [el (.querySelector js/document ".el-abstract")]
                (if (some? el) (.focus el))))))
        :else (do (comment println "Keydown" (:key-code e)) (on-window-keydown event d!))))))

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(defcomp
 comp-expr
 (states expr focus coord others tail? after-expr? beginner? readonly?)
 (let [focused? (= focus coord)
       first-id (apply min (keys (:data expr)))
       last-id (apply max (keys (:data expr)))
       sorted-children (->> (:data expr) (sort-by first))
       default-info {:after-expr? false}]
   (list->
    :div
    {:tab-index 0,
     :class-name (str "cirru-expr" (if focused? " cirru-focused" "")),
     :style (decide-expr-style
             expr
             (contains? others coord)
             focused?
             tail?
             after-expr?
             beginner?
             (count coord)),
     :on (if readonly? {} {:keydown (on-keydown coord), :click (on-focus coord)})}
    (loop [result [], children sorted-children, info default-info]
      (if (empty? children)
        result
        (let [[k child] (first children)
              child-coord (conj coord k)
              partial-others (->> others
                                  (filter (fn [x] (coord-contains? x child-coord)))
                                  (into #{}))
              cursor-key (:id child)]
          (if (nil? cursor-key) (.warn js/console "[Editor] missing :id" (clj->js child)))
          (recur
           (conj
            result
            [k
             (if (= :leaf (:type child))
               (cursor->
                cursor-key
                comp-leaf
                states
                child
                focus
                child-coord
                (contains? partial-others child-coord)
                (= first-id k)
                readonly?)
               (cursor->
                cursor-key
                comp-expr
                states
                child
                focus
                child-coord
                partial-others
                (= last-id k)
                (:after-expr? info)
                beginner?
                readonly?))])
           (rest children)
           (assoc info :after-expr? (expr? child)))))))))
