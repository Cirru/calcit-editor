
(ns app.comp.file-replacer
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div pre input button a textarea]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.modal :refer [comp-modal]]
            [fipp.edn :refer [pprint]]
            [cljs.reader :refer [read-string]]))

(defcomp
 comp-file-replacer
 (states file)
 (let [initial-file (with-out-str (pprint file)), state (or (:data states) initial-file)]
   (comp-modal
    (fn [mutate! dispatch!] (dispatch! :writer/peek-ns nil))
    (div
     {:style ui/column}
     (textarea
      {:value state,
       :style (merge style/input {:width 800, :height 400}),
       :on {:input (fn [e d! m!] (m! (:value e)))}})
     (=< nil 8)
     (div
      {:style (merge ui/row {:justify-content :flex-end})}
      (button
       {:inner-text "Submit",
        :style style/button,
        :on {:click (fn [e d! m!]
               (if (not= state initial-file) (d! :ir/replace-file (read-string state)))
               (m! nil)
               (d! :writer/peek-ns nil))}}))))))
