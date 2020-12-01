
(ns app.util.compile
  (:require [clojure.set :refer [difference intersection]]
            [cirru-sepal.analyze :refer [write-file]]
            [app.util
             :refer
             [ns->path file->cirru db->string tree->cirru now! hide-empty-fields]]
            ["chalk" :as chalk]
            ["path" :as path]
            ["fs" :as fs]
            ["child_process" :as cp]
            ["md5" :as md5]
            [app.config :as config]
            [cumulo-util.core :refer [unix-time!]]
            [applied-science.js-interop :as j]
            [cirru-edn.core :as cirru-edn]))

(defn create-file! [file-path file output-dir]
  (let [project-path (path/join output-dir file-path)]
    (cp/execSync (str "mkdir -p " (path/dirname project-path)))
    (fs/writeFileSync project-path (write-file (file->cirru file)))
    (println (.gray chalk (str "created " project-path)))))

(defn handle-compact-files! [pkg
                             old-files
                             new-files
                             added-names
                             removed-names
                             changed-names
                             configs]
  (let [compact-data {:package pkg,
                      :configs {:init-fn (:init-fn configs),
                                :reload-fn (:reload-fn configs),
                                :modules (:modules configs),
                                :version (:version configs)},
                      :files (->> new-files
                                  (map (fn [[ns-text file]] [ns-text (file->cirru file)]))
                                  (into {}))}
        inc-data (hide-empty-fields
                  {:removed removed-names,
                   :added (->> added-names
                               (map
                                (fn [ns-text]
                                  [ns-text (file->cirru (get new-files ns-text))]))
                               (into {})),
                   :changed (->> changed-names
                                 (map
                                  (fn [ns-text]
                                    [ns-text
                                     (let [old-file (get old-files ns-text)
                                           new-file (get new-files ns-text)
                                           old-defs (:defs old-file)
                                           new-defs (:defs new-file)
                                           old-def-names (set (keys old-defs))
                                           new-def-names (set (keys new-defs))
                                           added-defs (difference
                                                       new-def-names
                                                       old-def-names)
                                           removed-defs (difference
                                                         old-def-names
                                                         new-def-names)
                                           changed-defs (->> (intersection
                                                              old-def-names
                                                              new-def-names)
                                                             (filter
                                                              (fn [x]
                                                                (not=
                                                                 (get old-defs x)
                                                                 (get new-defs x)))))]
                                       (hide-empty-fields
                                        {:ns (if (= (:ns old-file) (:ns new-file))
                                           nil
                                           (tree->cirru (:ns new-file))),
                                         :proc (if (= (:proc old-file) (:proc new-file))
                                           nil
                                           (tree->cirru (:proc new-file))),
                                         :removed-defs removed-defs,
                                         :added-defs (->> added-defs
                                                          (map
                                                           (fn [x]
                                                             [x
                                                              (tree->cirru (get new-defs x))]))
                                                          (hide-empty-fields)),
                                         :changed-defs (->> changed-defs
                                                            (map
                                                             (fn [x]
                                                               [x
                                                                (tree->cirru
                                                                 (get new-defs x))]))
                                                            (hide-empty-fields))}))]))
                                 (into {}))})]
    (fs/writeFile
     "compact.cirru"
     (cirru-edn/write compact-data)
     (fn [err] (if (some? err) (js/console.log "Failed to write!" err))))
    (fs/writeFile
     ".compact-inc.cirru"
     (cirru-edn/write inc-data)
     (fn [err] (if (some? err) (js/console.log "Failed to write!" err))))))

(defn modify-file! [file-path file output-dir]
  (let [project-path (path/join output-dir file-path)]
    (fs/writeFileSync project-path (write-file (file->cirru file)))
    (println (.gray chalk (str "modified " project-path)))))

(defn remove-file! [file-path output-dir]
  (let [project-path (path/join output-dir file-path)]
    (cp/execSync (str "rm -rfv " project-path))
    (println (.red chalk (str "removed " project-path)))))

(defn handle-file-writing! [old-files
                            new-files
                            added-names
                            removed-names
                            changed-names
                            get-ext
                            output-dir]
  (doseq [ns-text added-names]
    (let [file (get new-files ns-text)]
      (create-file! (ns->path ns-text (get-ext file)) file output-dir)))
  (doseq [ns-text removed-names]
    (let [file (get old-files ns-text)]
      (remove-file! (ns->path ns-text (get-ext file)) output-dir)))
  (doseq [ns-text changed-names]
    (let [file (get new-files ns-text), old-file (get old-files ns-text)]
      (if (= (-> file :configs :extension) (-> old-file :configs :extension))
        (modify-file! (ns->path ns-text (get-ext file)) file output-dir)
        (do
         (remove-file! (ns->path ns-text (get-ext old-file)) output-dir)
         (create-file! (ns->path ns-text (get-ext file)) file output-dir))))))

(defn persist-async! [storage-path db-str started-time]
  (fs/writeFile
   storage-path
   db-str
   (fn [err]
     (if (some? err)
       (js/console.log (chalk/red "Failed to write storage!" err))
       (println
        (chalk/gray (str "took " (- (unix-time!) started-time) "ms to wrote calcit.cirru")))))))

(defn handle-files! [db *calcit-md5 configs dispatch! save-ir? filter-ns]
  (try
   (let [new-files (get-in db [:ir :files])
         old-files (get db :saved-files)
         new-names (set (keys new-files))
         old-names (set (keys old-files))
         filter-by-ns (fn [xs]
                        (if (some? filter-ns)
                          (if (contains? xs filter-ns) (list filter-ns) nil)
                          xs))
         added-names (filter-by-ns (difference new-names old-names))
         removed-names (filter-by-ns (difference old-names new-names))
         changed-names (->> (intersection new-names old-names)
                            (filter
                             (fn [ns-text]
                               (not= (get new-files ns-text) (get old-files ns-text))))
                            (set)
                            (filter-by-ns))
         extension (:extension configs)
         output-dir (:output configs)
         get-ext (fn [file]
                   (let [local-ext (-> file :configs :extension)]
                     (if (some? local-ext) (str "." (name local-ext)) extension)))]
     (if (:compact-output? configs)
       (handle-compact-files!
        (get-in db [:ir :package])
        old-files
        new-files
        added-names
        removed-names
        changed-names
        (:configs db))
       (handle-file-writing!
        old-files
        new-files
        added-names
        removed-names
        changed-names
        get-ext
        output-dir))
     (dispatch! :writer/save-files filter-ns)
     (if save-ir?
       (js/setTimeout
        (fn []
          (let [db-content (db->string db), started-time (unix-time!)]
            (reset! *calcit-md5 (md5 db-content))
            (persist-async! (:storage-file config/site) db-content started-time))))))
   (catch
    js/Error
    e
    (do
     (println (chalk/red e))
     (js/console.error e)
     (dispatch! :notify/push-message [:error (j/get e :message)])))))

(def path (js/require "path"))

(defn persist! [storage-path db-str started-time]
  (fs/writeFileSync storage-path db-str)
  (println
   (.gray chalk (str "took " (- (unix-time!) started-time) "ms to wrote calcit.cirru"))))
