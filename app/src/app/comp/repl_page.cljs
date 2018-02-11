
(ns app.comp.repl-page
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros
             :refer
             [defcomp action-> mutation-> list-> input <> span div a button]]
            [respo.comp.space :refer [=<]]
            [app.util.dom :refer [focus-search!]]
            [app.style :as style]
            [respo.util.list :refer [map-val]]))

(defcomp
 comp-repl-page
 (states router)
 (let [data (:data router), state (or (:data states) {:port 5900, :code ""})]
   (div
    {:style {:padding "0 16px"}}
    (if (:alive? data)
      (div
       {}
       (<> "connection is alive")
       (div
        {}
        (input
         {:style (merge style/input {:width 400}),
          :value (:code state),
          :on-input (mutation-> (assoc state :code (:value %e)))})
        (=< 8 nil)
        (button
         {:style style/button, :on-click (action-> :effect/send-code (:code state))}
         (<> "Run"))
        (=< 8 nil)
        (button
         {:style style/button, :on-click (action-> :repl/clear-logs nil)}
         (<> "Clear"))
        (=< 8 nil)
        (button
         {:style style/button, :on-click (action-> :effect/cljs-repl nil)}
         (<> "Browser")))
       (list->
        :pre
        {:style {:margin 0, :line-height "1.4em"}}
        (->> (:logs data)
             (sort-by (fn [[k log]] (:time log)))
             (map-val (fn [log] (div {} (<> (:text log))))))))
      (div
       {}
       (<> "no connection.")
       (=< 8 nil)
       (input
        {:style style/input,
         :type "number",
         :value (:port state),
         :on-input (mutation-> (assoc state :port (:value %e)))})
       (=< 8 nil)
       (button
        {:style style/button, :on-click (action-> :effect/connect-repl (:port state))}
        (<> "Try to connect")))))))
