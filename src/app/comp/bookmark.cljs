
(ns app.comp.bookmark
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> span div a]]
            [respo.comp.space :refer [=<]]))

(defn on-pick [bookmark idx]
  (fn [e d!]
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
  {:color (hsl 340 80 60),
   :font-family ui/font-normal,
   :font-size 12,
   :margin-right 4,
   :vertical-align :middle})

(def style-main {:vertical-align :middle, :color (hsl 0 0 70), :font-family ui/font-normal})

(def style-minor {:color (hsl 0 0 40), :font-size 12})

(defcomp
 comp-bookmark
 (bookmark idx selected?)
 (div
  {:class-name "stack-bookmark",
   :draggable true,
   :on-click (on-pick bookmark idx),
   :on-dragstart (fn [e d!] (-> e :event .-dataTransfer (.setData "id" idx))),
   :on-drop (fn [e d!]
     (let [target-idx (js/parseInt (-> e :event .-dataTransfer (.getData "id")))]
       (when (not= target-idx idx) (d! :writer/move-order {:from target-idx, :to idx})))),
   :on-dragover (fn [e d!] (-> e :event .preventDefault))}
  (case (:kind bookmark)
    :def
      (div
       {:style (merge style-bookmark)}
       (div
        {}
        (span
         {:inner-text (:extra bookmark),
          :style (merge style-main (if selected? style-highlight))}))
       (div {:style ui/row-middle} (=< 4 nil) (<> (:ns bookmark) style-minor)))
    (div
     {:style (merge style-bookmark {:padding "8px"})}
     (<> span (str (:kind bookmark)) style-kind)
     (<> (:ns bookmark) (merge style-main (if selected? style-highlight)))))))
