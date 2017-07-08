
(ns app.comp.bookmark
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(def style-minor {:color (hsl 0 0 50)})

(defn on-pick [idx] (fn [e d! m!] (d! :writer/point-to idx)))

(def style-highlight {:background-color (hsl 0 0 100 0.2)})

(def style-remove
  {:color (hsl 0 0 40), :cursor :pointer, :position :absolute, :top 4, :right 8})

(def style-kind {:color (hsl 0 0 50), :font-family "Josefin Sans"})

(defn on-remove [idx] (fn [e d! m!] (d! :writer/remove-idx idx)))

(def style-bookmark
  {:line-height "1.2em", :padding "4px 8px", :cursor :pointer, :position :relative})

(def style-main {:vertical-align :middle})

(defcomp
 comp-bookmark
 (bookmark idx selected?)
 (case (:kind bookmark)
   :def
     (div
      {:style (merge style-bookmark (if selected? style-highlight)),
       :on {:click (on-pick idx)}}
      (div
       {}
       (span {:inner-text (:extra bookmark), :style style-main})
       (=< 8 nil)
       (span
        {:class-name "ion-md-close", :style style-remove, :on {:click (on-remove idx)}}))
      (div {} (<> span "def" style-kind) (=< 8 nil) (<> span (:ns bookmark) style-minor)))
   (div
    {:style (merge style-bookmark (if selected? style-highlight)),
     :on {:click (on-pick idx)}}
    (div
     {}
     (<> span (:ns bookmark) nil)
     (=< 16 nil)
     (span {:class-name "ion-md-close", :style style-remove, :on {:click (on-remove idx)}}))
    (div {} (<> span (name (:kind bookmark)) style-kind)))))
