
(ns app.util.stack )

(defn =bookmark? [x y]
  (and (= (:kind x) (:kind y)) (= (:ns x) (:ns y)) (= (:extra x) (:extra y))))

(defn index-of-bookmark
  ([stack bookmark] (index-of-bookmark stack bookmark 0))
  ([stack bookmark idx]
   (if (empty? stack)
     -1
     (if (=bookmark? bookmark (first stack)) idx (recur (rest stack) bookmark (inc idx))))))

(defn push-bookmark
  ([bookmark] (push-bookmark bookmark false))
  ([bookmark forced?]
   (fn [writer]
     (let [{pointer :pointer, stack :stack} writer, idx (index-of-bookmark stack bookmark)]
       (if (or forced? (neg? idx))
         (-> writer
             (update
              :stack
              (fn [stack]
                (cond
                  (empty? stack) [bookmark]
                  (= pointer (dec (count stack))) (conj stack bookmark)
                  (=bookmark? bookmark (get stack (inc pointer))) stack
                  :else
                    (vec
                     (concat
                      (take (inc pointer) stack)
                      [bookmark]
                      (drop (inc pointer) stack))))))
             (update :pointer (fn [p] (if (empty? stack) 0 (inc p)))))
         (-> writer (assoc :pointer idx)))))))
