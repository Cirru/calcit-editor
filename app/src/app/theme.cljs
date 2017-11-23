
(ns app.theme (:require [app.theme.star-trail :as star-trail]))

(defn decide-leaf-theme [text focused? first? by-other? theme]
  (case theme :star-trail (star-trail/decide-leaf-style text focused? first? by-other?) {}))

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
    {}))
