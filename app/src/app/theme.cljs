
(ns app.theme
  (:require [app.theme.star-trail :as star-trail]
            [app.theme.rainbow :as rainbow]
            [app.theme.curves :as curves]))

(defn base-style-expr [theme]
  (case theme
    :star-trail star-trail/style-expr
    :rainbow rainbow/style-expr
    :curves curves/style-expr
    {}))

(defn base-style-leaf [theme]
  (case theme
    :star-trail star-trail/style-leaf
    :rainbow rainbow/style-leaf
    :curves curves/style-leaf
    {}))

(defn decide-expr-theme [expr
                         has-others?
                         focused?
                         tail?
                         after-expr?
                         beginner?
                         length
                         depth
                         theme]
  (case theme
    :star-trail
      (star-trail/decide-expr-style
       expr
       has-others?
       focused?
       tail?
       after-expr?
       beginner?
       length
       depth)
    :rainbow
      (rainbow/decide-expr-style
       expr
       has-others?
       focused?
       tail?
       after-expr?
       beginner?
       length
       depth)
    :curves
      (curves/decide-expr-style
       expr
       has-others?
       focused?
       tail?
       after-expr?
       beginner?
       length
       depth)
    {}))

(defn decide-leaf-theme [text focused? first? by-other? theme]
  (case theme
    :star-trail (star-trail/decide-leaf-style text focused? first? by-other?)
    :rainbow (rainbow/decide-leaf-style text focused? first? by-other?)
    :curves (curves/decide-leaf-style text focused? first? by-other?)
    {}))
