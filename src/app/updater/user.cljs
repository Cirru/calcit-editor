
(ns app.updater.user
  (:require [app.util :refer [find-first push-warning]]
            [clojure.string :as string]
            ["md5" :as md5]
            [app.schema :as schema]))

(defn change-theme [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])]
    (-> db
        (assoc-in [:users user-id :theme] op-data)
        (assoc-in [:sessions sid :theme] op-data))))

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
            (push-warning op-id op-time (str "Wrong password for " username))))
         (update
          session
          :notifications
          (push-warning op-id op-time (str "No user named: " username))))))))

(defn log-out [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :user-id] nil))

(defn nickname [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])]
    (assoc-in db [:users user-id :nickname] (if (string/blank? op-data) "Someone" op-data))))

(defn sign-up [db op-data session-id op-id op-time]
  (let [[username password] op-data
        maybe-user (find-first (fn [user] (= username (:name user))) (vals (:users db)))
        new-user-id (str "u" (count (:users db)))]
    (if (some? maybe-user)
      (update-in
       db
       [:sessions session-id :notifications]
       (push-warning op-id op-time (str "Name is token: " username)))
      (-> db
          (assoc-in [:sessions session-id :user-id] new-user-id)
          (assoc-in
           [:users new-user-id]
           (merge
            schema/user
            {:id new-user-id, :name username, :nickname username, :password (md5 password)}))))))
