
(ns server.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [cirru-sepal.analyze :refer [write-file]]
            [server.util :refer [ns->path file->cirru]]
            [server.schema :as schema]
            ["chalk" :as chalk]
            ["path" :as path]
            ["fs" :as fs]
            ["child_process" :as cp]))

(defn modify-file! [file-path file output-dir]
  (let [project-path (path/join output-dir file-path)]
    (fs/writeFileSync project-path (write-file (file->cirru file)))
    (println (.gray chalk (str "modified " project-path)))))

(defn now! [] (.valueOf (js/Date.)))

(defn persist! [storage-path db]
  (let [start-time (now!)]
    (fs/writeFileSync
     storage-path
     (pr-str (-> db (assoc :sessions {}) (assoc :saved-files {}))))
    (comment
     println
     (.gray chalk (str "took " (- (now!) start-time) "ms to wrote coir.edn")))))

(defn create-file! [file-path file output-dir]
  (let [project-path (path/join output-dir file-path)]
    (cp/execSync (str "mkdir -p " (path/dirname project-path)))
    (fs/writeFileSync project-path (write-file (file->cirru file)))
    (println (.gray chalk (str "created " project-path)))))

(defn remove-file! [file-path output-dir]
  (let [project-path (path/join output-dir file-path)]
    (cp/execSync (str "rm -rfv " project-path))
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
                               (not= (get new-files ns-text) (get old-files ns-text)))))
         extension (:extension configs)
         output-dir (:output configs)]
     (doseq [ns-text added-names]
       (create-file! (ns->path ns-text extension) (get new-files ns-text) output-dir))
     (doseq [ns-text removed-names] (remove-file! (ns->path ns-text extension) output-dir))
     (doseq [ns-text changed-names]
       (modify-file! (ns->path ns-text extension) (get new-files ns-text) output-dir))
     (dispatch! :writer/save-files nil)
     (if save-ir? (do (js/setTimeout (fn [] (persist! (:storage-key configs) db))))))
   (catch
    js/Error
    e
    (do (println (.red chalk e)) (dispatch! :notify/push-error (.-message e))))))

(def path (js/require "path"))
