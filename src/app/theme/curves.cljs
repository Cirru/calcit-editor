
(ns app.theme.curves
  (:require [app.theme.star-trail :as star-trail] [hsl.core :refer [hsl]]))

(defn decide-expr-style [expr has-others? focused? tail? layout-mode length depth]
  (merge
   {:border-radius "16px",
    :display :inline-block,
    :border-width "0 1px",
    :border-color (hsl 0 0 80 0.5),
    :padding "4px 8px"}
   (if focused? {:border-color (hsl 0 0 100 0.8)})))

(defn decide-leaf-style [text focused? first? by-other?]
  (merge (star-trail/decide-leaf-style text focused? first? by-other?) {}))

(def style-expr star-trail/style-expr)

(def style-leaf star-trail/style-leaf)
