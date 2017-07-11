
(ns server.util.stack )

(defn =bookmark? [x y]
  (and (= (:kind x) (:kind y)) (= (:ns x) (:ns y)) (= (:extra x) (:extra y))))

(defn index-of-bookmark
  ([stack bookmark] (index-of-bookmark stack bookmark 0))
  ([stack bookmark idx]
   (if (empty? stack)
     -1
     (if (=bookmark? bookmark (first stack)) idx (recur (rest stack) bookmark (inc idx))))))

(defn push-bookmark [bookmark]
  (fn [writer]
    (let [{pointer :pointer, stack :stack} writer, idx (index-of-bookmark stack bookmark)]
      (if (neg? idx)
        (-> writer
            (update :stack (fn [stack] (conj stack bookmark)))
            (assoc :pointer (count stack)))
        (-> writer (assoc :pointer idx))))))
