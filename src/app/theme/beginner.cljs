
(ns app.theme.beginner
  (:require [app.theme.star-trail :as star-trail] [hsl.core :refer [hsl]]))

(def style-expr-beginner {:outline (str "1px solid " (hsl 200 80 70 0.2))})

(defn decide-expr-style [expr has-others? focused? tail? layout-mode length depth]
  (merge
   (star-trail/decide-expr-style expr has-others? focused? tail? layout-mode length depth)
   style-expr-beginner))

(defn decide-leaf-style [text focused? first? by-other?]
  (merge (star-trail/decide-leaf-style text focused? first? by-other?)))

(def style-expr star-trail/style-expr)

(def style-leaf star-trail/style-leaf)
