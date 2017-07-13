
(ns server.updater.user (:require [server.util :refer [find-first push-warning]]))

(defn sign-up [db op-data session-id op-id op-time]
  (let [[username password] op-data
        maybe-user (find-first (fn [user] (= username (:name user))) (vals (:users db)))]
    (if (some? maybe-user)
      (update-in
       db
       [:sessions session-id :notifications]
       (push-warning op-id (str "Name is token: " username)))
      (-> db
          (assoc-in [:sessions session-id :user-id] op-id)
          (assoc-in
           [:users op-id]
           {:id op-id, :name username, :nickname username, :password password, :avatar nil})))))

(defn log-in [db op-data session-id op-id op-time]
  (let [[username password] op-data
        maybe-user (find-first
                    (fn [user] (and (= username (:name user))))
                    (vals (:users db)))]
    (update-in
     db
     [:sessions session-id]
     (fn [session]
       (if (some? maybe-user)
         (if (= password (:password maybe-user))
           (-> session (assoc :user-id (:id maybe-user)) (assoc :nickname (:name maybe-user)))
           (update
            session
            :notifications
            (push-warning op-id (str "Wrong password for " username))))
         (update
          session
          :notifications
          (push-warning op-id (str "No user named: " username))))))))

(defn log-out [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :user-id] nil))
