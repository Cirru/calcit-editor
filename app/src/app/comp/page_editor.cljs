
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a]])
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

(def style-missing
  {:font-family "Josefin Sans", :color (hsl 10 60 50), :font-size 20, :font-weight 100})

(def ui-missing (div {:style style-missing} (<> span "Expression is missing!" nil)))

(def style-nothing
  {:color (hsl 0 0 100 0.4), :padding "0 16px", :font-family "Josefin Sans"})

(defcomp
 comp-page-editor
 (states stack router-data pointer)
 (div
  {:style (merge ui/row ui/flex style-container)}
  (div
   {:style style-stack}
   (if (empty? stack)
     (<> span "Nothing selected" style-nothing)
     (->> stack
          (map-indexed
           (fn [idx bookmark] [idx (comp-bookmark bookmark idx (= idx pointer))])))))
  (=< 8 nil)
  (div
   {:style style-editor}
   (let [others (->> (:others router-data) (vals) (into #{})), expr (:expr router-data)]
     (if (some? expr)
       (cursor-> :root comp-expr states expr (:focus router-data) [] others false false)
       (if (not (empty? stack)) ui-missing)))
   (comment comp-inspect "Expr" router-data style/inspector))))
