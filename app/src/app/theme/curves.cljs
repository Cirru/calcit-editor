
(ns app.theme.curves
  (:require [app.theme.star-trail :as star-trail] [hsl.core :refer [hsl]]))

(defn decide-expr-style [expr has-others? focused? tail? after-expr? beginner? length depth]
  (merge
   {:border-radius "16px",
    :display :inline-block,
    :border-width "0 1px",
    :border-color (hsl 0 0 60),
    :padding "4px 8px"}
   (if focused? {:border-color (hsl 0 0 100)})))

(defn decide-leaf-style [text focused? first? by-other?]
  (merge (star-trail/decide-leaf-style text focused? first? by-other?) {:color :white}))

(defn ring [x] (hsl (* x 360) 70 70))

(def style-expr star-trail/style-expr)

(def style-leaf star-trail/style-leaf)
