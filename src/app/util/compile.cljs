
(ns app.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [cirru-sepal.analyze :refer [write-file]]
            [app.util :refer [ns->path file->cirru db->string now!]]
            ["chalk" :as chalk]
            ["path" :as path]
            ["fs" :as fs]
            ["child_process" :as cp]
            ["md5" :as md5]
            [app.config :as config]
            [cumulo-util.core :refer [unix-time!]]
            [applied-science.js-interop :as j]))

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

(defn handle-files! [db *calcit-md5 configs dispatch! save-ir? filter-ns]
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
                               (not= (get new-files ns-text) (get old-files ns-text))))
                            (set))
         extension (:extension configs)
         output-dir (:output configs)
         filter-by-ns (fn [xs]
                        (if (some? filter-ns)
                          (if (contains? xs filter-ns) (list filter-ns) nil)
                          xs))
         get-ext (fn [file]
                   (let [local-ext (-> file :configs :extension)]
                     (if (some? local-ext) (str "." (name local-ext)) extension)))]
     (doseq [ns-text (filter-by-ns added-names)]
       (let [file (get new-files ns-text)]
         (create-file! (ns->path ns-text (get-ext file)) file output-dir)))
     (doseq [ns-text (filter-by-ns removed-names)]
       (let [file (get old-files ns-text)]
         (remove-file! (ns->path ns-text (get-ext file)) output-dir)))
     (doseq [ns-text (filter-by-ns changed-names)]
       (let [file (get new-files ns-text), old-file (get old-files ns-text)]
         (if (= (-> file :configs :extension) (-> old-file :configs :extension))
           (modify-file! (ns->path ns-text (get-ext file)) file output-dir)
           (do
            (remove-file! (ns->path ns-text (get-ext old-file)) output-dir)
            (create-file! (ns->path ns-text (get-ext file)) file output-dir)))))
     (dispatch! :writer/save-files filter-ns)
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
