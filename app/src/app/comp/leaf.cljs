
(ns app.comp.leaf
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp <> span div input textarea a]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [keycode.core :as keycode]
            [app.client-util :as util]
            [app.util.shortcuts :refer [on-window-keydown]]
            [app.theme :refer [decide-leaf-theme]]
            [verbosely.core :refer [log!]]))

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
      (cond
        (= code keycode/backspace) (if (and (= "" text)) (d! :ir/delete-node nil))
        (and (= code keycode/space) (not shift?))
          (do (d! :ir/leaf-after nil) (.preventDefault event))
        (= code keycode/return)
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
           (if (contains? #{"\"" "|"} (first (:text leaf)))
             (do
              (d! :manual-state/draft-box nil)
              (js/setTimeout
               (fn []
                 (let [el (.querySelector js/document ".el-draft-box")]
                   (if (some? el) (.focus el))))))
             (d! :analyze/goto-def {:text (:text leaf), :forced? shift?})))
        (and meta? (= code keycode/slash))
          (do (.open js/window (str "https://clojuredocs.org/search?q=" (:text leaf))))
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
