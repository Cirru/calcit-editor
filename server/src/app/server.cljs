
(ns app.server
  (:require [app.schema :as schema]
            [app.service :refer [run-server! sync-clients!]]
            [app.updater :refer [updater]]
            [cljs.core.async :refer [<! >!]]
            [cljs.reader :refer [read-string]]
            [app.util.compile :refer [handle-files! persist!]]
            [app.util.env :refer [pick-configs]]
            [app.util :refer [db->string]]
            [app.repl :as repl]
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
         (assoc :repl {:alive? false, :logs {}})
         (assoc :saved-files (get-in db [:ir :files]))
         (update :configs (fn [configs] (or configs schema/configs)))))))

(defonce *reader-db (atom @*writer-db))

(defn compile-all-files! [configs]
  (handle-files!
   (assoc @*writer-db :saved-files {})
   *coir-md5
   configs
   (fn [op op-data] (println "After compile:" op op-data))
   false))

(def global-configs (pick-configs (:configs @*writer-db)))

(defn dispatch! [op op-data sid]
  (comment .log js/console "Action" (str op) (clj->js op-data) sid)
  (comment .log js/console "Database:" (clj->js @*writer-db))
  (let [d2! (fn [op2 op-data2] (dispatch! op2 op-data2 sid))
        op-id (.generate shortid)
        op-time (.valueOf (js/Date.))]
    (try
     (case op
       :effect/save-files (handle-files! @*writer-db *coir-md5 global-configs d2! true)
       :effect/connect-repl (repl/connect-socket-repl! op-data d2!)
       :effect/cljs-repl (repl/try-cljs-repl! d2! op-data)
       :effect/send-code (repl/send-raw-code! op-data d2!)
       :effect/eval-tree (repl/eval-tree! @*writer-db d2! sid)
       :effect/end-repl (repl/end-repl! d2!)
       (reset! *writer-db (updater @*writer-db op op-data sid op-id op-time)))
     (catch js/Error e (println (.red chalk e)) (.error js/console e)))))

(defn on-file-change! []
  (let [coir-path (:storage-key global-configs)
        file-content (fs/readFileSync coir-path "utf8")
        new-md5 (md5 file-content)]
    (if (not= new-md5 @*coir-md5)
      (let [coir (read-string file-content)]
        (println "coir changed")
        (reset! *coir-md5 new-md5)
        (dispatch! :watcher/file-change coir nil)))))

(defn render-loop! []
  (if (not= @*reader-db @*writer-db)
    (do
     (reset! *reader-db @*writer-db)
     (comment println "render loop")
     (sync-clients! @*reader-db)))
  (js/setTimeout render-loop! 20))

(defn serve-app! [port]
  (let [app (express), dir (path/join js/__dirname "app"), file-port (+ 100 port)]
    (.use app "/" (.static express dir) (serve-index dir (clj->js {:icons true})))
    (.listen app file-port)
    (println
     (str "Serving local editor at " (.blue chalk (str "http://localhost:" file-port))))))

(defn watch-file! []
  (let [coir-path (:storage-key global-configs)]
    (if (fs/existsSync coir-path)
      (do
       (reset! *coir-md5 (md5 (fs/readFileSync coir-path "utf8")))
       (gaze
        coir-path
        (fn [error watcher]
          (if (some? error)
            (.log js/console error)
            (.on watcher "changed" (fn [filepath] (on-file-change!))))))))))

(defn start-server! [configs]
  (run-server! #(dispatch! %1 %2 %3) (:port configs))
  (render-loop!)
  (watch-file!)
  (.on
   js/process
   "SIGINT"
   (fn [code]
     (persist! (:storage-key configs) (db->string @*writer-db))
     (println (str "\n" "Saved calcit.edn") (str (if (some? code) (str "with " code))))
     (.exit js/process))))

(defn main! []
  (let [configs global-configs, op (get configs :op)]
    (if (= op "compile")
      (compile-all-files! configs)
      (do
       (start-server! configs)
       (if (= "local" js/process.env.client) (serve-app! (:port configs)))))))

(defn reload! [] (println (.gray chalk "code updated.")) (sync-clients! @*reader-db))
