
(ns app.util (:require [clojure.string :as string] [app.schema :as schema]))

(defn now! [] (.valueOf (js/Date.)))

(defn coord-contains? [xs ys]
  (if (empty? ys) true (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false)))

(defn simple? [expr]
  (and (every? (fn [x] (= :leaf (:type x))) (vals (:data expr))) (<= (count (:data expr)) 6)))

(defn leaf? [x] (= :leaf (:type x)))

(defn expr? [x] (= :expr (:type x)))

(defn parse-query! []
  (->> (-> (.-search js/location) (subs 1) (string/split "&"))
       (map (fn [chunk] (update (vec (string/split chunk "=")) 0 keyword)))
       (into {})))

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

(defn tree->cirru [x]
  (if (= :leaf (:type x))
    (:text x)
    (->> (:data x) (sort-by first) (map (fn [entry] (tree->cirru (val entry)))) (vec))))
