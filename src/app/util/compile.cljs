
(ns app.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [cirru-sepal.analyze :refer [write-file]]
            [app.util :refer [ns->path file->cirru db->string]]
            ["chalk" :as chalk]
            ["path" :as path]
            ["fs" :as fs]
            ["child_process" :as cp]
            ["md5" :as md5]
            [app.config :as config]
            [cumulo-util.core :refer [unix-time!]]
            [applied-science.js-interop :as j]
            [app.client-util :refer [now!]]))

(defn create-file! [file-path file output-dir]
  (let [project-path (path/join output-dir file-path)]
    (cp/execSync (str "mkdir -p " (path/dirname project-path)))
    (fs/writeFileSync project-path (write-file (file->cirru file)))
    (println (.gray chalk (str "created " project-path)))))

(defn modify-file! [file-path file output-dir]
  (let [project-path (path/join output-dir file-path)]
    (fs/writeFileSync project-path (write-file (file->cirru file)))
    (println (.gray chalk (str "modified " project-path)))))

(defn persist! [storage-path db-str started-time]
  (fs/writeFileSync storage-path db-str)
  (println
   (.gray chalk (str "took " (- (unix-time!) started-time) "ms to wrote calcit.cirru"))))

(defn remove-file! [file-path output-dir]
  (let [project-path (path/join output-dir file-path)]
    (cp/execSync (str "rm -rfv " project-path))
    (println (.red chalk (str "removed " project-path)))))

(defn handle-files! [db *calcit-md5 configs dispatch! save-ir?]
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
     (if save-ir?
       (js/setTimeout
        (fn []
          (let [db-content (db->string db), started-time (unix-time!)]
            (reset! *calcit-md5 (md5 db-content))
            (persist! (:storage-file config/site) db-content started-time))))))
   (catch
    js/Error
    e
    (do
     (println (chalk/red e))
     (js/console.error e)
     (dispatch! :notify/push-message [:error (j/get e :message)])))))

(def path (js/require "path"))
