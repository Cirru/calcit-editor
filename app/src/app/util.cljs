
(ns app.util (:require [clojure.string :as string] [app.schema :as schema]))

(defn now! [] (.valueOf (js/Date.)))

(defn coord-contains? [xs ys]
  (if (empty? ys) true (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false)))

(defn simple? [expr]
  (and (every? (fn [x] (= :leaf (:type x))) (vals (:data expr))) (<= (count (:data expr)) 6)))

(defn leaf? [x] (= :leaf (:type x)))

(defn expr? [x] (= :expr (:type x)))

(def ws-host
  (let [query (->> (-> (.-search js/location) (subs 1) (string/split "&"))
                   (map (fn [chunk] (update (vec (string/split chunk "=")) 0 keyword)))
                   (into {}))]
    (println "Loading from url" query)
    (str
     "ws://"
     (or (:host query) (.-hostname js/location))
     ":"
     (or (:port query) (:port schema/configs)))))
