
(ns server.twig.user (:require [recollect.macros :refer [deftwig]]))

(deftwig twig-user (user) (dissoc user :password))
