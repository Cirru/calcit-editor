
(ns server.updater.notify )

(defn push-error [db op-data session-id op-id op-time]
  (update-in
   db
   [:sessions session-id :notifications]
   (fn [notifications] (conj notifications {:id op-id, :kind :error, :text op-data}))))

(defn clear [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :notifications] []))
