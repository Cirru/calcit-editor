
(ns app.comp.rename
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.comp.modal :refer [comp-modal]]))

(defn on-input [e d! m!] (m! (:value e)))

(defn on-keydown [new-name bookmark close-rename!]
  (fn [e d! m!]
    (case (:key-code e)
      13
        (let [[ns-text def-text] (string/split new-name "/")]
          (if (not (string/blank? new-name))
            (do
             (d!
              :ir/rename
              {:kind (:kind bookmark),
               :ns {:from (:ns bookmark), :to ns-text},
               :extra {:from (:extra bookmark), :to def-text}})
             (m! nil)
             (close-rename! m!))))
      27 (close-rename! m!)
      (println "unkown keycode" e))))

(def style-input {:min-width 360})

(defcomp
 comp-rename
 (states old-name close-rename! bookmark)
 (let [current-name (or (:data states) old-name)]
   (comp-modal
    close-rename!
    (div
     {}
     (div {} (<> (str "Rename " old-name " to:")))
     (input
      {:style (merge style/input style-input),
       :class-name "el-rename",
       :value current-name,
       :placeholder old-name,
       :on {:input on-input, :keydown (on-keydown current-name bookmark close-rename!)}})))))
