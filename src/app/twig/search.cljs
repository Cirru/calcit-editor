
(ns app.twig.search (:require [recollect.twig :refer [deftwig]]))

(deftwig
 twig-search
 (files)
 (->> files
      (mapcat
       (fn [entry]
         (let [[k file] entry]
           (concat
            [{:kind :ns, :ns k} {:kind :proc, :ns k}]
            (map
             (fn [f-entry] (let [[f-k file] f-entry] {:kind :def, :ns k, :extra f-k}))
             (:defs file))))))
      (set)))
