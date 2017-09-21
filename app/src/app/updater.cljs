
(ns app.updater )

(defn abstract [states] (assoc-in states [:editor :data :abstract?] true))
