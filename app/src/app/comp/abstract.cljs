
(ns app.comp.abstract
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.modal :refer [comp-modal]]
            [app.util.keycode :as keycode]))

(defn on-input [e d! m!] (m! (:value e)))

(defn on-keydown [state close-modal!]
  (fn [e d! m!]
    (cond
      (= keycode/enter (:key-code e))
        (if (not (string/blank? state))
          (do (d! :analyze/abstract-def state) (m! nil) (close-modal! m!)))
      (= (:keycode e) keycode/esc) (close-modal! m!))))

(defn on-submit [state close-modal!]
  (fn [e d! m!]
    (if (not (string/blank? state))
      (do (d! :analyze/abstract-def state) (m! nil) (close-modal! m!)))))

(defcomp
 comp-abstract
 (states close-modal!)
 (comp-modal
  close-modal!
  (let [state (or (:data states) "style-")]
    (div
     {}
     (input
      {:style style/input,
       :class-name "el-abstract",
       :value state,
       :on {:input on-input, :keydown (on-keydown state close-modal!)}})
     (=< nil 8)
     (button
      {:style style/button,
       :inner-text "Submit",
       :on {:click (on-submit state close-modal!)}})))))
