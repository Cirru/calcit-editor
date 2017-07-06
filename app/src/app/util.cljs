
(ns app.util )

(defn now! [] (.valueOf (js/Date.)))

(defn coord-contains? [xs ys]
  (if (empty? ys) true (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false)))

(defn simple? [expr]
  (and (every? (fn [x] (= :leaf (:type x))) (vals (:data expr))) (<= (count (:data expr)) 6)))
