
(ns app.comp.leaf
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp <> span div input textarea a]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [app.util.keycode :as keycode]
            [app.util :as util]
            [app.util.shortcuts :refer [on-window-keydown]]))

(defn on-input [state coord]
  (fn [e d! m!]
    (d! :ir/update-leaf (:value e))
    (m! (assoc state :text (:value e) :time (util/now!)))))

(def style-first {:color (hsl 40 85 60)})

(def style-space {:background-color (hsl 0 0 100 0.12)})

(def style-number {:color (hsl 0 70 40)})

(def style-highlight {:background-color (hsl 0 0 100 0.2)})

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(def style-big {:border-right (str "16px solid " (hsl 0 0 30))})

(def style-keyword {:color (hsl 240 30 64)})

(def initial-state {:text "", :time 0})

(def style-leaf
  (merge
   ui/input
   {:line-height "24px",
    :height 24,
    :margin "2px 2px",
    :padding "0px 4px",
    :background-color :transparent,
    :min-width 8,
    :color (hsl 200 14 60),
    :font-family "Menlo,Iosevka,monospace",
    :font-size 15,
    :vertical-align :baseline,
    :transition-duration "200ms",
    :transition-property "color",
    :text-align :left,
    :border-width "1px 1px 1px 1px",
    :resize :none,
    :white-space :nowrap}))

(def style-string {:color (hsl 120 60 56)})

(defn on-keydown [state leaf coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          code (:key-code e)
          shift? (.-shiftKey event)
          meta? (or (.-metaKey event) (.-ctrlKey event))
          selected? (not= event.target.selectionStart event.target.selectionEnd)
          text (if (> (:time state) (:time leaf)) (:text state) (:text leaf))
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

(def style-partial {:border-right (str "8px solid " (hsl 0 0 30)), :padding-right 0})

(defcomp
 comp-leaf
 (states leaf focus coord by-other? first? readonly?)
 (let [state (or (:data states) initial-state)
       text (or (if (> (:time state) (:time leaf)) (:text state) (:text leaf)) "")
       focused? (= focus coord)
       has-blank? (or (= text "") (string/includes? text " "))
       best-width (+
                   10
                   (text-width* text (:font-size style-leaf) (:font-family style-leaf)))
       max-width 240]
   (textarea
    {:value text,
     :spellcheck false,
     :class-name (str "cirru-leaf" (if (= focus coord) " cirru-focused" "")),
     :read-only readonly?,
     :style (merge
             {}
             {:width (min best-width max-width)}
             (if first? style-first)
             (if (string/starts-with? text ":") style-keyword)
             (if (string/starts-with? text "|") style-string)
             (if (> best-width max-width) style-partial)
             (if (string/includes? text "\n") style-big)
             (if (re-find (re-pattern "^-?\\d") text) style-number)
             (if has-blank? style-space)
             (if (or focused? by-other?) style-highlight)),
     :on (if readonly?
       {}
       {:click (on-focus coord),
        :keydown (on-keydown state leaf coord),
        :input (on-input state coord)})})))
