
(ns app.comp.expr
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> >> <> span div a]]
            [respo.comp.space :refer [=<]]
            [keycode.core :as keycode]
            [app.comp.leaf :refer [comp-leaf]]
            [app.client-util :refer [coord-contains? leaf? expr? expr-many-items?]]
            [app.util.shortcuts :refer [on-window-keydown on-paste!]]
            [app.theme :refer [decide-expr-theme]]
            [app.util :refer [tree->cirru]]
            [app.util.dom :refer [do-copy-logics!]]))

(defn on-keydown [coord expr picker-mode?]
  (fn [e d!]
    (let [event (:original-event e)
          shift? (.-shiftKey event)
          meta? (or (.-metaKey event) (.-ctrlKey event))
          code (:key-code e)]
      (cond
        (and meta? (= code keycode/return))
          (d! (if shift? :ir/append-leaf :ir/prepend-leaf) nil)
        (and meta? (= code keycode/return)) (d! :ir/prepend-leaf nil)
        (= code keycode/return)
          (if (empty? coord)
            (d! :ir/prepend-leaf nil)
            (d! (if shift? :ir/expr-before :ir/expr-after) nil))
        (= code keycode/backspace) (d! :ir/delete-node nil)
        (= code keycode/space)
          (do (d! (if shift? :ir/leaf-before :ir/leaf-after) nil) (.preventDefault event))
        (= code keycode/tab)
          (do (d! (if shift? :ir/unindent :ir/indent) nil) (.preventDefault event))
        (= code keycode/up)
          (do (if (not (empty? coord)) (d! :writer/go-up nil)) (.preventDefault event))
        (= code keycode/down)
          (do (d! :writer/go-down {:tail? shift?}) (.preventDefault event))
        (= code keycode/left) (do (d! :writer/go-left nil) (.preventDefault event))
        (= code keycode/right) (do (d! :writer/go-right nil) (.preventDefault event))
        (and meta? (= code keycode/c))
          (do-copy-logics! d! (pr-str (tree->cirru expr)) "Copied!")
        (and meta? (= code keycode/x))
          (do
           (do-copy-logics! d! (pr-str (tree->cirru expr)) "Copied!")
           (d! :ir/delete-node nil))
        (and meta? (= code keycode/v)) (on-paste! d!)
        (and meta? (= code keycode/b)) (d! :ir/duplicate nil)
        (and meta? (= code keycode/d))
          (do
           (if shift?
             (let [tree (tree->cirru expr)]
               (do
                (if (and (>= (count tree) 1) (string? (first tree)))
                  (d!
                   :analyze/goto-def
                   {:text (first tree), :forced? true, :args (subvec tree 1)})
                  (d! :notify/push-message [:warn "Can not create a function!"]))))
             (do
              (d! :manual-state/abstract nil)
              (js/setTimeout
               (fn []
                 (let [el (.querySelector js/document ".el-abstract")]
                   (if (some? el) (.focus el)))))))
           (.preventDefault event))
        (and meta? (= code keycode/slash) (not shift?)) (d! :ir/toggle-comment nil)
        (and picker-mode? (= code keycode/escape)) (d! :writer/picker-mode nil)
        :else
          (do
           (comment println "Keydown" (:key-code e))
           (on-window-keydown event d! {:name :editor}))))))

(defcomp
 comp-expr
 (states expr focus coord others tail? layout-mode readonly? picker-mode? theme depth)
 (let [focused? (= focus coord)
       first-id (apply min (keys (:data expr)))
       last-id (apply max (keys (:data expr)))
       sorted-children (->> (:data expr) (sort-by first))]
   (list->
    :div
    {:tab-index 0,
     :class-name (str "cirru-expr" (if focused? " cirru-focused" "")),
     :style (decide-expr-theme
             expr
             (contains? others coord)
             focused?
             tail?
             layout-mode
             (count coord)
             depth
             theme),
     :on (if readonly?
       {:click (fn [e d!]
          (if picker-mode?
            (do (.preventDefault (:event e)) (d! :writer/pick-node (tree->cirru expr)))))}
       {:keydown (on-keydown coord expr picker-mode?),
        :click (fn [e d!]
          (if picker-mode?
            (do (.preventDefault (:event e)) (d! :writer/pick-node (tree->cirru expr)))
            (d! :writer/focus coord)))})}
    (loop [result [], children sorted-children, prev-mode :inline]
      (if (empty? children)
        result
        (let [[k child] (first children)
              child-coord (conj coord k)
              partial-others (->> others
                                  (filter (fn [x] (coord-contains? x child-coord)))
                                  (into #{}))
              cursor-key k
              mode (if (leaf? child)
                     :inline
                     (if (expr-many-items? child 6)
                       :block
                       (case prev-mode
                         :inline :inline-block
                         :inline-block (if (expr-many-items? child 2) :block :inline-block)
                         :block)))]
          (if (nil? cursor-key) (.warn js/console "[Editor] missing cursor key" k child))
          (recur
           (conj
            result
            [k
             (if (= :leaf (:type child))
               (comp-leaf
                (>> states cursor-key)
                child
                focus
                child-coord
                (contains? partial-others child-coord)
                (= first-id k)
                readonly?
                picker-mode?
                theme)
               (comp-expr
                (>> states cursor-key)
                child
                focus
                child-coord
                partial-others
                (= last-id k)
                mode
                readonly?
                picker-mode?
                theme
                (inc depth)))])
           (rest children)
           mode)))))))
