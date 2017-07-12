
(ns app.comp.search
  (:require-macros [respo.macros :refer [defcomp <> span div input a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [app.util.keycode :as keycode]
            [app.util :as util]
            [app.style :as style]))

(defn on-input [state] (fn [e d! m!] (m! {:query (:value e), :selection 0})))

(def style-body {:overflow :auto, :padding-bottom 80})

(def style-candidate {:padding "0 8px", :color (hsl 0 0 100 0.6), :cursor :pointer})

(def style-search {:padding "0 16px"})

(def style-highlight {:color :white})

(def style-input (merge style/input {:width 400}))

(defn on-select [bookmark]
  (fn [e d! m!] (m! {:position :0, :query ""}) (d! :writer/select bookmark)))

(def initial-state {:query "", :selection 0})

(defn bookmark->str [bookmark]
  (str (:kind bookmark) " " (:ns bookmark) " " (:extra bookmark)))

(defn on-keydown [state candidates]
  (fn [e d! m!]
    (let [code (:key-code e), event (:original-event e)]
      (cond
        (= keycode/enter code)
          (do
           (m! {:query "", :position 0})
           (d! :writer/select (get (vec candidates) (:selection state))))
        (= keycode/up code)
          (do
           (.preventDefault event)
           (if (pos? (:selection state)) (m! (update state :selection dec))))
        (= keycode/down code)
          (do
           (.preventDefault event)
           (if (< (:selection state) (dec (count candidates)))
             (m! (update state :selection inc))))
        :else nil))))

(defcomp
 comp-search
 (states router-data)
 (let [state (or (:data states) initial-state)
       queries (->> (string/split (:query state) " ") (map string/trim))
       candidates (->> router-data
                       (filter
                        (fn [bookmark]
                          (every?
                           (fn [y] (string/includes? (bookmark->str bookmark) y))
                           queries)))
                       (sort-by bookmark->str))]
   (div
    {:style (merge ui/column style-search)}
    (div
     {}
     (input
      {:placeholder "Type to search...",
       :value (:query state),
       :class-name "search-input",
       :style style-input,
       :on {:input (on-input state), :keydown (on-keydown state candidates)}}))
    (div
     {:style (merge ui/flex style-body)}
     (->> candidates
          (map-indexed
           (fn [idx bookmark]
             (let [text (bookmark->str bookmark)]
               [text
                (div
                 {:class-name "hoverable",
                  :style (merge
                          style-candidate
                          (if (= idx (:selection state)) style-highlight)),
                  :on {:click (on-select bookmark)}}
                 (<> span text nil))]))))))))
