
(ns server.network
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [server.twig.container :refer [twig-container]]
            [recollect.diff :refer [diff-twig]]
            [recollect.twig :refer [render-twig]]
            ["shortid" :as shortid]
            ["chalk" :as chalk]
            [server.util.detect :refer [port-taken?]]
            ["uws" :as ws]))

(defonce *registry (atom {}))

(defn error-port-taken! [port]
  (do
   (println
    (.red
     chalk
     (str
      "Failed to start server, port "
      port
      " is in use!\nYou can try `port="
      (inc port)
      " cumulo-editor`.")))
   (.exit js/process 1)))

(defn run-server! [on-action! port]
  (let [WebSocketServer (.-Server ws)]
    (port-taken?
     port
     (fn [err taken?]
       (if (some? err)
         (do (.error js/console err) (.exit js/process 1))
         (if taken?
           (error-port-taken! port)
           (let [wss (new WebSocketServer (js-obj "port" port))]
             (.on
              wss
              "connection"
              (fn [socket]
                (let [sid (.generate shortid), op-id (.generate shortid)]
                  (on-action! :session/connect nil sid op-id)
                  (swap! *registry assoc sid socket)
                  (println (.gray chalk (str "client connected: " sid)))
                  (.on
                   socket
                   "message"
                   (fn [rawData]
                     (let [action (reader/read-string rawData), [op op-data] action]
                       (on-action! op op-data sid op-id))))
                  (.on
                   socket
                   "close"
                   (fn []
                     (println (.gray chalk (str "client disconnected: " sid)))
                     (swap! *registry dissoc sid)
                     (on-action! :session/disconnect nil sid op-id))))))
             (println
              "Server started, please edit on"
              (.blue chalk (str "http://cumulo-editor.cirru.org?port=" port))))))))))

(defonce client-caches (atom {}))

(defn sync-clients! [db]
  (doseq [sid (keys @*registry)]
    (let [session-id sid
          session (get-in db [:sessions sid])
          old-store (or (get @client-caches session-id) nil)
          new-store (render-twig (twig-container db session) old-store)
          changes (diff-twig old-store new-store {:key :id})
          socket (get @*registry session-id)]
      (comment .info js/console "Changes for" session-id ":" (clj->js changes))
      (if (and (not (empty? changes)) (some? socket))
        (do (.send socket (pr-str changes)) (swap! client-caches assoc session-id new-store))))))
