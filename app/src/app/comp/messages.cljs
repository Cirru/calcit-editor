
(ns app.comp.messages
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp list-> <> span div pre input button a]]
            [respo.comp.space :refer [=<]]
            [app.util :as util]
            [app.style :as style]))

(defn on-clear [e d! m!] (d! :notify/clear nil))

(def style-message
  {:position :absolute,
   :right 8,
   :cursor :pointer,
   :font-weight 100,
   :font-family "Hind",
   :background-color (hsl 0 0 0 0.7),
   :border (str "1px solid " (hsl 0 0 100 0.2)),
   :padding "0 8px"})

(defcomp
 comp-messages
 (messages)
 (list->
  :div
  {}
  (->> messages
       (take-last 4)
       (map-indexed
        (fn [idx msg]
          [(:id msg)
           (div
            {:style (merge
                     style-message
                     {:top (+ 52 (* idx 40))}
                     {:color (case (:kind msg)
                        :error (hsl 0 80 80)
                        :warning (hsl 60 80 80)
                        :info (hsl 240 80 80)
                        (hsl 120 80 80))}),
             :on {:click on-clear}}
            (<> span (:text msg) nil))])))))
