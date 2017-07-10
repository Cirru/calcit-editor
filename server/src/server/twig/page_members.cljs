
(ns server.twig.page-members
  (:require [recollect.bunch :refer [create-twig]] [server.twig.member :refer [twig-member]]))

(def twig-page-members
  (create-twig
   :page-members
   (fn [sessions users]
     (->> sessions
          (map
           (fn [entry]
             (let [[k session] entry]
               [k (twig-member session (get users (:user-id session)))])))
          (into {})))))
