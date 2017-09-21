
(ns app.comp.changed-info
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div pre input button a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def style-defs {:padding-left 16})

(def style-status {:font-size 12, :font-family "Josefin Sans", :color (hsl 160 70 40)})

(defn on-preview [ns-text kind status]
  (fn [e d! m!]
    (println "peek" ns-text kind status)
    (d!
     :writer/select
     (case kind
       :ns {:kind :ns, :ns ns-text, :extra nil}
       :proc {:kind :proc, :ns ns-text, :extra nil}
       {:kind :def, :ns ns-text, :extra kind}))))

(def style-status-card {:cursor :pointer})

(defn render-status [ns-text kind status]
  (span
   {:style style-status-card,
    :title (str "Browse " kind),
    :on {:click (on-preview ns-text kind status)}}
   (<> span kind nil)
   (=< 8 nil)
   (<> span (name status) style-status)))

(def style-info {:background-color (hsl 0 0 100 0.1), :padding 8, :margin-bottom 8})

(defcomp
 comp-changed-info
 (info ns-text)
 (div
  {:style style-info}
  (div
   {}
   (<> span ns-text nil)
   (=< 24 nil)
   (render-status ns-text :ns (:ns info))
   (=< 8 nil)
   (render-status ns-text :proc (:proc info)))
  (div
   {:style style-defs}
   (->> (:defs info)
        (map
         (fn [entry]
           (let [[def-text status] entry]
             [def-text (div {} (render-status ns-text def-text status))])))))))
