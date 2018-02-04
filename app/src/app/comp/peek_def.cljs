
(ns app.comp.peek-def
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> action-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.util :refer [stringify-s-expr tree->cirru]]
            [respo-ui.comp.icon :refer [comp-android-icon]]))

(defcomp
 comp-peek-def
 (simple-expr)
 (div
  {:style (merge
           ui/row
           {:align-items :center, :color (hsl 0 0 50), :font-size 12, :line-height "1.5em"})}
  (<>
   (stringify-s-expr (tree->cirru simple-expr))
   {:font-family "Source Code Pro, Iosevka,Consolas,monospace", :overflow :hidden})
  (=< 8 nil)
  (span
   {:on-click (action-> :writer/hide-peek nil), :style {:cursor :pointer}}
   (comp-android-icon :close))))
