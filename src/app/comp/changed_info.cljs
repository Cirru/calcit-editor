
(ns app.comp.changed-info
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> >> <> span div pre input button a]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [feather.core :refer [comp-icon]]))

(defn on-preview [ns-text kind status]
  (fn [e d!]
    (comment println "peek" ns-text kind status)
    (d!
     :writer/select
     (case kind
       :ns {:kind :ns, :ns ns-text, :extra nil}
       :proc {:kind :proc, :ns ns-text, :extra nil}
       {:kind :def, :ns ns-text, :extra kind}))))

(defn on-reset-def [ns-text kind]
  (fn [e d!]
    (d!
     :ir/reset-at
     (case kind
       :ns {:ns ns-text, :kind :ns}
       :proc {:ns ns-text, :kind :proc}
       {:ns ns-text, :kind :def, :extra kind}))
    (d! :states/clear nil)))

(def style-reset
  {:text-decoration :underline, :font-size 12, :color (hsl 220 60 80 0.6), :cursor :pointer})

(def style-status {:font-size 12, :font-family "Josefin Sans", :color (hsl 160 70 40)})

(def style-status-card {:cursor :pointer})

(defn render-status [ns-text kind status]
  (span
   {:style style-status-card,
    :title (str "Browse " kind),
    :on-click (on-preview ns-text kind status)}
   (<> kind)
   (=< 8 nil)
   (<> (name status) style-status)
   (=< 4 nil)
   (span
    {:class-name "is-minor"}
    (comp-icon :corner-up-left style-reset (on-reset-def ns-text kind)))))

(def style-defs {:padding-left 16})

(def style-info {:background-color (hsl 0 0 100 0.1), :padding 8, :margin-bottom 8})

(defcomp
 comp-changed-info
 (info ns-text)
 (div
  {:style style-info}
  (div
   {}
   (<> ns-text)
   (=< 8 nil)
   (span
    {:class-name "is-minor"}
    (comp-icon
     :corner-up-left
     style-reset
     (fn [e d!] (d! :ir/reset-ns ns-text) (d! :states/clear nil))))
   (=< 24 nil)
   (if (not= :same (:ns info)) (render-status ns-text :ns (:ns info)))
   (=< 8 nil)
   (if (not= :same (:proc info)) (render-status ns-text :proc (:proc info))))
  (div
   {:style (merge ui/row-parted {:align-items :flex-end})}
   (list->
    :div
    {:style style-defs}
    (->> (:defs info)
         (map
          (fn [entry]
            (let [[def-text status] entry]
              [def-text (div {} (render-status ns-text def-text status))])))))
   (div {} (comp-icon :save style-reset (fn [e d!] (d! :effect/save-ns ns-text)))))))
