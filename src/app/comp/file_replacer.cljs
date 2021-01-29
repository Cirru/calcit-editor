
(ns app.comp.file-replacer
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div pre input button a textarea]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.modal :refer [comp-modal]]
            [cljs.reader :refer [read-string]]
            [flavored-edn.core :refer [write-edn]]))

(defcomp
 comp-file-replacer
 (states file)
 (let [cursor (:cursor states), state (or (:data states) (write-edn file {:indent 2}))]
   (comp-modal
    (fn [d!] (d! :writer/draft-ns nil))
    (div
     {:style ui/column}
     (textarea
      {:value state,
       :style (merge style/input {:width 800, :height 400}),
       :on-input (fn [e d!] (d! cursor (:value e)))})
     (=< nil 8)
     (div
      {:style (merge ui/row {:justify-content :flex-end})}
      (button
       {:inner-text "Submit",
        :style style/button,
        :on-click (fn [e d!]
          (if (not= state (write-edn file {:indent 2}))
            (d! :ir/replace-file (read-string state)))
          (d! cursor nil)
          (d! :writer/draft-ns nil))}))))))
