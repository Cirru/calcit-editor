
(ns app.util.shortcuts
  (:require [app.util.keycode :as keycode] [app.util.dom :refer [focus-search!]]))

(defn on-window-keydown [event dispatch!]
  (let [meta? (.-metaKey event), code (.-keyCode event)]
    (cond
      (and meta? (= code keycode/p))
        (do
         (dispatch! :router/change {:name :search})
         (focus-search!)
         (.preventDefault event))
      (and meta? (= code keycode/e)) (do (dispatch! :writer/edit-ns nil))
      (and meta? (= code keycode/j)) (do (dispatch! :writer/move-next nil))
      (and meta? (= code keycode/k)) (do (dispatch! :writer/move-previous nil)))))
