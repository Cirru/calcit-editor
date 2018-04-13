
(ns app.connection
  (:require [cljs.reader :as reader] [recollect.patch :refer [patch-twig]]))

(defonce *global-ws (atom nil))

(defn send! [op op-data] (.send @*global-ws (pr-str [op op-data])))

(defn heartbeat! []
  (js/setTimeout
   (fn []
     (if (some? @*global-ws)
       (do (send! :ping nil) (heartbeat!))
       (println "Disabled heartbeat since connection lost.")))
   30000))

(defn setup-socket! [*store configs]
  (let [ws-url (:url configs)
        ws (js/WebSocket. ws-url)
        handle-close! (if (fn? (:on-close! configs)) (:on-close! configs) identity)
        handle-open! (if (fn? (:on-open! configs)) (:on-open! configs) identity)]
    (set! ws.onopen (fn [event] (reset! *global-ws ws) (handle-open! event) (heartbeat!)))
    (set! ws.onclose (fn [event] (reset! *global-ws nil) (handle-close! event)))
    (set!
     ws.onmessage
     (fn [event]
       (let [changes (reader/read-string event.data)]
         (comment println "Changes" (count changes))
         (reset! *store (patch-twig @*store changes)))))))
