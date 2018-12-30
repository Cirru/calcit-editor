
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
            [recollect.twig :refer [render-twig]]
            [recollect.diff :refer [diff-twig]]
            [app.twig.container :refer [twig-container]]
            [app.util.env :refer [check-version!]]
            [app.config :as config]
            [cumulo-util.file :refer [write-mildly! merge-local-edn!]]
            [app.util.env :refer [get-cli-configs!]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defonce *calcit-md5 (atom nil))

(defonce *client-caches (atom {}))

(def storage-file (path/join (.. js/process -env -PWD) (:storage-file config/site)))

(defonce initial-db
  (merge-local-edn!
   schema/database
   storage-file
   (fn [found?]
     (if found?
       (println "Loading calcit.edn")
       (println (.yellow chalk "Using default schema."))))))

(defonce *writer-db
  (atom
   (-> initial-db
       (assoc :repl {:alive? false, :logs {}})
       (assoc :saved-files (get-in initial-db [:ir :files])))))

(defonce *reader-db (atom @*writer-db))

(defn compile-all-files! [configs]
  (handle-files!
   (assoc @*writer-db :saved-files {})
   *calcit-md5
   configs
   (fn [op op-data] (println "After compile:" op op-data))
   false))

(defn dispatch! [op op-data sid]
  (comment .log js/console "Action" (str op) (clj->js op-data) sid)
  (comment .log js/console "Database:" (clj->js @*writer-db))
  (let [d2! (fn [op2 op-data2] (dispatch! op2 op-data2 sid))
        op-id (.generate shortid)
        op-time (.valueOf (js/Date.))]
    (try
     (case op
       :effect/save-files
         (handle-files! @*writer-db *calcit-md5 (:configs initial-db) d2! true)
       :effect/connect-repl (repl/connect-socket-repl! op-data d2!)
       :effect/cljs-repl (repl/try-cljs-repl! d2! op-data)
       :effect/send-code (repl/send-raw-code! op-data d2!)
       :effect/eval-tree (repl/eval-tree! @*writer-db d2! sid)
       :effect/end-repl (repl/end-repl! d2!)
       (reset! *writer-db (updater @*writer-db op op-data sid op-id op-time)))
     (catch js/Error e (println (.red chalk e)) (.error js/console e)))))

(defn on-file-change! []
  (let [file-content (fs/readFileSync storage-file "utf8"), new-md5 (md5 file-content)]
    (if (not= new-md5 @*calcit-md5)
      (let [calcit (read-string file-content)]
        (println (.blue chalk "calcit storage file changed!"))
        (reset! *calcit-md5 new-md5)
        (dispatch! :watcher/file-change calcit nil)))))

(defn sync-clients! [db]
  (wss-each!
   (fn [sid socket]
     (let [session (get-in db [:sessions sid])
           old-store (or (get @*client-caches sid) nil)
           new-store (render-twig (twig-container db session) old-store)
           changes (diff-twig old-store new-store {:key :id})]
       (println "Changes for" sid ":" changes)
       (if (not= changes [])
         (do
          (wss-send! sid {:kind :patch, :data changes})
          (swap! *client-caches assoc sid new-store)))))))

(defn render-loop! []
  (if (not= @*reader-db @*writer-db)
    (do
     (reset! *reader-db @*writer-db)
     (comment println "render loop")
     (sync-clients! @*reader-db)))
  (js/setTimeout render-loop! 20))

(defn run-server! [dispatch! port]
  (pick-port!
   port
   (fn [unoccupied-port]
     (wss-serve!
      unoccupied-port
      {:on-open (fn [sid socket]
         (dispatch! :session/connect nil sid)
         (println (.gray chalk (str "client connected: " sid)))),
       :on-data (fn [sid action]
         (case (:kind action)
           :op (dispatch! (:op action) (:data action) sid)
           (println "unknown data" action))),
       :on-close (fn [sid event]
         (println (.gray chalk (str "client disconnected: " sid)))
         (dispatch! :session/disconnect nil sid)),
       :on-error (fn [error] (.error js/console error))}))))

(defn serve-app! [port]
  (let [app (express), dir (path/join js/__dirname ""), file-port (+ 100 port)]
    (.use app "/" (express/static dir) (serve-index dir (clj->js {:icons true})))
    (.listen app file-port)
    (println
     (str "Serving local editor at " (.blue chalk (str "http://localhost:" file-port))))))

(defn watch-file! []
  (if (fs/existsSync storage-file)
    (do
     (reset! *calcit-md5 (md5 (fs/readFileSync storage-file "utf8")))
     (gaze
      storage-file
      (fn [error watcher]
        (if (some? error)
          (.log js/console error)
          (.on ^js watcher "changed" (fn [filepath] (on-file-change!)))))))))

(defn start-server! [configs]
  (run-server! #(dispatch! %1 %2 %3) (:port config/site))
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
  (let [configs (:config initial-db), cli-configs (get-cli-configs!)]
    (if (:compile? cli-configs)
      (compile-all-files! configs)
      (do
       (start-server! configs)
       (when (:local-ui? cli-configs) (serve-app! (:port configs)))
       (check-version!)))))

(defn reload! [] (println (.gray chalk "code updated.")) (sync-clients! @*reader-db))
