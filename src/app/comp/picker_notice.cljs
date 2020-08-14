
(ns app.comp.picker-notice
  (:require [respo.core :refer [defcomp list-> >> <> span div a pre]]
            [clojure.string :as string]
            [respo-ui.core :as ui]
            [hsl.core :refer [hsl]]
            [respo.comp.space :refer [=<]]))

(def style-container
  {:padding "4px 8px",
   :margin "8px 0",
   :background-color (hsl 0 0 30 0.5),
   :position :fixed,
   :top 8,
   :right 20,
   :z-index 100,
   :border-radius "4px",
   :max-width "32vw"})

(def style-name
  {:font-family ui/font-code,
   :cursor :pointer,
   :font-size 11,
   :margin-right 3,
   :margin-bottom 3,
   :word-break :none,
   :line-height "14px",
   :background-color (hsl 0 0 30),
   :padding "1px 3px",
   :display :inline-block})

(defcomp
 comp-picker-notice
 (choices target-node)
 (let [imported-names (:imported choices)
       defined-names (:defined choices)
       render-code (fn [x]
                     (span
                      {:inner-text x,
                       :style style-name,
                       :on-click (fn [e d!] (d! :writer/pick-node x))}))
       hint (if (some? target-node) (:text target-node) nil)
       hint-fn (fn [x] (if (string/blank? hint) false (string/includes? x hint)))]
   (div
    {:style style-container}
    (div
     {:style {:font-family ui/font-fancy,
              :font-size 16,
              :font-weight 300,
              :color (hsl 0 0 80),
              :cursor :pointer},
      :on-click (fn [e d!] (d! :writer/picker-mode nil))}
     (<> "Picker mode: pick a target..."))
    (let [possible-names (->> (concat imported-names defined-names)
                              distinct
                              (filter hint-fn))]
      (if-not (empty? possible-names)
        (div
         {}
         (list->
          {}
          (->> possible-names
               (sort-by (fn [x] (string/index-of x hint)))
               (map (fn [x] [x (render-code x)]))))
         (=< nil 8))))
    (let [filtered-names (->> imported-names (remove hint-fn))]
      (if-not (empty? filtered-names)
        (div
         {}
         (list-> {} (->> filtered-names sort (map (fn [x] [x (render-code x)]))))
         (=< nil 8))))
    (list-> {} (->> defined-names (remove hint-fn) sort (map (fn [x] [x (render-code x)])))))))
