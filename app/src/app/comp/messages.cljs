
(ns app.comp.messages
  (:require-macros [respo.macros :refer [defcomp <> span div pre input button a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.util :as util]
            [app.style :as style]))

(def style-message
  {:position :absolute,
   :right 8,
   :bottom 8,
   :cursor :pointer,
   :font-weight 100,
   :font-family "Hind",
   :background-color (hsl 0 0 0 0.7),
   :border (str "1px solid " (hsl 0 0 100 0.2)),
   :padding "0 8px"})

(defn on-clear [e d! m!] (d! :notify/clear nil))

(defcomp
 comp-messages
 (messages)
 (div
  {}
  (->> messages
       (take-last 4)
       (map-indexed
        (fn [idx msg]
          [(:id msg)
           (div
            {:style (merge
                     style-message
                     {:bottom (+ 8 (* idx 40))}
                     {:color (case (:kind msg)
                        :error (hsl 0 80 80)
                        :warning (hsl 60 80 80)
                        :info (hsl 240 80 80)
                        (hsl 120 80 80))}),
             :on {:click on-clear}}
            (<> span (:text msg) nil))])))))
