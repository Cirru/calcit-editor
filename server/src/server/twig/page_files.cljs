
(ns server.twig.page-files (:require [recollect.bunch :refer [create-twig]]))

(def twig-page-files
  (create-twig
   :page-files
   (fn [files selected-ns]
     {:ns-set (into #{} (keys files)),
      :defs-set (if (some? selected-ns)
        (do
         (println (get-in files [selected-ns :defs]))
         (->> (get-in files [selected-ns :defs]) (keys) (into #{})))
        #{})})))
