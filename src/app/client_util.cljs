
(ns app.client-util
  (:require [clojure.string :as string]
            [app.config :as config]
            ["url-parse" :as url-parse]
            [app.schema :as schema]))

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

(defn now! [] (.now js/Date))

(defn parse-query! []
  (let [url-obj (url-parse js/location.href true)]
    (js->clj (.-query url-obj) :keywordize-keys true)))

(defn simple? [expr]
  (let [leaf? (fn [x] (= :leaf (:type x)))]
    (and (every? leaf? (vals (:data expr))) (<= (count (:data expr)) 6))))

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
      (js/console.log "Loading from url" query)
      (str
       "ws://"
       (or (:host query) "localhost")
       ":"
       (or (:port query) (:port schema/configs))))
    "ws://localhost:6001"))
