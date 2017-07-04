
(ns server.updater.session (:require [server.schema :as schema]))

(defn connect [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id] (merge schema/session {:id session-id})))

(defn disconnect [db op-data session-id op-id op-time]
  (update db :sessions (fn [session] (dissoc session session-id))))

(defn remove-notification [db op-data session-id op-id op-time]
  (update-in
   db
   [:sessions session-id :notifications]
   (fn [notifications] (subvec notifications 0 op-data))))
