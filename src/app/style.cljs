
(ns app.style (:require [respo-ui.core :as ui] [hsl.core :refer [hsl]]))

(def button
  {:background-color (hsl 0 0 100 0),
   :text-decoration :underline,
   :color (hsl 0 0 100 0.4),
   :min-width 80,
   :vertical-align :middle,
   :border :none,
   :line-height "30px",
   :font-size 14,
   :text-align :center,
   :padding "0 8px",
   :outline :none,
   :cursor :pointer})

(def font-code "Source Code Pro, monospace")

(def input
  (merge
   ui/input
   {:background-color (hsl 0 0 100 0.16),
    :color (hsl 0 0 100),
    :font-family "Menlo,monospace",
    :border :none}))

(def inspector {:opacity 0.9, :background-color (hsl 0 0 90), :color :black})

(def link ui/link)

(def title
  {:font-family ui/font-fancy, :font-size 20, :font-weight 100, :color (hsl 0 0 80)})
