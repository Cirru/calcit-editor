
(ns app.style (:require [respo-ui.style :as ui] [hsl.core :refer [hsl]]))

(def input (merge ui/input {}))

(def button (merge ui/button {}))

(def header
  {:font-family "Josefin Sans", :font-size 16, :font-weight 100, :color (hsl 0 0 80)})
