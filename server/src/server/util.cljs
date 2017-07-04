
(ns server.util )

(defn find-first [f xs] (reduce (fn [_ x] (when (f x) (reduced x))) nil xs))
