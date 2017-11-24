
(ns server.twig.peek-def
  (:require [recollect.macros :refer [deftwig]] [server.util :refer [tree->cirru]]))

(deftwig twig-peek-def (tree) (tree->cirru tree))
