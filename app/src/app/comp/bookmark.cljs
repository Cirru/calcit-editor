
(ns app.comp.bookmark
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(def style-kind {:color (hsl 0 0 50), :font-family "Josefin Sans"})

(def style-bookmark {:line-height "1.2em", :padding "4px 0", :cursor :pointer})

(defn on-pick [idx] (fn [e d! m!] (d! :writer/point-to idx)))

(def style-minor {:color (hsl 0 0 50)})

(defcomp
 comp-bookmark
 (bookmark idx)
 (case (:kind bookmark)
   :def
     (div
      {:style style-bookmark, :on {:click (on-pick idx)}}
      (div {} (<> span (:extra bookmark) nil))
      (div {} (<> span "def" style-kind) (=< 8 nil) (<> span (:ns bookmark) style-minor)))
   (div
    {:style style-bookmark, :on {:click (on-pick idx)}}
    (div {} (<> span (:ns bookmark) nil))
    (div {} (<> span (name (:kind bookmark)) style-kind)))))
