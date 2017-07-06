
(ns server.util )

(defn prepend-data [x] [:data x])

(defn expr? [x] (= :expr (:type x)))

(defn to-bookmark [writer] (get (:stack writer) (:pointer writer)))

(def kinds #{:ns :def :proc})

(defn leaf? [x] (= :leaf (:type x)))

(defn to-keys [target-expr] (vec (sort (keys (:data target-expr)))))

(defn to-writer [db session-id] (get-in db [:sessions session-id :writer]))

(defn bookmark->path [bookmark]
  (assert (map? bookmark) "Bookmark should be data")
  (assert (contains? kinds (:kind bookmark)) "invalid bookmark type")
  (if (= :def (:kind bookmark))
    (concat
     [:ir :files (:ns bookmark) :defs (:extra bookmark)]
     (mapcat prepend-data (:focus bookmark)))
    (concat
     [:ir :files (:ns bookmark) (:kind bookmark)]
     (mapcat prepend-data (:focus bookmark)))))

(defn find-first [f xs] (reduce (fn [_ x] (when (f x) (reduced x))) nil xs))
