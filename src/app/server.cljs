
(ns app.server
  (:require [app.schema :as schema]
            [app.updater :refer [updater]]
            [cljs.core.async :refer [<! >!]]
            [cljs.reader :refer [read-string]]
            [app.util.compile :refer [handle-files! persist!]]
            [app.util.env :refer [pick-port!]]
            [app.util :refer [db->string]]
            [app.repl :as repl]
            ["chalk" :as chalk]
            ["path" :as path]
            ["express" :as express]
            ["serve-index" :as serve-index]
            ["shortid" :as shortid]
            ["fs" :as fs]
            ["md5" :as md5]
            ["gaze" :as gaze]
            [ws-edn.server :refer [wss-serve! wss-send! wss-each!]]
            ["shortid" :as shortid]
            [recollect.twig :refer [clear-twig-caches! new-twig-loop!]]
            [recollect.diff :refer [diff-twig]]
            [app.twig.container :refer [twig-container]]
            [app.util.env :refer [check-version!]]
            [app.config :as config]
            [cumulo-util.file :refer [write-mildly!]]
            [cumulo-util.core :refer [unix-time! id! delay!]]
            [app.util.env :refer [get-cli-configs!]]
            [cirru-edn.core :as cirru-edn])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defonce *calcit-md5 (atom nil))

(defonce *client-caches (atom {}))

(def storage-file (path/join (.. js/process -env -PWD) (:storage-file config/site)))

(defonce initial-db
  (merge
   schema/database
   (let [found? (fs/existsSync storage-file), configs (:configs schema/database)]
     (if found?
       (println (.gray chalk "Loading calcit.cirru"))
       (println (.yellow chalk "Using default schema.")))
     (if found?
       (let [started-at (unix-time!)
             data (cirru-edn/parse (fs/readFileSync storage-file "utf8"))
             cost (- (unix-time!) started-at)]
         (println (chalk/gray (str "Took " cost "ms to load.")))
         data)
       (if (some? configs)
         {:configs (assoc configs :compact-output? (:compact? (get-cli-configs!)))})))))

(defonce *writer-db
  (atom
   (-> initial-db
       (assoc :repl {:alive? false, :logs {}})
       (assoc :saved-files (get-in initial-db [:ir :files]))
       (assoc :sessions {}))))

(defonce *reader-db (atom @*writer-db))

(defn compile-all-files! [configs]
  (handle-files!
   (assoc @*writer-db :saved-files {})
   *calcit-md5
   configs
   (fn [op op-data] (println "After compile:" op op-data))
   false
   nil))

(defn dispatch! [op op-data sid]
  (when config/dev? (js/console.log "Action" (str op) (clj->js op-data) sid))
  (comment js/console.log "Database:" (clj->js @*writer-db))
  (let [d2! (fn [op2 op-data2] (dispatch! op2 op-data2 sid))
        op-id (id!)
        op-time (unix-time!)]
    (try
     (case op
       :effect/save-files
         (handle-files! @*writer-db *calcit-md5 (:configs initial-db) d2! true nil)
       :effect/save-ns
         (handle-files! @*writer-db *calcit-md5 (:configs initial-db) d2! true op-data)
       :effect/connect-repl (repl/load-nrepl! d2!)
       :effect/cljs-repl (repl/try-cljs-repl! d2! op-data)
       :effect/send-code (repl/send-raw-code! op-data d2!)
       :effect/eval-tree (repl/eval-tree! @*writer-db d2! sid)
       :effect/end-repl (repl/end-repl! d2!)
       (reset! *writer-db (updater @*writer-db op op-data sid op-id op-time)))
     (catch js/Error e (println (.red chalk e)) (.error js/console e)))))

(defn on-file-change! []
  (let [file-content (fs/readFileSync storage-file "utf8"), new-md5 (md5 file-content)]
    (if (not= new-md5 @*calcit-md5)
      (let [calcit (cirru-edn/parse file-content)]
        (println (.blue chalk "calcit storage file changed!"))
        (reset! *calcit-md5 new-md5)
        (dispatch! :watcher/file-change calcit nil)))))

(defn sync-clients! [db]
  (wss-each!
   (fn [sid socket]
     (let [session (get-in db [:sessions sid])
           old-store (or (get @*client-caches sid) nil)
           new-store (twig-container db session)
           changes (diff-twig old-store new-store {:key :id})]
       (when config/dev? (println "Changes for" sid ":" (count changes)))
       (if (not= changes [])
         (do
          (wss-send! sid {:kind :patch, :data changes})
          (swap! *client-caches assoc sid new-store))))))
  (new-twig-loop!))

(defn render-loop! []
  (if (not= @*reader-db @*writer-db)
    (do
     (reset! *reader-db @*writer-db)
     (comment println "render loop")
     (sync-clients! @*reader-db)))
  (js/setTimeout render-loop! 20))

(defn run-server! [dispatch! port]
  (wss-serve!
   port
   {:on-open (fn [sid socket]
      (dispatch! :session/connect nil sid)
      (println (chalk/gray (<< "client connected: ~{sid}")))),
    :on-data (fn [sid action]
      (case (:kind action)
        :op (dispatch! (:op action) (:data action) sid)
        :ping (do)
        (println "unknown data" action))),
    :on-close (fn [sid event]
      (println (chalk/gray (<< "client disconnected: ~{sid}")))
      (dispatch! :session/disconnect nil sid)),
    :on-error (fn [error] (js/console.error error))}))

(defn serve-app! [port]
  (let [app (express), dir (path/join js/__dirname ""), file-port (+ 100 port)]
    (.use app "/" (express/static dir) (serve-index dir (clj->js {:icons true})))
    (.listen app file-port)
    (println
     (str "Serving local editor at " (chalk/blue (str "http://localhost:" file-port))))))

(defn watch-file! []
  (if (fs/existsSync storage-file)
    (do
     (reset! *calcit-md5 (md5 (fs/readFileSync storage-file "utf8")))
     (gaze
      storage-file
      (fn [error watcher]
        (if (some? error)
          (.log js/console error)
          (.on ^js watcher "changed" (fn [filepath] (delay! 0.02 on-file-change!)))))))))

(defn start-server! [configs]
  (pick-port!
   (:port configs)
   (fn [unoccupied-port] (run-server! #(dispatch! %1 %2 %3) unoccupied-port)))
  (render-loop!)
  (watch-file!)
  (.on
   js/process
   "SIGINT"
   (fn [code]
     (if (empty? (get-in @*writer-db [:ir :files]))
       (println "Not writing empty project.")
       (do
        (let [started-time (unix-time!)]
          (persist! storage-file (db->string @*writer-db) started-time))
        (println (str "\n" "Saved calcit.cirru") (str (if (some? code) (str "with " code))))))
     (.exit js/process))))

(defn main! []
  (let [configs (:configs initial-db), cli-configs (get-cli-configs!)]
    (if (:compile? cli-configs)
      (compile-all-files! configs)
      (do
       (start-server! configs)
       (when (:local-ui? cli-configs) (serve-app! (:port configs)))
       (check-version!)))))

(defn reload! []
  (println (.gray chalk "code updated."))
  (clear-twig-caches!)
  (sync-clients! @*reader-db))
