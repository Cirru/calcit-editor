
(ns app.comp.repl-page
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
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
       state (or (:data states) {:code "", :build-id "client", :ns "cljs.user"})]
   (div
    {:style (merge ui/column {:padding "0 16px"})}
    (if (:alive? data)
      (div
       {:style ui/column}
       (div
        {}
        (input
         {:style (merge style/input {:width 160}),
          :value (:ns state),
          :on-input (mutation-> (assoc state :ns (:value %e))),
          :placeholder "ns"})
        (=< 8 nil)
        (input
         {:style (merge style/input {:width 320}),
          :value (:code state),
          :on-input (mutation-> (assoc state :code (:value %e))),
          :on-keydown (fn [e d! m!]
            (if (= keycode/return (:key-code e))
              (d! :effect/send-code {:code (:code state), :ns (:ns state)}))),
          :placeholder "Clojure(Script) code to run"})
        (=< 8 nil)
        (a
         {:style style/link,
          :on-click (action->
                     :effect/send-code
                     {:code (str "(println " (:code state) ")"), :ns (:ns state)})}
         (<> "Run"))
        (=< 8 nil)
        (a {:style style/link, :on-click (action-> :repl/clear-logs nil)} (<> "Clear"))
        (=< 48 nil)
        (input
         {:style (merge style/input {:width 120}),
          :value (:build-id state),
          :on-input (mutation-> (assoc state :build-id (:value %e))),
          :placeholder "build-id"})
        (=< 8 nil)
        (a
         {:style style/link,
          :on-click (action-> :effect/cljs-repl (keyword (:build-id state)))}
         (<> "Connect runtime"))
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
                  :padding "16px 0",
                  :user-select :text})}
        (->> (:logs data)
             (sort-by (fn [[k log]] (- 0 (:time log))))
             (map-val (fn [log] (div {} (<> (:text log))))))))
      (div
       {}
       (<> "No connection.")
       (=< 8 nil)
       (button
        {:style style/button, :on-click (action-> :effect/connect-repl nil)}
        (<> "Try to connect")))))))
