
(ns server.util
  (:require [clojure.string :as string]
            [server.schema :as schema]
            [bisection-key.core :as bisection]))

(defn parse-require [piece]
  (let [method (get piece 1), ns-text (get piece 0)]
    (if (= method ":as")
      {(get piece 2) {:method :as, :ns ns-text}}
      (->> (get piece 2)
           (rest)
           (map (fn [def-text] [def-text {:method :refer, :ns ns-text, :def def-text}]))
           (into {})))))

(defn parse-deps [require-exprs]
  (let [require-rules (->> require-exprs (filter (fn [xs] (= ":require" (first xs)))) (first))]
    (if (some? require-rules)
      (loop [result {}, xs (rest require-rules)]
        (comment println "loop" result xs)
        (if (empty? xs)
          result
          (let [rule (first xs)]
            (recur (merge result (parse-require (subvec rule 1))) (rest xs))))))))

(defn pick-second-key [m] (first (rest (sort (keys m)))))

(defn parse-def [text]
  (let [clean-text (-> text (string/replace "@" ""))]
    (if (string/includes? clean-text "/")
      (let [[ns-text def-text] (string/split clean-text "/")]
        {:method :as, :key ns-text, :def def-text})
      {:method :refer, :key clean-text, :def clean-text})))

(defn prepend-data [x] [:data x])

(defn push-info [op-id text]
  (fn [xs] (conj xs (merge schema/notification {:id op-id, :kind :info, :text text}))))

(defn expr? [x] (= :expr (:type x)))

(defn to-bookmark [writer] (get (:stack writer) (:pointer writer)))

(defn tree->cirru [x]
  (if (= :leaf (:type x))
    (:text x)
    (->> (:data x) (sort-by first) (map (fn [entry] (tree->cirru (val entry)))) (vec))))

(def shortid (js/require "shortid"))

(defn cirru->tree [xs author timestamp]
  (if (vector? xs)
    (merge
     schema/expr
     {:time timestamp,
      :author author,
      :id (.generate shortid),
      :data (loop [result {}, ys xs, next-id bisection/mid-id]
        (if (empty? ys)
          result
          (let [y (first ys)]
            (recur
             (assoc result next-id (cirru->tree y author timestamp))
             (rest ys)
             (bisection/bisect next-id bisection/max-id)))))})
    (merge schema/leaf {:time timestamp, :author author, :text xs, :id (.generate shortid)})))

(defn cirru->file [file author timestamp]
  (-> file
      (update :ns #(cirru->tree % author timestamp))
      (update :proc #(cirru->tree % author timestamp))
      (update
       :defs
       (fn [defs]
         (->> defs
              (map (fn [entry] (let [[k xs] entry] [k (cirru->tree xs author timestamp)])))
              (into {}))))))

(def kinds #{:ns :def :proc})

(defn leaf? [x] (= :leaf (:type x)))

(defn ns->path [ns-text extension]
  (assert (string? extension) (str "extension should be string but got: " extension))
  (-> ns-text (string/replace "." "/") (string/replace "-" "_") (str extension)))

(defn same-buffer? [x y]
  (and (= (:kind x) (:kind y)) (= (:ns x) (:ns y)) (= (:extra x) (:extra y))))

(defn to-keys [target-expr] (vec (sort (keys (:data target-expr)))))

(defn push-warning [op-id text]
  (fn [xs] (conj xs (merge schema/notification {:id op-id, :kind :warning, :text text}))))

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
