
(ns app.comp.leaf
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp <> span div input textarea a]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [app.util.keycode :as keycode]
            [app.util :as util]
            [app.util.shortcuts :refer [on-window-keydown]]
            [app.theme :refer [decide-leaf-theme]]))

(def initial-state {:text "", :at 0})

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(defn on-input [state coord]
  (fn [e d! m!]
    (d! :ir/update-leaf (:value e))
    (m! (assoc state :text (:value e) :at (util/now!)))))

(defn on-keydown [state leaf coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          code (:key-code e)
          shift? (.-shiftKey event)
          meta? (or (.-metaKey event) (.-ctrlKey event))
          selected? (not= event.target.selectionStart event.target.selectionEnd)
          text (if (> (:at state) (:at leaf)) (:text state) (:text leaf))
          text-length (count text)]
      (println "selected?" selected?)
      (cond
        (= code keycode/delete) (if (and (= "" text)) (d! :ir/delete-node nil))
        (and (not shift?) (= code keycode/space))
          (do (d! :ir/leaf-after nil) (.preventDefault event))
        (= code keycode/enter)
          (do (d! (if shift? :ir/leaf-before :ir/leaf-after) nil) (.preventDefault event))
        (= code keycode/tab)
          (do (d! (if shift? :ir/unindent-leaf :ir/indent) nil) (.preventDefault event))
        (= code keycode/up)
          (do (if (not (empty? coord)) (d! :writer/go-up nil)) (.preventDefault event))
        (and (not selected?) (= code keycode/left))
          (if (zero? event.target.selectionStart)
            (do (d! :writer/go-left nil) (.preventDefault event)))
        (and meta? (= code keycode/b)) (d! :analyze/peek-def (:text leaf))
        (and (not selected?) (= code keycode/right))
          (if (= text-length event.target.selectionEnd)
            (do (d! :writer/go-right nil) (.preventDefault event)))
        (and meta? shift? (= code keycode/v))
          (do (d! :writer/paste nil) (.preventDefault event))
        (and meta? (= code keycode/d))
          (do
           (.preventDefault event)
           (d! :analyze/goto-def {:text (:text leaf), :forced? shift?}))
        :else (do (comment println "Keydown leaf" code) (on-window-keydown event d!))))))

(defcomp
 comp-leaf
 (states leaf focus coord by-other? first? readonly? theme)
 (let [state (or (:data states) initial-state)
       text (or (if (> (:at state) (:at leaf)) (:text state) (:text leaf)) "")
       focused? (= focus coord)]
   (textarea
    {:value text,
     :spellcheck false,
     :class-name (str "cirru-leaf" (if (= focus coord) " cirru-focused" "")),
     :read-only readonly?,
     :style (decide-leaf-theme text focused? first? by-other? theme),
     :on (if readonly?
       {}
       {:click (on-focus coord),
        :keydown (on-keydown state leaf coord),
        :input (on-input state coord)})})))
