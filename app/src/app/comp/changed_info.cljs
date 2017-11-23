
(ns app.comp.changed-info
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp list-> cursor-> <> span div pre input button a]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def style-reset
  {:text-decoration :underline, :font-size 12, :color (hsl 220 60 60 0.6), :cursor :pointer})

(def style-status {:font-size 12, :font-family "Josefin Sans", :color (hsl 160 70 40)})

(def style-status-card {:cursor :pointer})

(defn on-reset-def [ns-text kind]
  (fn [e d! m!]
    (d!
     :ir/reset-at
     (case kind
       :ns {:ns ns-text, :kind :ns}
       :proc {:ns ns-text, :kind :proc}
       {:ns ns-text, :kind :def, :extra kind}))))

(defn on-preview [ns-text kind status]
  (fn [e d! m!]
    (println "peek" ns-text kind status)
    (d!
     :writer/select
     (case kind
       :ns {:kind :ns, :ns ns-text, :extra nil}
       :proc {:kind :proc, :ns ns-text, :extra nil}
       {:kind :def, :ns ns-text, :extra kind}))))

(defn render-status [ns-text kind status]
  (span
   {:style style-status-card,
    :title (str "Browse " kind),
    :on {:click (on-preview ns-text kind status)}}
   (<> span kind nil)
   (=< 8 nil)
   (<> span (name status) style-status)
   (=< 4 nil)
   (span
    {:class-name "ion-arrow-return-left",
     :title "Reset this",
     :style style-reset,
     :on {:click (on-reset-def ns-text kind)}})))

(def style-info {:background-color (hsl 0 0 100 0.1), :padding 8, :margin-bottom 8})

(defn on-reset-ns [ns-text] (fn [e d! m!] (d! :ir/reset-ns ns-text)))

(def style-defs {:padding-left 16})

(defcomp
 comp-changed-info
 (info ns-text)
 (div
  {:style style-info}
  (div
   {}
   (<> span ns-text nil)
   (=< 4 nil)
   (span
    {:class-name "ion-arrow-return-left",
     :title "Reset this",
     :style style-reset,
     :on {:click (on-reset-ns ns-text)}})
   (=< 24 nil)
   (if (not= :same (:ns info)) (render-status ns-text :ns (:ns info)))
   (=< 8 nil)
   (if (not= :same (:proc info)) (render-status ns-text :proc (:proc info))))
  (list->
   :div
   {:style style-defs}
   (->> (:defs info)
        (map
         (fn [entry]
           (let [[def-text status] entry]
             [def-text (div {} (render-status ns-text def-text status))])))))))
