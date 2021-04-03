
(ns app.util
  (:require [clojure.string :as string]
            [app.schema :as schema]
            [bisection-key.core :as bisection]
            [cirru-edn.core :as cirru-edn]
            ["shortid" :as shortid]))

(def kinds #{:ns :def :proc})

(defn prepend-data [x] [:data x])

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

(defn bookmark-full-str [bookmark]
  (case (:kind bookmark)
    :def (str (:ns bookmark) "/" (:extra bookmark))
    :ns (str (:ns bookmark) "/")
    (do (js/console.warn (str "Unknown" (pr-str bookmark))) "")))

(defn cirru->tree [xs author timestamp]
  (if (vector? xs)
    (merge
     schema/expr
     {:at timestamp,
      :by author,
      :data (loop [result {}, ys xs, next-id bisection/mid-id]
        (if (empty? ys)
          result
          (let [y (first ys)]
            (recur
             (assoc result next-id (cirru->tree y author timestamp))
             (rest ys)
             (bisection/bisect next-id bisection/max-id)))))})
    (merge schema/leaf {:at timestamp, :by author, :text xs})))

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

(defn db->string [db]
  (cirru-edn/write (-> db (dissoc :sessions {}) (dissoc :saved-files {}) (dissoc :repl))))

(defn expr? [x] (= :expr (:type x)))

(defn tree->cirru [x]
  (if (= :leaf (:type x))
    (:text x)
    (with-meta
     (->> (:data x) (sort-by first) (map (fn [entry] (tree->cirru (val entry)))) (vec))
     :quoted-cirru)))

(defn file->cirru [file]
  (-> file
      (update :ns tree->cirru)
      (update :proc tree->cirru)
      (update
       :defs
       (fn [defs]
         (->> defs (map (fn [entry] (let [[k xs] entry] [k (tree->cirru xs)]))) (into {}))))))

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

(defn find-first [f xs] (reduce (fn [_ x] (when (f x) (reduced x))) nil xs))

(defn hide-empty-fields [x]
  (->> x (remove (fn [[k v]] (or (nil? v) (empty? v)))) (into {})))

(defn leaf? [x] (= :leaf (:type x)))

(defn now! [] (.now js/Date))

(defn ns->path [ns-text extension]
  (assert (string? extension) (str "extension should be string but got: " extension))
  (-> ns-text (string/replace "." "/") (string/replace "-" "_") (str extension)))

(defn parse-def [text]
  (let [clean-text (-> text (string/replace "@" ""))]
    (if (string/includes? clean-text "/")
      (let [[ns-text def-text] (string/split clean-text "/")]
        {:method :as, :key ns-text, :def def-text})
      {:method :refer, :key clean-text, :def clean-text})))

(defn parse-require [piece]
  (let [method (get piece 1), ns-text (get piece 0)]
    (case method
      ":as" {(get piece 2) {:method :as, :ns ns-text}}
      ":refer"
        (->> (get piece 2)
             (filter (fn [def-text] (not= def-text "[]")))
             (map (fn [def-text] [def-text {:method :refer, :ns ns-text, :def def-text}]))
             (into {}))
      ":default" {(get piece 2) {:method :refer, :ns ns-text, :def (get piece 2)}}
      (do (println "Unknown referring:" piece) nil))))

(defn parse-deps [require-exprs]
  (let [require-rules (->> require-exprs (filter (fn [xs] (= ":require" (first xs)))) (first))]
    (if (some? require-rules)
      (loop [result {}, xs (rest require-rules)]
        (comment println "loop" result xs)
        (if (empty? xs)
          result
          (let [rule (first xs)]
            (recur
             (merge result (parse-require (if (= (first rule) "[]") (subvec rule 1) rule)))
             (rest xs))))))))

(defn push-info [op-id op-time text]
  (fn [xs]
    (conj xs (merge schema/notification {:id op-id, :kind :info, :text text, :time op-time}))))

(defn push-warning [op-id op-time text]
  (fn [xs]
    (conj
     xs
     (merge schema/notification {:id op-id, :kind :warning, :text text, :time op-time}))))

(defn same-buffer? [x y]
  (and (= (:kind x) (:kind y)) (= (:ns x) (:ns y)) (= (:extra x) (:extra y))))

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

(defn to-bookmark [writer] (get (:stack writer) (:pointer writer)))

(defn to-keys [target-expr] (vec (sort (keys (:data target-expr)))))

(defn to-writer [db session-id] (get-in db [:sessions session-id :writer]))
