
(ns app.network (:require [cljs.reader :as reader] [recollect.patch :refer [patch-bunch]]))

(defonce *global-ws (atom nil))

(defn send! [op op-data] (.send @*global-ws (pr-str [op op-data])))

(defn setup-socket! [*store configs]
  (let [ws-url (:url configs)
        ws (js/WebSocket. ws-url)
        handle-close! (if (fn? (:on-close! configs)) (:on-close! configs) identity)
        handle-open! (if (fn? (:on-open! configs)) (:on-open! configs) identity)]
    (set! ws.onopen (fn [event] (reset! *global-ws ws) (handle-open! event)))
    (set! ws.onclose (fn [event] (handle-close! event)))
    (set!
     ws.onmessage
     (fn [event]
       (let [changes (reader/read-string event.data)]
         (comment println "Changes" (count changes))
         (reset! *store (patch-bunch @*store changes)))))))
