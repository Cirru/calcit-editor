
(ns app.comp.theme-menu
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> list-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def theme-list [:star-trail :curves])

(defcomp
 comp-theme-menu
 (states theme)
 (let [state (if (some? (:data states)) (:data states) false)]
   (div
    {:style {:position :relative,
             :width 60,
             :color (hsl 0 0 80 0.4),
             :font-family "Josefin Sans,sans-serif",
             :cursor :pointer},
     :on {:click (fn [e d! m!] (m! (not state)))}}
    (<> (or theme "no theme"))
    (if state
      (list->
       :div
       {:style {:position :absolute, :bottom "100%", :left 0}, :on {:click #()}}
       (->> theme-list
            (map
             (fn [theme-name]
               [theme-name
                (div
                 {:style {:color (hsl 0 0 100)},
                  :on {:click (fn [e d! m!] (d! :user/change-theme theme-name))}}
                 (<> theme-name))]))))))))
