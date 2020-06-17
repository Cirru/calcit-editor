
(ns app.comp.repl-preview
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> list-> <> span div pre input button a textarea]]
            [respo.comp.space :refer [=<]]
            [respo.util.list :refer [map-val]]
            [app.style :as style]
            [cljs.reader :refer [read-string]]))

(def style-clear {:color (hsl 0 0 100 0.6), :font-family ui/font-fancy, :cursor :pointer})

(defcomp
 comp-repl-preview
 (logs)
 (div
  {:style (merge
           ui/column
           {:position :fixed,
            :top 48,
            :right 20,
            :max-width "40%",
            :max-height "30%",
            :padding "2px 4px",
            :background-color (hsl 0 0 40 0.3),
            :border-radius "4px"})}
  (div
   {:style ui/row-parted}
   (<> "REPL" {:font-family ui/font-fancy, :font-weight 300})
   (=< 8 nil)
   (span
    {:style style-clear,
     :inner-text "Clear",
     :on-click (fn [e d!] (d! :repl/clear-logs nil))}))
  (list->
   :pre
   {:style (merge
            ui/flex
            {:margin 0,
             :line-height "1.6em",
             :overflow :auto,
             :font-size 11,
             :font-family ui/font-code,
             :white-space :pre-line,
             :padding "2px 0px",
             :user-select :text})}
   (->> logs
        (sort-by (fn [[k log]] (- 0 (:time log))))
        (map-val
         (fn [log]
           (div
            {:style (case (log :type)
               :output {:color (hsl 0 0 60)}
               :value {:color (hsl 220 80 70)}
               :error {:color (hsl 0 80 60)}
               {})}
            (<> (:text log)))))))))
