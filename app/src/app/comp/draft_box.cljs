
(ns app.comp.draft-box
  (:require-macros [respo.macros :refer [defcomp <> span div textarea button a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.comp.modal :refer [comp-modal]]
            [app.style :as style]))

(def style-area
  {:background-color (hsl 0 0 100 0.2),
   :min-height 60,
   :min-width 600,
   :color :white,
   :font-family "Source Code Pro, monospace",
   :font-size 14})

(def style-wrong
  {:color :red,
   :font-size 24,
   :font-weight 100,
   :font-family "Josefin Sans",
   :cursor :pointer})

(def style-text {:font-family "Source Code Pro, monospace", :padding "0 8px"})

(defn on-input [e d! m!] (m! (:value e)))

(def style-toolbar {:justify-content :flex-end})

(defn on-submit [text close-modal!]
  (fn [e d! m!] (d! :ir/update-leaf text) (m! nil) (close-modal! m!)))

(defn on-wrong [close-modal!] (fn [e d! m!] (close-modal! m!)))

(defcomp
 comp-draft-box
 (states expr focus close-modal!)
 (comp-modal
  close-modal!
  (let [path (->> focus (mapcat (fn [x] [:data x])) (vec))
        original-text (get-in expr (conj path :text))
        state (or (:data states) original-text)]
    (if (nil? original-text)
      (span
       {:style style-wrong,
        :inner-text "Does not edit expression!",
        :on {:click (on-wrong close-modal!)}})
      (div
       {:style ui/column}
       (div {} (<> span original-text style-text))
       (=< nil 8)
       (textarea
        {:style (merge ui/textarea style-area),
         :value state,
         :class-name "el-draft-box",
         :on {:input on-input}})
       (=< nil 8)
       (div
        {:style (merge ui/row style-toolbar)}
        (button
         {:style style/button,
          :inner-text (str "Submit " (count state) " chars"),
          :on {:click (on-submit state close-modal!)}})))))))
