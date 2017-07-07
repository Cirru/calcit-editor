
(ns app.comp.changed-info
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div pre input button a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def style-defs {:padding-left 16})

(def style-status {:font-size 12, :font-family "Josefin Sans"})

(defn render-status [kind status]
  (span {} (<> span kind nil) (=< 8 nil) (<> span status style-status)))

(defcomp
 comp-changed-info
 (info ns-text)
 (div
  {}
  (div
   {}
   (<> span ns-text nil)
   (=< 24 nil)
   (render-status "ns" (:ns info))
   (=< 8 nil)
   (render-status "proc" (:proc info)))
  (div
   {:style style-defs}
   (->> (:defs info)
        (map
         (fn [entry]
           (let [[def-text status] entry] [def-text (div {} (render-status def-text status))])))))))
