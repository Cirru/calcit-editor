
(ns server.twig.page-members
  (:require [recollect.macros :refer [deftwig]] [server.twig.member :refer [twig-member]]))

(deftwig
 twig-page-members
 (sessions users)
 (->> sessions
      (map
       (fn [entry]
         (let [[k session] entry] [k (twig-member session (get users (:user-id session)))])))
      (into {})))
