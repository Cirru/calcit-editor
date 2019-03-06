
(ns app.comp.repl-page
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.core
             :refer
             [defcomp action-> mutation-> list-> input <> span div a button]]
            [respo.comp.space :refer [=<]]
            [app.util.dom :refer [focus-search!]]
            [app.style :as style]
            [respo.util.list :refer [map-val]]
            [keycode.core :as keycode]))

(defcomp
 comp-repl-page
 (states router)
 (let [data (:data router)
       state (or (:data states) {:port nil, :code "", :build-id "client"})]
   (div
    {:style (merge ui/column {:padding "0 16px"})}
    (if (:alive? data)
      (div
       {:style ui/column}
       (div
        {}
        (input
         {:style (merge style/input {:width 400}),
          :value (:code state),
          :on-input (mutation-> (assoc state :code (:value %e))),
          :on-keydown (fn [e d! m!]
            (if (= keycode/return (:key-code e)) (d! :effect/send-code (:code state)))),
          :placeholder "Clojure(Script) code to run"})
        (=< 8 nil)
        (a
         {:style style/link,
          :on-click (action-> :effect/send-code (str "(println " (:code state) ")"))}
         (<> "Run"))
        (=< 8 nil)
        (a {:style style/link, :on-click (action-> :repl/clear-logs nil)} (<> "Clear"))
        (=< 8 nil)
        (input
         {:style (merge style/input {:width 120}),
          :value (:build-id state),
          :on-input (mutation-> (assoc state :build-id (:value %e))),
          :placeholder "build-id"})
        (=< 8 nil)
        (a
         {:style style/link,
          :on-click (action-> :effect/cljs-repl (keyword (:build-id state)))}
         (<> "Connect to runtime"))
        (=< 8 nil)
        (a
         {:style (merge style/link), :on-click (action-> :effect/end-repl nil)}
         (<> "Exit")))
       (list->
        :pre
        {:style (merge
                 ui/flex
                 {:margin 0,
                  :line-height "1.6em",
                  :overflow :auto,
                  :font-size 12,
                  :font-family ui/font-code,
                  :white-space :pre-line,
                  :padding "16px 0"})}
        (->> (:logs data)
             (sort-by (fn [[k log]] (- 0 (:time log))))
             (map-val (fn [log] (div {} (<> (:text log))))))))
      (div
       {}
       (<> "No connection.")
       (=< 8 nil)
       (input
        {:style (merge style/input),
         :type "number",
         :value (:port state),
         :on-input (mutation-> (assoc state :port (:value %e))),
         :placeholder "Socket REPL Port"})
       (=< 8 nil)
       (button
        {:style style/button, :on-click (action-> :effect/connect-repl (:port state))}
        (<> "Try to connect")))))))
