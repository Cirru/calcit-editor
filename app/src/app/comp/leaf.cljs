
(ns app.comp.leaf
  (:require-macros [respo.macros :refer [defcomp <> span div input a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [app.util.keycode :as keycode]
            [app.util :as util]))

(def style-leaf
  (merge
   ui/input
   {:line-height "14px",
    :height 20,
    :margin "2px 0px",
    :padding "0 4px",
    :background-color :transparent,
    :min-width 8,
    :color (hsl 200 30 70),
    :opacity 0.8,
    :font-family "Menlo",
    :font-size 14,
    :border-radius "4px",
    :vertical-align :baseline,
    :transition-duration "200ms",
    :transition-property "color",
    :text-align :center}))

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(defn on-keydown [state leaf coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          code (:key-code e)
          shift? (.-shiftKey event)
          meta? (.-metaKey event)
          text (if (> (:time state) (:time leaf)) (:text state) (:text leaf))
          text-length (count text)]
      (cond
        (= code keycode/delete) (if (and (= "" text)) (d! :ir/delete-node nil))
        (and (not shift?) (= code keycode/space))
          (do (d! :ir/leaf-after nil) (.preventDefault event))
        (= code keycode/enter) (d! (if shift? :ir/leaf-before :ir/leaf-after) nil)
        (= code keycode/tab)
          (do (d! (if shift? :ir/unindent-leaf :ir/indent) nil) (.preventDefault event))
        (= code keycode/up)
          (do (if (not (empty? coord)) (d! :writer/go-up nil)) (.preventDefault event))
        (= code keycode/left)
          (if (zero? event.target.selectionStart)
            (do (d! :writer/go-left nil) (.preventDefault event)))
        (= code keycode/right)
          (if (= text-length event.target.selectionEnd)
            (do (println text-length) (d! :writer/go-right nil) (.preventDefault event)))
        (and meta? shift? (= code keycode/v))
          (do (d! :writer/paste nil) (.preventDefault event))
        :else (println "Keydown leaf" code)))))

(defn on-input [state coord]
  (fn [e d! m!]
    (d! :ir/update-leaf (:value e))
    (m! (assoc state :text (:value e) :time (util/now!)))))

(def initial-state {:text "", :time 0})

(def style-highlight {:opacity 1, :color (hsl 0 0 100 0.9)})

(defcomp
 comp-leaf
 (states leaf focus coord by-other? first?)
 (let [state (or (:data states) initial-state)
       text (if (> (:time state) (:time leaf)) (:text state) (:text leaf))
       focused? (= focus coord)
       has-blank? (or (= text "") (string/includes? text " "))]
   (input
    {:value text,
     :spellcheck false,
     :class-name (if (= focus coord) "cirru-focused" nil),
     :placeholder coord,
     :style (merge
             style-leaf
             {:width (+
                      10
                      (text-width* text (:font-size style-leaf) (:font-family style-leaf)))}
             (if first? {:color (hsl 50 100 70)})
             (if has-blank? {:background-color (hsl 0 0 100 0.2)})
             (if (or focused? by-other?) style-highlight)),
     :on {:click (on-focus coord),
          :keydown (on-keydown state leaf coord),
          :input (on-input state coord)}})))
