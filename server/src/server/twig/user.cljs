
(ns server.twig.user (:require [recollect.macros :refer [deftwig]]))

(deftwig twig-user (user) (-> user (dissoc :password) (update :theme #(or % :star-trail))))
