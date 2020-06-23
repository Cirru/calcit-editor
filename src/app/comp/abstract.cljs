
(ns app.comp.abstract
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.modal :refer [comp-modal]]
            [keycode.core :as keycode]))

(defcomp
 comp-abstract
 (states close-modal!)
 (comp-modal
  close-modal!
  (let [cursor (:cursor states), state (or (:data states) "style-")]
    (div
     {}
     (input
      {:style style/input,
       :class-name "el-abstract",
       :value state,
       :on-input (fn [e d!] (d! cursor (:value e))),
       :on-keydown (fn [e d!]
         (cond
           (= keycode/return (:key-code e))
             (if (not (string/blank? state))
               (do (d! :analyze/abstract-def state) (d! cursor nil) (close-modal! d!)))
           (= (:keycode e) keycode/escape) (close-modal! d!)))})
     (=< nil 8)
     (button
      {:style style/button,
       :inner-text "Submit",
       :on-click (fn [e d!]
         (if (not (string/blank? state))
           (do (d! :analyze/abstract-def state) (d! cursor nil) (close-modal! d!))))})))))
