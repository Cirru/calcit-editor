
(ns server.twig.page-editor (:require [recollect.bunch :refer [create-twig]]))

(def twig-page-editor
  (create-twig
   :page-editor
   (fn [files writer]
     (let [pointer (:pointer writer), stack (:stack writer), bookmark (get stack pointer)]
       (if (some? bookmark)
         (let [ns-text (:ns bookmark)]
           {:focus (:focus bookmark),
            :expr (case (:kind bookmark)
              :ns (get-in files [ns-text :ns])
              :procs (get-in files [ns-text :procs])
              :def (get-in files [ns-text :defs (:extra bookmark)])
              nil)})
         nil)))))
