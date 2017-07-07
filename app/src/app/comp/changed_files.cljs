
(ns app.comp.changed-files
  (:require-macros [respo.macros :refer [defcomp <> span div pre input button a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.util :as util]
            [app.style :as style]
            [app.comp.changed-info :refer [comp-changed-info]]))

(defn on-save [e d! m!] (d! :writer/save-files nil))

(defcomp
 comp-changed-files
 (states changed-files)
 (div
  {}
  (<> div "Changed" style/title)
  (div
   {}
   (->> changed-files
        (map (fn [entry] (let [[k info] entry] [k (comp-changed-info info k)])))))
  (div {} (button {:inner-text "Save", :style style/button, :on {:click on-save}}))))
