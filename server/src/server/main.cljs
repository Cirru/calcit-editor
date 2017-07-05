
(ns server.main
  (:require [server.schema :as schema]
            [server.network :refer [run-server! render-clients!]]
            [server.updater.core :refer [updater]]
            [cljs.core.async :refer [<!]]
            [cljs.reader :refer [read-string]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce *writer-db
  (atom
   (let [fs (js/require "fs"), filepath (:storage-key schema/configs)]
     (enable-console-print!)
     (if (fs.existsSync filepath)
       (do (println "Found storage.") (read-string (fs.readFileSync filepath "utf8")))
       (do (println "Found no storage.") schema/database)))))

(defn persist! []
  (let [fs (js/require "fs")]
    (fs.writeFileSync (:storage-key schema/configs) (pr-str (assoc @*writer-db :sessions {})))))

(defonce *reader-db (atom @*writer-db))

(defn reload! [] (println "Code updated.") (render-clients! @*reader-db))

(defn render-loop! []
  (if (not= @*reader-db @*writer-db)
    (do
     (reset! *reader-db @*writer-db)
     (comment println "render loop")
     (render-clients! @*reader-db)))
  (js/setTimeout render-loop! 300))

(defn main! []
  (println "Loading configs:" (pr-str schema/configs))
  (let [server-ch (run-server! {:port (:port schema/configs)})]
    (go-loop
     []
     (let [[op op-data session-id op-id op-time] (<! server-ch)]
       (.log js/console "Action" (str op) (clj->js op-data) session-id op-id op-time)
       (.log js/console "Database:" (clj->js @*writer-db))
       (try
        (let [new-db (updater @*writer-db op op-data session-id op-id op-time)]
          (reset! *writer-db new-db))
        (catch js/Error e (.log js/console e)))
       (recur)))
    (render-loop!))
  (add-watch *reader-db :log (fn [] ))
  (.on
   js/process
   "SIGINT"
   (fn [code] (persist!) (println "Saving file on exit" code) (.exit js/process)))
  (println "Server started."))
