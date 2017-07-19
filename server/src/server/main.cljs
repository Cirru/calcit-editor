
(ns server.main
  (:require [server.schema :as schema]
            [server.network :refer [run-server! render-clients!]]
            [server.updater.core :refer [updater]]
            [cljs.core.async :refer [<! >!]]
            [cljs.reader :refer [read-string]]
            [server.util.compile :refer [handle-files! persist!]]
            [server.util.env :refer [pick-configs]])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(defonce *writer-db
  (atom
   (let [fs (js/require "fs")
         filepath (:storage-key schema/configs)
         db (if (fs.existsSync filepath)
              (do
               (println "Loading existing database storagte...")
               (read-string (fs.readFileSync filepath "utf8")))
              (do (println "Using default schema.") schema/database))]
     (-> db
         (assoc :saved-files (get-in db [:ir :files]))
         (update :configs (fn [configs] (or configs schema/configs)))))))

(defonce *reader-db (atom @*writer-db))

(defn render-loop! []
  (if (not= @*reader-db @*writer-db)
    (do
     (reset! *reader-db @*writer-db)
     (comment println "render loop")
     (render-clients! @*reader-db)))
  (js/setTimeout render-loop! 20))

(defn start-server! [configs]
  (let [server-ch (run-server! {:port (:port configs)})]
    (go-loop
     []
     (let [[op op-data session-id op-id op-time] (<! server-ch)
           dispatch! (fn [op' op-data']
                       (go (>! server-ch [op' op-data' session-id op-id op-time])))]
       (comment .log js/console "Action" (str op) (clj->js op-data) session-id op-id op-time)
       (comment .log js/console "Database:" (clj->js @*writer-db))
       (try
        (do
         (cond
           (= op :effect/save-files) (handle-files! @*writer-db configs dispatch! true)
           :else
             (let [new-db (updater @*writer-db op op-data session-id op-id op-time)]
               (reset! *writer-db new-db))))
        (catch js/Error e (.log js/console e)))
       (recur)))
    (render-loop!))
  (add-watch *reader-db :log (fn [] ))
  (.on
   js/process
   "SIGINT"
   (fn [code]
     (println "Saving coir.edn with code" code)
     (persist! @*writer-db)
     (.exit js/process)))
  (println "Server started."))

(defn compile-all-files! [configs]
  (handle-files!
   (assoc @*writer-db :saved-files {})
   configs
   (fn [op op-data] (println "After compile:" op op-data))
   false))

(defn main! []
  (let [configs (pick-configs (:configs @*writer-db)), op (get configs :op)]
    (if (= op "compile") (compile-all-files! configs) (start-server! configs))))

(defn reload! [] (println "Code updated.") (render-clients! @*reader-db))
