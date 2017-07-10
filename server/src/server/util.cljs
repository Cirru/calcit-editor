
(ns server.util
  (:require [clojure.string :as string]
            [server.schema :as schema]
            [bisection-key.core :as bisection]))

(defn prepend-data [x] [:data x])

(defn expr? [x] (= :expr (:type x)))

(defn to-bookmark [writer] (get (:stack writer) (:pointer writer)))

(defn tree->cirru [x]
  (if (= :leaf (:type x))
    (:text x)
    (->> (:data x) (sort-by first) (map (fn [entry] (tree->cirru (val entry)))) (vec))))

(defn cirru->tree [xs author timestamp]
  (if (vector? xs)
    (merge
     schema/expr
     {:time timestamp,
      :author author,
      :data (loop [result {}, ys xs, next-id bisection/mid-id]
        (if (empty? ys)
          result
          (let [y (first ys)]
            (recur
             (assoc result next-id (cirru->tree y author timestamp))
             (rest ys)
             (bisection/bisect next-id bisection/max-id)))))})
    (merge schema/leaf {:time timestamp, :author author, :text xs})))

(def kinds #{:ns :def :proc})

(defn leaf? [x] (= :leaf (:type x)))

(defn ns->path [ns-text]
  (-> ns-text
      (string/replace "." "/")
      (string/replace "-" "_")
      (str (:extension schema/configs))))

(defn same-buffer? [x y]
  (and (= (:kind x) (:kind y)) (= (:ns x) (:ns y)) (= (:extra x) (:extra y))))

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

(defn file->cirru [file]
  (-> file
      (update :ns tree->cirru)
      (update :proc tree->cirru)
      (update
       :defs
       (fn [defs]
         (->> defs (map (fn [entry] (let [[k xs] entry] [k (tree->cirru xs)]))) (into {}))))))

(defn find-first [f xs] (reduce (fn [_ x] (when (f x) (reduced x))) nil xs))
