
(ns app.theme.rainbow
  (:require [app.theme.star-trail :as star-trail] [hsl.core :refer [hsl]]))

(defn ring [x] (hsl (* x 360) 70 70))

(defn decide-expr-style [expr has-others? focused? tail? after-expr? beginner? length depth]
  (merge
   {:background-color (case (mod depth 7)
      0 (ring 0)
      1 (ring (/ 1 7))
      2 (ring (/ 2 7))
      3 (ring (/ 3 7))
      4 (ring (/ 4 7))
      5 (ring (/ 5 7))
      6 (ring (/ 6 7))
      :black)}
   (if focused? {:border-color (hsl 0 0 100)})))

(defn decide-leaf-style [text focused? first? by-other?]
  (merge (star-trail/decide-leaf-style text focused? first? by-other?) {:color :white}))

(def style-expr star-trail/style-expr)

(def style-leaf star-trail/style-leaf)
