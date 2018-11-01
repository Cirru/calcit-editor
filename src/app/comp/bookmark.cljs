
(ns app.comp.bookmark
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.core :refer [defcomp <> span div a]]
            [respo.comp.space :refer [=<]]))

(defn on-pick [bookmark idx]
  (fn [e d! m!]
    (let [event (:original-event e)
          shift? (.-shiftKey event)
          alt? (.-altKey event)
          meta? (.-metaKey event)]
      (cond
        meta? (d! :writer/collapse idx)
        alt? (d! :writer/remove-idx idx)
        :else (d! :writer/point-to idx)))))

(def style-bookmark
  {:line-height "1.2em",
   :padding "4px 8px",
   :cursor :pointer,
   :position :relative,
   :white-space :nowrap})

(def style-highlight {:color (hsl 0 0 100)})

(def style-kind
  {:color (hsl 0 0 50),
   :font-family "Josefin Sans",
   :font-size 14,
   :margin-left 8,
   :vertical-align :middle})

(def style-main {:vertical-align :middle, :color (hsl 0 0 70)})

(def style-minor {:color (hsl 0 0 50), :font-size 12})

(defcomp
 comp-bookmark
 (bookmark idx selected?)
 (case (:kind bookmark)
   :def
     (div
      {:class-name "stack-bookmark",
       :style (merge style-bookmark),
       :on {:click (on-pick bookmark idx)}}
      (div
       {}
       (span
        {:inner-text (:extra bookmark),
         :style (merge style-main (if selected? style-highlight))}))
      (div {} (<> span "def" style-kind) (=< 8 nil) (<> span (:ns bookmark) style-minor)))
   (div
    {:class-name "stack-bookmark",
     :style (merge style-bookmark),
     :on {:click (on-pick bookmark idx)}}
    (div {} (<> span (:ns bookmark) (merge style-main (if selected? style-highlight))))
    (div {} (<> span (name (:kind bookmark)) style-kind)))))

(defn on-remove [idx] (fn [e d! m!] (d! :writer/remove-idx idx)))

(def style-remove {:color (hsl 0 0 40), :cursor :pointer, :vertical-align :middle})
