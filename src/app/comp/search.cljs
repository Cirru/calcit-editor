
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
    :def (:extra bookmark)
    :ns (:ns bookmark)
    (do (js/console.warn (str "Unknown" (pr-str bookmark))) "")))

(defcomp
 comp-no-results
 ()
 (div
  {:style (merge
           ui/row-middle
           {:padding 8, :font-family ui/font-fancy, :color (hsl 0 0 60), :font-weight 300})}
  (<> "No results")))

(def initial-state {:query "", :selection 0})

(defn on-input [state cursor] (fn [e d!] (d! cursor {:query (:value e), :selection 0})))

(defn on-keydown [state candidates cursor]
  (fn [e d!]
    (let [code (:key-code e), event (:original-event e)]
      (cond
        (= keycode/return code)
          (let [target (get (vec candidates) (:selection state))]
            (if (some? target)
              (do (d! :writer/select target) (d! cursor {:query "", :position 0}))))
        (= keycode/up code)
          (do
           (.preventDefault event)
           (if (pos? (:selection state)) (d! cursor (update state :selection dec))))
        (= keycode/escape code)
          (do (d! :router/change {:name :editor}) (d! cursor {:query "", :position 0}))
        (= keycode/down code)
          (do
           (.preventDefault event)
           (if (< (:selection state) (dec (count candidates)))
             (d! cursor (update state :selection inc))))
        :else (on-window-keydown (:event e) d! {:name :search})))))

(defn on-select [bookmark cursor]
  (fn [e d!] (d! :writer/select bookmark) (d! cursor {:position :0, :query ""})))

(defn query-length [bookmark]
  (case (:kind bookmark)
    :def (count (:extra bookmark))
    :ns (count (:ns bookmark))
    (do (js/console.warn (str "Unknown" (pr-str bookmark))) 0)))

(def style-body {:overflow :auto, :padding-bottom 80})

(def style-candidate {:padding "0 8px", :color (hsl 0 0 100 0.6), :cursor :pointer})

(def style-highlight {:color :white})

(defcomp
 comp-search
 (states router-data)
 (let [cursor (:cursor states)
       state (or (:data states) initial-state)
       queries (->> (string/split (:query state) " ") (map string/trim))
       def-candidates (->> router-data
                           (filter
                            (fn [bookmark]
                              (and (= :def (:kind bookmark))
                                   (every?
                                    (fn [y] (string/includes? (:extra bookmark) y))
                                    queries))))
                           (sort-by
                            (if (string/blank? (:query state)) bookmark->str query-length)))
       ns-candidates (->> router-data
                          (filter
                           (fn [bookmark]
                             (and (= :ns (:kind bookmark))
                                  (every?
                                   (fn [y] (string/includes? (:ns bookmark) y))
                                   queries))))
                          (sort-by
                           (if (string/blank? (:query state)) bookmark->str query-length)))]
   (div
    {:style (merge ui/expand ui/row-middle {:height "100%", :padding "0 16px"})}
    (div
     {:style (merge ui/column {:width 320, :height "100%"})}
     (div
      {}
      (input
       {:placeholder "Type to search...",
        :value (:query state),
        :class-name "search-input",
        :style (merge style/input {:width "100%"}),
        :on-input (on-input state cursor),
        :on-keydown (on-keydown state def-candidates cursor)}))
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
                   :on-click (on-select bookmark cursor)}
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
                   :on-click (on-select bookmark cursor)}
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
                    :on-click (on-select (assoc bookmark :kind :proc) cursor)})))]))))))))
