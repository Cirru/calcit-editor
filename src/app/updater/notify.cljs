
(ns app.updater.notify (:require [app.util :refer [push-info]]))

(defn broadcast [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])
        user-name (get-in db [:users user-id :name])]
    (update
     db
     :sessions
     (fn [sessions]
       (->> sessions
            (map
             (fn [[k session]]
               [k
                (update
                 session
                 :notifications
                 (push-info op-id op-time (str user-name ": " op-data)))]))
            (into {}))))))

(defn clear [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :notifications] []))

(defn push-message [db op-data sid op-id op-time]
  (let [[kind text] op-data]
    (update-in
     db
     [:sessions sid :notifications]
     (fn [xs] (conj xs {:id op-id, :kind kind, :text text, :time op-time})))))
