
(ns server.main
  (:require [server.schema :as schema]
            [server.network :refer [run-server! sync-clients!]]
            [server.updater.core :refer [updater]]
            [cljs.core.async :refer [<! >!]]
            [cljs.reader :refer [read-string]]
            [server.util.compile :refer [handle-files! persist!]]
            [server.util.env :refer [pick-configs]]
            [server.util :refer [db->string]]
            ["chalk" :as chalk]
            ["path" :as path]
            ["express" :as express]
            ["serve-index" :as serve-index]
            ["shortid" :as shortid]
            ["fs" :as fs]
            ["md5" :as md5]
            ["gaze" :as gaze]))

(defonce *coir-md5 (atom nil))

(defonce *writer-db
  (atom
   (let [filepath (:storage-key schema/configs)
         db (if (fs/existsSync filepath)
              (read-string (fs/readFileSync filepath "utf8"))
              (do (println (.yellow chalk "Using default schema.")) schema/database))]
     (-> db
         (assoc :saved-files (get-in db [:ir :files]))
         (update :configs (fn [configs] (or configs schema/configs)))))))

(def global-configs (pick-configs (:configs @*writer-db)))

(defn dispatch! [op op-data sid]
  (comment .log js/console "Action" (str op) (clj->js op-data) sid)
  (comment .log js/console "Database:" (clj->js @*writer-db))
  (cond
    (= op :effect/save-files)
      (handle-files! @*writer-db *coir-md5 global-configs #(dispatch! %1 %2 sid) true)
    :else
      (try
       (let [new-db (updater
                     @*writer-db
                     op
                     op-data
                     sid
                     (.generate shortid)
                     (.valueOf (js/Date.)))]
         (reset! *writer-db new-db))
       (catch js/Error e (println (.red chalk e))))))

(defonce *reader-db (atom @*writer-db))

(defn on-file-change! []
  (let [coir-path (:storage-key global-configs)
        file-content (fs/readFileSync coir-path "utf8")
        new-md5 (md5 file-content)]
    (if (not= new-md5 @*coir-md5)
      (let [coir (read-string file-content)]
        (println (.yellow chalk "coir changed, check file changes!"))
        (reset! *coir-md5 new-md5)
        (dispatch! :watcher/file-change coir nil)))))

(defn watch-file! []
  (let [coir-path (:storage-key global-configs)]
    (reset! *coir-md5 (md5 (fs/readFileSync coir-path "utf8")))
    (gaze
     coir-path
     (fn [error watcher]
       (if (some? error)
         (.log js/console error)
         (.on watcher "changed" (fn [filepath] (on-file-change!))))))))

(defn render-loop! []
  (if (not= @*reader-db @*writer-db)
    (do
     (reset! *reader-db @*writer-db)
     (comment println "render loop")
     (sync-clients! @*reader-db)))
  (js/setTimeout render-loop! 20))

(defn start-server! [configs]
  (run-server! #(dispatch! %1 %2 %3) (:port configs))
  (render-loop!)
  (watch-file!)
  (.on
   js/process
   "SIGINT"
   (fn [code]
     (persist! (:storage-key configs) (db->string @*writer-db))
     (println (str "\n" "Saved coir.edn") (str (if (some? code) (str "with " code))))
     (.exit js/process))))

(defn serve-app! [port]
  (let [app (express), dir (path/join js/__dirname "app"), file-port (+ 100 port)]
    (.use app "/" (.static express dir) (serve-index dir (clj->js {:icons true})))
    (.listen app file-port)
    (println
     (str "Serving local editor at " (.blue chalk (str "http://localhost:" file-port))))))

(defn compile-all-files! [configs]
  (handle-files!
   (assoc @*writer-db :saved-files {})
   *coir-md5
   configs
   (fn [op op-data] (println "After compile:" op op-data))
   false))

(defn main! []
  (let [configs global-configs, op (get configs :op)]
    (if (= op "compile")
      (compile-all-files! configs)
      (do (start-server! configs) (serve-app! (:port configs))))))

(defn reload! [] (println (.gray chalk "code updated.")) (sync-clients! @*reader-db))
