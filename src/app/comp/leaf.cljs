
(ns app.comp.leaf
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> span div input textarea a]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [keycode.core :as keycode]
            [app.util :as util]
            [app.util.shortcuts :refer [on-window-keydown on-paste!]]
            [app.theme :refer [decide-leaf-theme]]
            [app.util :refer [tree->cirru]]
            [app.util.dom :refer [do-copy-logics!]]))

(def initial-state {:text "", :at 0})

(defn on-focus [leaf coord picker-mode?]
  (fn [e d!]
    (if picker-mode?
      (do (.preventDefault (:event e)) (d! :writer/pick-node (tree->cirru leaf)))
      (d! :writer/focus coord))))

(defn on-input [state coord cursor]
  (fn [e d!]
    (let [now (util/now!)]
      (d! :ir/update-leaf {:text (:value e), :at now})
      (d! cursor (assoc state :text (:value e) :at now)))))

(defn on-keydown [state leaf coord picker-mode?]
  (fn [e d!]
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
        (and meta?
             (= code keycode/c)
             (= (.-selectionStart (.-target event)) (.-selectionEnd (.-target event))))
          (do-copy-logics! d! (tree->cirru leaf) "Copied!")
        (and meta? shift? (= code keycode/v)) (do (on-paste! d!) (.preventDefault event))
        (and meta? (= code keycode/d))
          (do
           (.preventDefault event)
           (if (->> ["\"" "|" "#\""] (some (fn [x] (string/starts-with? (:text leaf) x))))
             (do
              (d! :manual-state/draft-box nil)
              (js/setTimeout
               (fn []
                 (let [el (.querySelector js/document ".el-draft-box")]
                   (if (some? el) (.focus el))))))
             (d! :analyze/goto-def {:text (:text leaf), :forced? shift?})))
        (and meta? (= code keycode/slash) (not shift?))
          (do
           (.open
            js/window
            (str "https://clojuredocs.org/search?q=" (last (string/split (:text leaf) "/")))))
        (and picker-mode? (= code keycode/escape)) (d! :writer/picker-mode nil)
        :else
          (do
           (comment println "Keydown leaf" code)
           (on-window-keydown event d! {:name :editor}))))))

(defcomp
 comp-leaf
 (states leaf focus coord by-other? first? readonly? picker-mode? theme)
 (let [cursor (:cursor states)
       state (or (:data states) initial-state)
       text (or (if (> (:at state) (:at leaf)) (:text state) (:text leaf)) "")
       focused? (= focus coord)]
   (textarea
    {:value text,
     :spellcheck false,
     :class-name (str "cirru-leaf" (if (= focus coord) " cirru-focused" "")),
     :read-only readonly?,
     :style (decide-leaf-theme text focused? first? by-other? theme),
     :on (if readonly?
       {:click (on-focus leaf coord picker-mode?)}
       {:click (on-focus leaf coord picker-mode?),
        :keydown (on-keydown state leaf coord picker-mode?),
        :input (on-input state coord cursor)})})))
