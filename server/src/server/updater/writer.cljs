
(ns server.updater.writer )

(defn edit [db op-data session-id op-id op-time]
  (let [ns-text (get-in db [:sessions session-id :writer :selected-ns])
        bookmark (assoc op-data :ns ns-text :focus [])]
    (-> db
        (update-in
         [:sessions session-id :writer]
         (fn [writer]
           (let [{stack :stack, pointer :pointer} writer]
             (assoc writer :stack (conj stack bookmark) :pointer (count stack)))))
        (assoc-in [:sessions session-id :router] {:name :editor}))))

(defn point-to [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :writer :pointer] op-data))

(defn focus [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])]
    (assoc-in db [:sessions session-id :writer :stack (:pointer writer) :focus] op-data)))
