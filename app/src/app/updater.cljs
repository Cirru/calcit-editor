
(ns app.updater )

(defn abstract [states] (assoc-in states [:editor :data :abstract?] true))

(defn draft-box [states] (assoc-in states [:editor :data :draft-box?] true))
