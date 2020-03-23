
(ns app.comp.theme-menu
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> list-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def theme-list [:star-trail :beginner :curves])

(defcomp
 comp-theme-menu
 (states theme)
 (let [cursor (:cursor states), state (if (some? (:data states)) (:data states) false)]
   (div
    {:style {:position :relative,
             :width 60,
             :color (hsl 0 0 80 0.4),
             :font-family "Josefin Sans,sans-serif",
             :cursor :pointer,
             :display :inline-block},
     :on-click (fn [e d!] (d! cursor (not state)))}
    (<> (or theme "no theme"))
    (if state
      (list->
       :div
       {:style {:position :absolute,
                :bottom "100%",
                :right 0,
                :background-color :black,
                :border (str "1px solid " (hsl 0 0 100 0.2))},
        :on-click #()}
       (->> theme-list
            (map
             (fn [theme-name]
               [theme-name
                (div
                 {:style (merge
                          {:color (hsl 0 0 70), :padding "0 8px"}
                          (when (= theme theme-name) {:color :white})),
                  :on-click (fn [e d!] (d! :user/change-theme theme-name) (d! cursor false))}
                 (<> theme-name))]))))))))
