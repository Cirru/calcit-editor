
(ns app.comp.leaf
  (:require-macros [respo.macros :refer [defcomp <> span div input a]])
  (:require [hsl.core :refer [hsl]]
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
    :margin "2px 4px",
    :padding "0 4px",
    :background-color (hsl 0 0 100 0.3),
    :min-width 12,
    :color :white,
    :font-family "Menlo",
    :font-size 14}))

(defn on-focus [coord] (fn [e d! m!] (d! :writer/focus coord)))

(defn on-keydown [state leaf coord]
  (fn [e d! m!]
    (let [event (:original-event e)
          code (:key-code e)
          shift? (.-shiftKey event)
          text (if (> (:time state) (:time leaf)) (:text state) (:text leaf))
          text-length (count text)]
      (cond
        (= code keycode/delete) (if (and (= "" text)) (d! :ir/delete-node nil))
        (and (not shift?) (= code keycode/space))
          (do (d! :ir/leaf-after nil) (.preventDefault event))
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
        :else (println "Keydown leaf" code)))))

(defn on-input [state coord]
  (fn [e d! m!]
    (d! :ir/update-leaf (:value e))
    (m! (assoc state :text (:value e) :time (util/now!)))))

(def initial-state {:text "", :time 0})

(defcomp
 comp-leaf
 (states leaf focus coord)
 (let [state (or (:data states) initial-state)
       text (if (> (:time state) (:time leaf)) (:text state) (:text leaf))]
   (input
    {:value text,
     :class-name (if (= focus coord) "cirru-focused" nil),
     :placeholder coord,
     :style (merge
             style-leaf
             {:width (+
                      8
                      (text-width* text (:font-size style-leaf) (:font-family style-leaf)))}),
     :on {:click (on-focus coord),
          :keydown (on-keydown state leaf coord),
          :input (on-input state coord)}})))
