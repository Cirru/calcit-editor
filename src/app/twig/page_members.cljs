
(ns app.twig.page-members
  (:require [recollect.twig :refer [deftwig]] [app.twig.member :refer [twig-member]]))

(deftwig
 twig-page-members
 (sessions users)
 (->> sessions
      (map
       (fn [entry]
         (let [[k session] entry] [k (twig-member session (get users (:user-id session)))])))
      (into {})))
