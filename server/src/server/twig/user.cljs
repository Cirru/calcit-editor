
(ns server.twig.user (:require [recollect.bunch :refer [create-twig]]))

(def twig-user (create-twig :user (fn [user] (dissoc user :password))))
