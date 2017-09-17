
(ns app.comp.modal
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div pre input button a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]))

(def style-backdrop
  (merge
   ui/center
   {:position :fixed,
    :width "100%",
    :height "100%",
    :top 0,
    :left 0,
    :background-color (hsl 0 0 0 0.6)}))

(defcomp
 comp-modal
 (inner-tree close-modal!)
 (div
  {:style style-backdrop, :on {:click (fn [e d! m!] (close-modal! m!))}}
  (div {:on {:click (fn [e d! m!] (println "nothing!"))}} inner-tree)))
