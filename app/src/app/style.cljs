
(ns app.style (:require [respo-ui.style :as ui] [hsl.core :refer [hsl]]))

(def input (merge ui/input {}))

(def button (merge ui/button {}))

(def title
  {:font-family "Josefin Sans", :font-size 20, :font-weight 100, :color (hsl 0 0 80)})
