
(ns app.comp.modal
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div pre input button a]]
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
 (close-modal! inner-tree)
 (div
  {:style style-backdrop, :on {:click (fn [e d! m!] (close-modal! m! d!))}}
  (div {:on {:click (fn [e d! m!] (println "nothing!"))}} inner-tree)))
