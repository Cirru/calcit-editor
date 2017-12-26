
(ns app.style (:require [respo-ui.style :as ui] [hsl.core :refer [hsl]]))

(def button
  (merge
   ui/button
   {:background-color (hsl 0 0 100 0),
    :text-decoration :underline,
    :color (hsl 0 0 100 0.4),
    :min-width 40,
    :vertical-align :middle}))

(def click {:text-decoration :underline})

(def input
  (merge
   ui/input
   {:background-color (hsl 0 0 100 0.16),
    :color (hsl 0 0 100),
    :font-family "Menlo,monospace"}))

(def inspector {:opacity 0.9, :background-color (hsl 0 0 90), :color :black})

(def title
  {:font-family "Josefin Sans", :font-size 20, :font-weight 100, :color (hsl 0 0 80)})
