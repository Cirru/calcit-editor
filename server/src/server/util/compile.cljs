
(ns server.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [stack-server.analyze :refer [generate-file]]
            [server.util :refer [ns->path file->cirru]]
            [fipp.edn :as fipp]
            [server.schema :as schema]
            ["chalk" :as chalk]))

(def path (js/require "path"))

(def fs (js/require "fs"))

(defn modify-file! [file-path file configs]
  (let [project-path (path.join (:output configs) file-path)]
    (fs.writeFileSync project-path (generate-file (file->cirru file)))
    (println (.gray chalk (str "modified" project-path)))))

(def cp (js/require "child_process"))

(defn now! [] (.valueOf (js/Date.)))

(defn persist! [db]
  (let [start-time (now!)]
    (fs.writeFileSync
     (:storage-key schema/configs)
     (pr-str (-> db (assoc :sessions {}) (assoc :saved-files {}))))
    (comment
     println
     (.gray chalk (str "took " (- (now!) start-time) "ms to wrote coir.edn")))))

(defn create-file! [file-path file configs]
  (let [project-path (path.join (:output configs) file-path)]
    (cp.execSync (str "mkdir -p " (path.dirname project-path)))
    (fs.writeFileSync project-path (generate-file (file->cirru file)))
    (println (.gray chalk (str "created " project-path)))))

(defn remove-file! [file-path configs]
  (let [project-path (path.join (:output configs) file-path)]
    (cp.execSync (str "rm -rfv " project-path))
    (println (.red chalk (str "removed " project-path)))))

(defn handle-files! [db configs dispatch! save-ir?]
  (try
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
     (doseq [ns-text added-names]
       (create-file! (ns->path ns-text configs) (get new-files ns-text) configs))
     (doseq [ns-text removed-names] (remove-file! (ns->path ns-text configs) configs))
     (doseq [ns-text changed-names]
       (modify-file! (ns->path ns-text configs) (get new-files ns-text) configs))
     (dispatch! :writer/save-files nil)
     (if save-ir? (do (js/setTimeout (fn [] (persist! db))))))
   (catch
    js/Error
    e
    (do (println (.red chalk e)) (dispatch! :notify/push-error (.-message e))))))
