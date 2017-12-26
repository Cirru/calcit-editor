
(ns app.comp.changed-files
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp list-> <> span div pre input button a]]
            [respo.comp.space :refer [=<]]
            [app.util :as util]
            [app.style :as style]
            [app.comp.changed-info :refer [comp-changed-info]]))

(defn on-reset [e d! m!] (d! :ir/reset-files nil))

(defn on-save [e d! m!] (d! :effect/save-files nil))

(def style-column {:overflow :auto, :padding-bottom 120})

(def style-nothing {:font-family "Josefin Sans", :color (hsl 0 0 100 0.5)})

(defcomp
 comp-changed-files
 (states changed-files)
 (div
  {:style style-column}
  (<> div "Changes" style/title)
  (list->
   :div
   {}
   (->> changed-files
        (map (fn [entry] (let [[k info] entry] [k (comp-changed-info info k)])))))
  (if (empty? changed-files)
    (div {:style style-nothing} (<> span "No changes" nil))
    (div
     {}
     (button {:inner-text "Save", :style style/button, :on {:click on-save}})
     (button {:inner-text "Reset", :style style/button, :on {:click on-reset}})))))
