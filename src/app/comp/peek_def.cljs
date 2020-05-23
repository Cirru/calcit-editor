
(ns app.comp.peek-def
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.util :refer [stringify-s-expr tree->cirru]]
            [feather.core :refer [comp-icon]]))

(defcomp
 comp-peek-def
 (simple-expr)
 (div
  {:style (merge
           ui/row
           {:align-items :center, :color (hsl 0 0 50), :font-size 12, :line-height "1.5em"})}
  (<>
   (stringify-s-expr (tree->cirru simple-expr))
   {:font-family "Source Code Pro, Iosevka,Consolas,monospace",
    :white-space :nowrap,
    :overflow :hidden,
    :text-overflow :ellipsis,
    :max-width 480})
  (comp-icon
   :delete
   {:font-size 12, :color (hsl 0 0 50), :cursor :pointer, :margin-left 8}
   (fn [e d!] (d! :writer/hide-peek nil)))))
