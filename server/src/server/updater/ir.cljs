
(ns server.updater.ir (:require [server.schema :as schema]))

(defn add-ns [db op-data session-id op-id op-time]
  (assoc-in db [:ir :files op-data] schema/file))

(defn add-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])
        user-id (get-in db [:sessions session-id :user-id])]
    (assoc-in
     db
     [:ir :files selected-ns :defs op-data]
     (assoc schema/expr :time op-time :author user-id))))

(defn remove-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])]
    (update-in db [:ir :files selected-ns :defs] (fn [defs] (dissoc defs op-data)))))
