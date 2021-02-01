
(ns app.client-util
  (:require [clojure.string :as string]
            [app.config :as config]
            ["url-parse" :as url-parse]
            [app.schema :as schema]))

(defn coord-contains? [xs ys]
  (if (empty? ys) true (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false)))

(defn expr? [x] (= :expr (:type x)))

(defn expr-many-items? [x] (let [d (:data x)] (or (> (count d) 2) (some expr? (vals d)))))

(defn leaf? [x] (= :leaf (:type x)))

(defn parse-query! []
  (let [url-obj (url-parse js/location.href true)]
    (js->clj (.-query url-obj) :keywordize-keys true)))

(defn simple? [expr]
  (let [leaf? (fn [x] (= :leaf (:type x)))]
    (and (every? leaf? (vals (:data expr))) (<= (count (:data expr)) 6))))

(def ws-host
  (if (and (exists? js/location) (not (string/blank? (.-search js/location))))
    (let [query (parse-query!)]
      (js/console.log "Loading from url" query)
      (str
       "ws://"
       (or (:host query) "localhost")
       ":"
       (or (:port query) (:port schema/configs))))
    "ws://localhost:6001"))
