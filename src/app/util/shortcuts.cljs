
(ns app.util.shortcuts
  (:require [keycode.core :as keycode]
            [app.util.dom :refer [focus-search!]]
            [cljs.reader :refer [read-string]]
            [app.util.list :refer [cirru-form?]]))

(defn on-paste! [d!]
  (-> js/navigator
      .-clipboard
      (.readText)
      (.then
       (fn [text]
         (println "read from text...")
         (let [cirru-code (read-string text)]
           (if (cirru-form? cirru-code)
             (d! :writer/paste cirru-code)
             (d! :notify/push-message [:error "Not valid code"])))))
      (.catch
       (fn [error]
         (.error js/console "Not able to read from paste:" error)
         (d! :notify/push-message [:error "Failed to paste!"])))))

(defn on-window-keydown [event dispatch! router]
  (if (some? router)
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
        (and meta? (not shift?) (= code keycode/j))
          (do (.preventDefault event) (dispatch! :writer/move-next nil))
        (and meta? (not shift?) (= code keycode/i))
          (do (dispatch! :writer/move-previous nil))
        (and meta? (= code keycode/k)) (do (dispatch! :writer/finish nil))
        (and meta? (= code keycode/s))
          (do (.preventDefault event) (dispatch! :effect/save-files nil))
        (and meta? shift? (= code keycode/f)) (dispatch! :router/change {:name :files})
        (and meta? (not shift?) (= code keycode/period)) (dispatch! :writer/picker-mode)))))
