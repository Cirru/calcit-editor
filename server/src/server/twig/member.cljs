
(ns server.twig.member (:require [recollect.bunch :refer [create-twig]]))

(def twig-member
  (create-twig
   :member
   (fn [session user]
     {:user user,
      :nickname (:nickname session),
      :bookmark (let [writer (:writer session)] (get (:stack writer) (:pointer writer))),
      :page (get-in session [:router :name])})))
