
(ns app.twig.member (:require [recollect.twig :refer [deftwig]]))

(deftwig
 twig-member
 (session user)
 {:user user,
  :nickname (:nickname session),
  :bookmark (let [writer (:writer session)] (get (:stack writer) (:pointer writer))),
  :page (get-in session [:router :name])})
