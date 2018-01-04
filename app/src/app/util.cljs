
(ns app.util (:require [clojure.string :as string] [app.schema :as schema]))

(defn coord-contains? [xs ys]
  (if (empty? ys) true (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false)))

(defn expr? [x] (= :expr (:type x)))

(defn tree->cirru [x]
  (if (= :leaf (:type x))
    (:text x)
    (->> (:data x) (sort-by first) (map (fn [entry] (tree->cirru (val entry)))) (vec))))

(defn file-tree->cirru [file]
  (-> file
      (update :ns tree->cirru)
      (update :proc tree->cirru)
      (update
       :defs
       (fn [defs]
         (->> defs
              (map
               (fn [entry]
                 (let [[def-text def-tree] entry] [def-text (tree->cirru def-tree)])))
              (into {}))))))

(defn leaf? [x] (= :leaf (:type x)))

(defn now! [] (.valueOf (js/Date.)))

(defn parse-query! []
  (let [search (.-search js/location)]
    (if (empty? search)
      {}
      (->> (-> search (subs 1) (string/split "&"))
           (map (fn [chunk] (update (vec (string/split chunk "=")) 0 keyword)))
           (into {})))))

(defn simple? [expr]
  (and (every? (fn [x] (= :leaf (:type x))) (vals (:data expr))) (<= (count (:data expr)) 6)))

(defn stringify-s-expr [x]
  (if (vector? x)
    (str
     "("
     (string/join
      " "
      (map
       (fn [y]
         (if (vector? y) (stringify-s-expr y) (if (string/includes? y " ") (pr-str y) y)))
       x))
     ")")))

(def ws-host
  (if (and (exists? js/location) (not (string/blank? (.-search js/location))))
    (let [query (parse-query!)]
      (println "Loading from url" query)
      (str
       "ws://"
       (or (:host query) "localhost")
       ":"
       (or (:port query) (:port schema/configs))))
    "ws://localhost:6001"))
