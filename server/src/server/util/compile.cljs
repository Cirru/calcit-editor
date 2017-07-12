
(ns server.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [stack-server.analyze :refer [generate-file]]
            [server.util :refer [ns->path file->cirru]]
            [server.schema :as schema]))

(def path (js/require "path"))

(def fs (js/require "fs"))

(defn modify-file! [file-path file configs]
  (let [project-path (path.join (:output configs) file-path)]
    (println "Modifying" project-path)
    (fs.writeFileSync project-path (generate-file (file->cirru file)))))

(def cp (js/require "child_process"))

(defn persist! [db]
  (let [fs (js/require "fs")]
    (fs.writeFileSync
     (:storage-key schema/configs)
     (pr-str (-> db (assoc :sessions {}) (assoc :saved-files {}))))))

(defn create-file! [file-path file configs]
  (let [project-path (path.join (:output configs) file-path)]
    (println "Creating" project-path)
    (cp.execSync (str "mkdir -p " (path.dirname project-path)))
    (fs.writeFileSync project-path (generate-file (file->cirru file)))))

(defn remove-file! [file-path configs]
  (let [project-path (path.join (:output configs) file-path)]
    (println "Removing" project-path)
    (cp.execSync (str "rm -rfv " project-path))))

(defn handle-files! [db configs dispatch!]
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
     (persist! db))
   (catch js/Error e (do (.log js/console e) (dispatch! :notify/push-error (.-message e))))))
