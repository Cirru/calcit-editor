
(ns app.service
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [app.twig.container :refer [twig-container]]
            [recollect.diff :refer [diff-twig]]
            [recollect.twig :refer [render-twig]]
            ["shortid" :as shortid]
            ["chalk" :as chalk]
            [app.util.detect :refer [port-taken?]]
            ["ws" :as ws]))

(defonce *registry (atom {}))

(defonce client-caches (atom {}))

(defn pick-port! [port next-fn]
  (port-taken?
   port
   (fn [err taken?]
     (if (some? err)
       (do (.error js/console err) (.exit js/process 1))
       (if taken?
         (do (println "port" port "is in use.") (pick-port! (inc port) next-fn))
         (do
          (let [link (str "http://calcit-editor.cirru.org?port=" port)]
            (println "port" port "is ok, please edit on" (.blue chalk link)))
          (next-fn port)))))))

(defn run-server! [on-action! port]
  (pick-port!
   port
   (fn [unoccupied-port]
     (let [WebSocketServer (.-Server ws)
           wss (new WebSocketServer (js-obj "port" unoccupied-port))]
       (.on
        wss
        "connection"
        (fn [socket]
          (let [sid (.generate shortid)]
            (on-action! :session/connect nil sid)
            (swap! *registry assoc sid socket)
            (println (.gray chalk (str "client connected: " sid)))
            (.on
             socket
             "message"
             (fn [rawData]
               (let [action (reader/read-string rawData), [op op-data] action]
                 (on-action! op op-data sid))))
            (.on
             socket
             "close"
             (fn []
               (println (.gray chalk (str "client disconnected: " sid)))
               (swap! *registry dissoc sid)
               (on-action! :session/disconnect nil sid))))))))))

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
