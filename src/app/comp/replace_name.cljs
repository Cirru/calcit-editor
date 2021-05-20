
(ns app.comp.replace-name
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> span div pre input button img a br]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [respo-alerts.core :refer [comp-modal]]))

(defn use-replace-name-modal [states on-replace]
  (let [cursor (:cursor states)
        state (or (:data states) {:old-name "", :new-name "", :show? false})
        on-submit (fn [d!]
                    (comment "special trick to use spaces to remove a leaf")
                    (when (and (not (string/blank? (:old-name state)))
                               (not (= (:new-name state) "")))
                      (on-replace (:old-name state) (:new-name state) d!)
                      (d! cursor (assoc state :show? false))))]
    {:ui (comp-modal
          {:title "Replace variable",
           :style {:width 240},
           :container-style {},
           :render-body (fn [[]]
             (div
              {:style {:padding 16}}
              (div
               {}
               (input
                {:placeholder "from...",
                 :style (merge ui/input {:font-family ui/font-code}),
                 :value (:old-name state),
                 :autofocus true,
                 :id "replace-input",
                 :on-input (fn [e d!] (d! cursor (assoc state :old-name (:value e))))}))
              (=< nil 8)
              (div
               {}
               (input
                {:placeholder "to...",
                 :style (merge ui/input {:font-family ui/font-code}),
                 :on-input (fn [e d!] (d! cursor (assoc state :new-name (:value e)))),
                 :value (:new-name state),
                 :on-keydown (fn [e d!] (if (= 13 (:key-code e)) (on-submit d!)))}))
              (=< nil 8)
              (div
               {:style ui/row-parted}
               (span nil)
               (button
                {:style ui/button,
                 :inner-text "Replace",
                 :on-click (fn [e d!] (on-submit d!))}))))}
          (:show? state)
          (fn [d!] (d! cursor (assoc state :show? false)))),
     :show (fn [d!]
       (d! cursor (assoc state :old-name "" :new-name "" :show? true))
       (js/setTimeout
        (fn []
          (let [el (js/document.querySelector "#replace-input")] (if (some? el) (.select el))))))}))
