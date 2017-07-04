
(ns server.updater.user (:require [server.util :refer [find-first]]))

(defn sign-up [db op-data session-id op-id op-time]
  (let [[username password] op-data
        maybe-user (find-first (fn [user] (= username (:name user))) (vals (:users db)))]
    (if (some? maybe-user)
      (update-in
       db
       [:sessions session-id :notifications]
       (fn [notifications]
         (conj
          notifications
          {:id op-id, :kind :attentive, :text (str "Name is token: " username)})))
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
           (assoc session :user-id (:id maybe-user))
           (update
            session
            :notifications
            (fn [notifications]
              (conj
               notifications
               {:id op-id, :kind :attentive, :text (str "Wrong password for " username)}))))
         (update
          session
          :notifications
          (fn [notifications]
            (conj
             notifications
             {:id op-id, :kind :attentive, :text (str "No user named: " username)}))))))))

(defn log-out [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :user-id] nil))
