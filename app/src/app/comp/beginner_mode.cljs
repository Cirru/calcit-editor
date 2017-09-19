
(ns app.comp.beginner-mode
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a pre]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def style-beginner
  {:color (hsl 0 0 100 0.5), :font-family "Josefin Sans", :font-weight 100, :cursor :pointer})

(def style-active {:color (hsl 0 0 100)})

(defn comp-beginner-mode [beginner? toggler]
  (span
   {:style (merge style-beginner (if beginner? style-active)), :on {:click toggler}}
   (<> span "Beginner?" nil)))

(defn on-toggle [state *cursor*]
  (fn [e d! m!] (comment println "Toggleing" *cursor* state) (m! *cursor* (not state))))
