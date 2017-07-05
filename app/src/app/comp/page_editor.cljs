
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.bookmark :refer [comp-bookmark]]
            [app.comp.expr :refer [comp-expr]]
            [app.style :as style]))

(def style-stack {:width 240})

(def style-editor (merge ui/flex {}))

(def style-container {:position :relative})

(defcomp
 comp-page-editor
 (states stack router-data)
 (div
  {:style (merge ui/row ui/flex style-container)}
  (div
   {:style style-stack}
   (->> stack (map-indexed (fn [idx bookmark] [idx (comp-bookmark bookmark idx)]))))
  (div
   {:style style-editor}
   (comp-expr router-data [])
   (comp-inspect "Expr" router-data style/inspector))))
