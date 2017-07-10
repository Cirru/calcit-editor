
(ns server.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [stack-server.analyze :refer [generate-file]]
            [server.util :refer [ns->path file->cirru]]
            [server.schema :as schema]))

(defn emit-file! [] )

(def fs (js/require "fs"))

(def path (js/require "path"))

(def cp (js/require "child_process"))

(defn create-file! [file-path file]
  (let [project-path (path.join (:output schema/configs) file-path)]
    (println "Creating" project-path)
    (cp.execSync (str "mkdir -p " (path.dirname project-path)))
    (fs.writeFileSync project-path (generate-file (file->cirru file)))))

(defn remove-file! [file-path]
  (let [project-path (path.join (:output schema/configs) file-path)]
    (println "Removing" project-path)
    (cp.execSync (str "rm -rfv " project-path))))

(defn modify-file! [file-path file]
  (let [project-path (path.join (:output schema/configs) file-path)]
    (println "Modifying" project-path)
    (fs.writeFileSync project-path (generate-file (file->cirru file)))))

(defn handle-files! [db dispatch!]
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
     (doseq [ns-text added-names] (create-file! (ns->path ns-text) (get new-files ns-text)))
     (doseq [ns-text removed-names] (remove-file! (ns->path ns-text)))
     (doseq [ns-text changed-names]
       (modify-file! (ns->path ns-text) (get new-files ns-text)))
     (dispatch! :writer/save-files nil))
   (catch js/Error e (do (.log js/console e) (dispatch! :notify/push-error (.-message e))))))
