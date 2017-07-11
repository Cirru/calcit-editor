
(ns server.network
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [cljs.core.async :refer [chan >!]]
            [server.twig.container :refer [twig-container]]
            [recollect.diff :refer [diff-bunch]]
            [recollect.bunch :refer [render-bunch]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce socket-registry (atom {}))

(defonce server-chan (chan))

(def shortid (js/require "shortid"))

(def ws (js/require "uws"))

(def WebSocketServer (.-Server ws))

(defn handle-message [op op-data session-id]
  (let [op-id (.generate shortid), op-time (.valueOf (js/Date.))]
    (go (>! server-chan [op op-data session-id op-id op-time]))))

(defn run-server! [configs]
  (let [port (:port configs), wss (new WebSocketServer (js-obj "port" port))]
    (println "Edit with " (str "http://cumulo-editor.cirru.org?port=" port))
    (.on
     wss
     "connection"
     (fn [socket]
       (let [session-id (.generate shortid)]
         (handle-message :session/connect nil session-id)
         (swap! socket-registry assoc session-id socket)
         (.info js/console "New client.")
         (.on
          socket
          "message"
          (fn [rawData]
            (let [action (reader/read-string rawData), [op op-data] action]
              (handle-message op op-data session-id))))
         (.on
          socket
          "close"
          (fn []
            (.warn js/console "Client closed!")
            (swap! socket-registry dissoc session-id)
            (handle-message :session/disconnect nil session-id)))))))
  server-chan)

(defonce client-caches (atom {}))

(defn render-clients! [db]
  (doseq [session-entry (:sessions db)]
    (let [[session-id session] session-entry
          old-store (or (get @client-caches session-id) nil)
          new-store (render-bunch (twig-container db session) old-store)
          *changes (atom [])
          collect! (fn [x] (swap! *changes conj x))
          socket (get @socket-registry session-id)]
      (diff-bunch collect! [] old-store new-store)
      (.info js/console "Changes for" session-id ":" (clj->js @*changes))
      (if (and (not= *changes []) (some? socket))
        (do
         (.send socket (pr-str @*changes))
         (swap! client-caches assoc session-id new-store))))))
