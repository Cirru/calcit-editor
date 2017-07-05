
(ns server.util )

(defn find-first [f xs] (reduce (fn [_ x] (when (f x) (reduced x))) nil xs))

(defn expr? [x] (= :expr (:type x)))

(defn leaf? [x] (= :leaf (:type x)))
