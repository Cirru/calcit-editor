
(ns server.util.compile (:require [clojure.set :refer [difference intersection]]))

(defn emit-file! [] )

(defn handle-files! [db]
  (let [new-files (get-in db [:ir :files])
        old-files (get db :saved-files)
        new-names (set (keys new-files))
        old-names (set (keys old-files))
        added-names (difference new-names old-names)
        removed-names (difference old-names new-names)
        changed-names (->> (intersection new-names old-names)
                           (filter
                            (fn [ns-text]
                              (not= (get new-files ns-text) (get old-files ns-text)))))]
    (doseq [ns-text added-names] (println "add:" ns-text))
    (doseq [ns-text removed-names] (println "remove:" ns-text))
    (doseq [ns-text changed-names] (println "changed:" ns-text))))
