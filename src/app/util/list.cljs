
(ns app.util.list )

(defn cirru-form? [x] (if (string? x) true (if (vector? x) (map cirru-form? x) false)))

(defn dissoc-idx [xs idx]
  (if (or (neg? idx) (> idx (dec (count xs)))) (throw (js/Error. "Index out of bound!")))
  (cond
    (zero? idx) (subvec xs 1)
    (= idx (dec (count xs))) (pop xs)
    :else (vec (concat (take idx xs) (drop (inc idx) xs)))))
