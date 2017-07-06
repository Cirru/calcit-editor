
(ns server.util )

(defn find-first [f xs] (reduce (fn [_ x] (when (f x) (reduced x))) nil xs))

(defn expr? [x] (= :expr (:type x)))

(defn leaf? [x] (= :leaf (:type x)))

(defn prepend-data [x] [:data x])

(def kinds #{:ns :def :proc})

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
