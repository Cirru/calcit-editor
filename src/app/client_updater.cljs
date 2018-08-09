
(ns app.client-updater )

(defn abstract [states] (assoc-in states [:editor :data :abstract?] true))

(defn clear-editor [states]
  (update
   states
   :editor
   (fn [scope] (->> scope (filter (fn [[k v]] (keyword? k))) (into {})))))

(defn draft-box [states] (assoc-in states [:editor :data :draft-box?] true))
