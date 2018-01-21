
(ns app.comp.peek-def
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.util :refer [stringify-s-expr tree->cirru]]))

(defcomp
 comp-peek-def
 (simple-expr)
 (div
  {:style {:font-family "Source Code Pro, Iosevka,Consolas,monospace",
           :height "1.5em",
           :font-size 12,
           :color (hsl 0 0 50),
           :overflow :hidden}}
  (<> (stringify-s-expr (tree->cirru simple-expr)))))
