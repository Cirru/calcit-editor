
(ns app.util.shortcuts
  (:require [app.util.keycode :as keycode] [app.util.dom :refer [focus-search!]]))

(defn on-window-keydown [event dispatch!]
  (cond
    (and (.-metaKey event) (= (.-keyCode event) keycode/p))
      (do (dispatch! :router/change {:name :search}) (focus-search!) (.preventDefault event))))
