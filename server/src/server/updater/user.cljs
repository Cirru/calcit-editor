
(ns server.updater.user
  (:require [server.util :refer [find-first push-warning]]
            [clojure.string :as string]
            ["md5" :as md5]
            [server.schema :as schema]))

(defn change-theme [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])]
    (assoc-in db [:users user-id :theme] op-data)))

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
         (if (= (md5 password) (:password maybe-user))
           (-> session (assoc :user-id (:id maybe-user)))
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

(defn nickname [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])]
    (assoc-in db [:users user-id :nickname] (if (string/blank? op-data) "Someone" op-data))))

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
           (merge
            schema/user
            {:id op-id, :name username, :nickname username, :password (md5 password)}))))))
