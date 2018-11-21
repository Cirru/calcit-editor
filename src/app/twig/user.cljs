
(ns app.twig.user (:require [recollect.twig :refer [deftwig]]))

(deftwig twig-user (user) (-> user (dissoc :password)))
