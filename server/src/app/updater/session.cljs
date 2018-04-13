
(ns app.updater.session (:require [app.schema :as schema]))

(defn connect [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id] (merge schema/session {:id session-id})))

(defn disconnect [db op-data session-id op-id op-time]
  (update db :sessions (fn [session] (dissoc session session-id))))

(defn select-ns [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :writer :selected-ns] op-data))
