
(ns server.updater.notify )

(defn clear [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :notifications] []))

(defn push-message [db op-data sid op-id op-time]
  (let [[kind text] op-data]
    (update-in
     db
     [:sessions sid :notifications]
     (fn [xs] (conj xs {:id op-id, :kind kind, :text text, :time op-time})))))
