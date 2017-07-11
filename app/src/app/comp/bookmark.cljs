
(ns app.comp.bookmark
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(def style-minor {:color (hsl 0 0 50), :font-size 12})

(defn on-pick [bookmark idx]
  (fn [e d! m!]
    (let [event (:original-event e), shift? (.-shiftKey event), meta? (.-metaKey event)]
      (cond
        shift?
          (let [new-name (js/prompt
                          "Rename:"
                          (if (= :def (:kind bookmark))
                            (str (:ns bookmark) "/" (:extra bookmark))
                            (:ns bookmark)))
                [ns-text def-text] (string/split new-name "/")]
            (d!
             :ir/rename
             {:kind (:kind bookmark),
              :ns {:from (:ns bookmark), :to ns-text},
              :extra {:from (:extra bookmark), :to def-text},
              :index idx}))
        meta? (d! :writer/collapse idx)
        :else (d! :writer/point-to idx)))))

(def style-highlight {:color (hsl 0 0 100)})

(def style-remove {:color (hsl 0 0 40), :cursor :pointer, :vertical-align :middle})

(def style-kind
  {:color (hsl 0 0 50),
   :font-family "Josefin Sans",
   :font-size 14,
   :margin-left 8,
   :vertical-align :middle})

(defn on-remove [idx] (fn [e d! m!] (d! :writer/remove-idx idx)))

(def style-bookmark
  {:line-height "1.2em", :padding "4px 8px", :cursor :pointer, :position :relative})

(def style-main {:vertical-align :middle, :color (hsl 0 0 70)})

(defcomp
 comp-bookmark
 (bookmark idx selected?)
 (case (:kind bookmark)
   :def
     (div
      {:style (merge style-bookmark), :on {:click (on-pick bookmark idx)}}
      (div
       {}
       (span
        {:inner-text (:extra bookmark),
         :style (merge style-main (if selected? style-highlight))}))
      (div
       {}
       (<> span "def" style-kind)
       (=< 8 nil)
       (<> span (:ns bookmark) style-minor)
       (=< 8 nil)
       (span
        {:class-name "ion-md-close", :style style-remove, :on {:click (on-remove idx)}})))
   (div
    {:style (merge style-bookmark), :on {:click (on-pick bookmark idx)}}
    (div {} (<> span (:ns bookmark) (merge style-main (if selected? style-highlight))))
    (div
     {}
     (<> span (name (:kind bookmark)) style-kind)
     (=< 8 nil)
     (span {:class-name "ion-md-close", :style style-remove, :on {:click (on-remove idx)}})))))
