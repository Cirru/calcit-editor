
(ns app.util.shortcuts
  (:require [keycode.core :as keycode] [app.util.dom :refer [focus-search!]]))

(defn on-window-keydown [event dispatch!]
  (let [meta? (or (.-metaKey event) (.-ctrlKey event))
        shift? (.-shiftKey event)
        code (.-keyCode event)]
    (cond
      (and meta? (or (= code keycode/p) (= code keycode/o)))
        (do
         (dispatch! :router/change {:name :search})
         (focus-search!)
         (.preventDefault event))
      (and meta? (= code keycode/e))
        (if shift? (do (dispatch! :effect/eval-tree)) (dispatch! :writer/edit-ns nil))
      (and meta? (= code keycode/j))
        (do (.preventDefault event) (dispatch! :writer/move-next nil))
      (and meta? (= code keycode/i)) (do (dispatch! :writer/move-previous nil))
      (and meta? (= code keycode/k)) (do (dispatch! :writer/finish nil))
      (and meta? (= code keycode/s))
        (do (.preventDefault event) (dispatch! :effect/save-files nil))
      (and meta? shift? (= code keycode/f)) (dispatch! :router/change {:name :files}))))
