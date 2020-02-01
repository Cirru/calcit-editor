
(ns app.comp.search
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div input a]]
            [respo.comp.space :refer [=<]]
            [polyfill.core :refer [text-width*]]
            [keycode.core :as keycode]
            [app.client-util :as util]
            [app.style :as style]
            [app.util.shortcuts :refer [on-window-keydown]]))

(defn bookmark->str [bookmark]
  (case (:kind bookmark)
    :def (str (:ns bookmark) " " (:extra bookmark))
    :ns (str (:kind bookmark) " " (:ns bookmark) " " (:extra bookmark))
    :proc (str (:kind bookmark) " " (:ns bookmark) " " (:extra bookmark))
    (str "Unknown" (pr-str bookmark))))

(defcomp
 comp-no-results
 ()
 (div
  {:style (merge
           ui/center
           {:padding 20, :font-family ui/font-fancy, :color (hsl 0 0 60), :font-weight 300})}
  (<> "No results")))

(def initial-state {:query "", :selection 0})

(defn on-input [state] (fn [e d! m!] (m! {:query (:value e), :selection 0})))

(defn on-keydown [state candidates]
  (fn [e d! m!]
    (let [code (:key-code e), event (:original-event e)]
      (cond
        (= keycode/return code)
          (let [target (get (vec candidates) (:selection state))]
            (if (some? target) (do (d! :writer/select target) (m! {:query "", :position 0}))))
        (= keycode/up code)
          (do
           (.preventDefault event)
           (if (pos? (:selection state)) (m! (update state :selection dec))))
        (= keycode/escape code)
          (do (d! :router/change {:name :editor}) (m! {:query "", :position 0}))
        (= keycode/down code)
          (do
           (.preventDefault event)
           (if (< (:selection state) (dec (count candidates)))
             (m! (update state :selection inc))))
        :else (on-window-keydown (:event e) d! {:name :search})))))

(defn on-select [bookmark]
  (fn [e d! m!] (d! :writer/select bookmark) (m! {:position :0, :query ""})))

(def style-body {:overflow :auto, :padding-bottom 80})

(def style-candidate {:padding "0 8px", :color (hsl 0 0 100 0.6), :cursor :pointer})

(def style-highlight {:color :white})

(defcomp
 comp-search
 (states router-data)
 (let [state (or (:data states) initial-state)
       queries (->> (string/split (:query state) " ") (map string/trim))
       def-candidates (->> router-data
                           (filter
                            (fn [bookmark]
                              (and (= :def (:kind bookmark))
                                   (every?
                                    (fn [y] (string/includes? (:extra bookmark) y))
                                    queries))))
                           (sort-by bookmark->str))
       ns-candidates (->> router-data
                          (filter
                           (fn [bookmark]
                             (and (= :ns (:kind bookmark))
                                  (every?
                                   (fn [y] (string/includes? (:ns bookmark) y))
                                   queries))))
                          (sort-by bookmark->str))]
   (div
    {:style (merge ui/expand ui/row-middle {:height "100%", :padding "0 16px"})}
    (div
     {:style (merge ui/column {:width 480, :height "100%"})}
     (div
      {}
      (input
       {:placeholder "Type to search...",
        :value (:query state),
        :class-name "search-input",
        :style (merge style/input {:width "100%"}),
        :on {:input (on-input state), :keydown (on-keydown state def-candidates)}}))
     (if (empty? def-candidates) (comp-no-results))
     (list->
      :div
      {:style (merge ui/expand style-body)}
      (->> def-candidates
           (take 20)
           (map-indexed
            (fn [idx bookmark]
              (let [text (bookmark->str bookmark), selected? (= idx (:selection state))]
                [text
                 (div
                  {:class-name "hoverable",
                   :style (merge style-candidate (if selected? style-highlight)),
                   :on {:click (on-select bookmark)}}
                  (<> (:extra bookmark) nil)
                  (=< 8 nil)
                  (<>
                   (:ns bookmark)
                   (merge
                    {:font-size 12, :color (hsl 0 0 40)}
                    (if selected? style-highlight))))]))))))
    (div
     {:style (merge ui/column {:width 320, :height "100%"})}
     (=< nil 32)
     (if (empty? ns-candidates) (comp-no-results))
     (list->
      :div
      {:style (merge ui/expand style-body)}
      (->> ns-candidates
           (take 20)
           (map-indexed
            (fn [idx bookmark]
              [(:ns bookmark)
               (let [pieces (string/split (:ns bookmark) ".")]
                 (div
                  {:class-name "hoverable",
                   :style (merge ui/row-middle style-candidate),
                   :on-click (on-select bookmark)}
                  (span
                   {}
                   (<> (str (string/join "." (butlast pieces)) ".") {:color (hsl 0 0 50)})
                   (<> (last pieces) {:color (hsl 0 0 80)}))
                  (=< 12 nil)
                  (span
                   {:inner-text "proc",
                    :style {:padding "0 8px",
                            :background-color (hsl 0 0 20),
                            :font-size 12,
                            :line-height "18px"},
                    :on-click (on-select (assoc bookmark :kind :proc))})))]))))))))
