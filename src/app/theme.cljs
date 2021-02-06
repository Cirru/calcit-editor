
(ns app.theme
  (:require [app.theme.star-trail :as star-trail]
            [app.theme.curves :as curves]
            [app.theme.beginner :as beginner]))

(defn base-style-expr [theme]
  (case theme
    :star-trail star-trail/style-expr
    :curves curves/style-expr
    :beginner beginner/style-expr
    {}))

(defn base-style-leaf [theme]
  (case theme
    :star-trail star-trail/style-leaf
    :curves curves/style-leaf
    :beginner beginner/style-leaf
    {}))

(defn decide-expr-theme [expr has-others? focused? tail? layout-mode length depth theme]
  (case theme
    :star-trail
      (star-trail/decide-expr-style expr has-others? focused? tail? layout-mode length depth)
    :curves
      (curves/decide-expr-style expr has-others? focused? tail? layout-mode length depth)
    :beginner
      (beginner/decide-expr-style expr has-others? focused? tail? layout-mode length depth)
    {}))

(defn decide-leaf-theme [text focused? first? by-other? theme]
  (case theme
    :star-trail (star-trail/decide-leaf-style text focused? first? by-other?)
    :curves (curves/decide-leaf-style text focused? first? by-other?)
    :beginner (beginner/decide-leaf-style text focused? first? by-other?)
    {}))
