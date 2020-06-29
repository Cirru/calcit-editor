
(ns app.comp.changed-files
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div pre input button a]]
            [respo.comp.space :refer [=<]]
            [app.client-util :as util]
            [app.style :as style]
            [app.comp.changed-info :refer [comp-changed-info]]))

(def style-column {:overflow :auto, :padding-bottom 120})

(def style-nothing {:font-family "Josefin Sans", :color (hsl 0 0 100 0.5)})

(defcomp
 comp-changed-files
 (states changed-files)
 (div
  {:style style-column}
  (<> div "Changes" style/title)
  (list-> :div {} (->> changed-files (map (fn [[k info]] [k (comp-changed-info info k)]))))
  (if (empty? changed-files)
    (div {:style style-nothing} (<> "No changes"))
    (div
     {}
     (a
      {:inner-text "Save",
       :style style/button,
       :on-click (fn [e d!] (d! :effect/save-files nil))})
     (a
      {:inner-text "Reset",
       :style style/button,
       :on-click (fn [e d!] (d! :ir/reset-files nil) (d! :states/clear nil))})))))
