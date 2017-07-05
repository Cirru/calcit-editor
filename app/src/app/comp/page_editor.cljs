
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.comp.bookmark :refer [comp-bookmark]]))

(def style-stack {:width 240})

(def style-editor (merge ui/flex {}))

(defcomp
 comp-page-editor
 (states stack router-data)
 (div
  {:style (merge ui/row ui/flex)}
  (div
   {:style style-stack}
   (->> stack (map-indexed (fn [idx bookmark] [idx (comp-bookmark bookmark idx)]))))
  (div {:style style-editor} (<> span "editor" nil))))
