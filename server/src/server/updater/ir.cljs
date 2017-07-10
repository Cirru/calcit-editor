
(ns server.updater.ir
  (:require [server.schema :as schema]
            [bisection-key.core :as bisection]
            [server.util
             :refer
             [expr?
              leaf?
              bookmark->path
              to-writer
              to-bookmark
              to-keys
              cirru->tree
              pick-second-key]]))

(defn rename [db op-data session-id op-id op-time]
  (let [{kind :kind, ns-info :ns, extra-info :extra} op-data]
    (cond
      (= :ns kind)
        (let [{old-ns :from, new-ns :to} ns-info
              expr (get-in db [:ir :files old-ns :ns])
              next-id (pick-second-key (:data expr))]
          (-> db
              (update-in
               [:ir :files]
               (fn [files] (-> files (dissoc old-ns) (assoc new-ns (get files old-ns)))))
              (assoc-in [:sessions session-id :writer :stack (:index op-data) :ns] new-ns)
              (update-in [:ir :files new-ns :ns :data next-id :text] (fn [x] new-ns))))
      (= :def kind)
        (let [{old-ns :from, new-ns :to} ns-info
              {old-def :from, new-def :to} extra-info
              expr (get-in db [:ir :files old-ns :defs old-def])
              next-id (pick-second-key (:data expr))]
          (-> db
              (update-in
               [:ir :files]
               (fn [files]
                 (-> files
                     (update-in [old-ns :defs] (fn [file] (dissoc file old-def)))
                     (assoc-in [new-ns :defs new-def] (get-in files [old-ns :defs old-def])))))
              (update-in
               [:sessions session-id :writer :stack (:index op-data)]
               (fn [bookmark] (-> bookmark (assoc :ns new-ns) (assoc :extra new-def))))
              (update-in
               [:ir :files new-ns :defs new-def :data next-id :text]
               (fn [x] new-def))))
      :else (do (println "Unexpected kind:" kind) db))))

(defn add-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])
        user-id (get-in db [:sessions session-id :user-id])
        cirru-expr ["defn" op-data []]]
    (assoc-in
     db
     [:ir :files selected-ns :defs op-data]
     (cirru->tree cirru-expr user-id op-time))))

(defn leaf-before [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        current-key (last (:focus bookmark))
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        child-keys (to-keys target-expr)
        idx (.indexOf child-keys current-key)
        next-id (bisection/bisect
                 (if (zero? idx) bisection/min-id (get child-keys (dec idx)))
                 current-key)
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :time op-time :author user-id)]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-leaf)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id))))))

(defn leaf-after [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        current-key (last (:focus bookmark))
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        child-keys (vec (sort (keys (:data target-expr))))
        idx (.indexOf child-keys current-key)
        next-id (bisection/bisect
                 current-key
                 (if (= idx (dec (count child-keys)))
                   bisection/max-id
                   (get child-keys (inc idx))))
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :time op-time :author user-id)]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-leaf)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id))))))

(defn remove-ns [db op-data session-id op-id op-time]
  (-> db (update-in [:ir :files] (fn [files] (dissoc files op-data)))))

(defn duplicate [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        target-expr (get-in db (bookmark->path bookmark))
        parent-path (bookmark->path (update bookmark :focus butlast))
        parent-expr (get-in db parent-path)
        child-keys (to-keys parent-expr)
        last-coord (last (:focus bookmark))
        idx (.indexOf child-keys last-coord)
        next-id (if (= idx (dec (count child-keys)))
                  bisection/max-id
                  (bisection/bisect last-coord (get child-keys (inc idx))))]
    (-> db
        (update-in
         parent-path
         (fn [expr] (update expr :data (fn [data] (assoc data next-id target-expr)))))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id))))))

(defn expr-before [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        current-key (last (:focus bookmark))
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        child-keys (to-keys target-expr)
        idx (.indexOf child-keys current-key)
        next-id (bisection/bisect
                 (if (zero? idx) bisection/min-id (get child-keys (dec idx)))
                 current-key)
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :time op-time :author user-id)
        new-expr (-> schema/expr
                     (assoc :time op-time :author user-id)
                     (assoc-in [:data bisection/mid-id] new-leaf))]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id bisection/mid-id))))))

(defn update-leaf [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        data-path (bookmark->path bookmark)]
    (-> db (update-in data-path (fn [leaf] (assoc leaf :text op-data))))))

(defn unindent [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        parent-bookmark (update bookmark :focus butlast)
        last-coord (last (:focus bookmark))
        parent-path (bookmark->path parent-bookmark)]
    (-> db
        (update-in
         parent-path
         (fn [base-expr]
           (let [expr (get-in base-expr [:data last-coord])
                 child-keys (vec (sort (keys (:data base-expr))))
                 children (->> (:data expr) (sort-by first) (map val))
                 idx (.indexOf child-keys last-coord)
                 limit-id (if (= idx (dec (count child-keys)))
                            bisection/max-id
                            (get child-keys (inc idx)))]
             (loop [result base-expr, xs children, next-id last-coord]
               (if (empty? xs)
                 result
                 (recur
                  (assoc-in result [:data next-id] (first xs))
                  (rest xs)
                  (bisection/bisect next-id limit-id))))))))))

(defn indent [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        data-path (bookmark->path bookmark)
        user-id (get-in db [:sessions session-id :user-id])
        new-expr (assoc schema/expr :time op-time :author user-id)]
    (-> db
        (update-in data-path (fn [node] (assoc-in new-expr [:data bisection/mid-id] node)))
        (update-in
         [:sessions session-id :writer :stack pointer :focus]
         (fn [focus] (vec (concat (butlast focus) [(last focus) bisection/mid-id])))))))

(defn remove-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])]
    (update-in db [:ir :files selected-ns :defs] (fn [defs] (dissoc defs op-data)))))

(defn append-leaf [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        focus (:focus bookmark)
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :author user-id :time op-time)
        expr-path (bookmark->path bookmark)
        target-expr (get-in db expr-path)
        new-id (if (empty? (:data target-expr))
                 bisection/mid-id
                 (let [max-entry (apply max (keys (:data target-expr)))]
                   (bisection/bisect max-entry bisection/max-id)))]
    (-> db
        (update-in
         expr-path
         (fn [expr] (if (expr? expr) (assoc-in expr [:data new-id] new-leaf) expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj focus new-id))))))

(defn expr-after [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        current-key (last (:focus bookmark))
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        child-keys (to-keys target-expr)
        idx (.indexOf child-keys current-key)
        next-id (bisection/bisect
                 current-key
                 (if (= idx (dec (count child-keys)))
                   bisection/max-id
                   (get child-keys (inc idx))))
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :time op-time :author user-id)
        new-expr (-> schema/expr
                     (assoc :time op-time :author user-id)
                     (assoc-in [:data bisection/mid-id] new-leaf))]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id bisection/mid-id))))))

(defn add-ns [db op-data session-id op-id op-time]
  (let [user-id (get-in db [:sessions session-id :user-id])
        cirru-expr ["ns" op-data]
        default-expr (cirru->tree cirru-expr user-id op-time)
        empty-expr (cirru->tree [] user-id op-time)]
    (assoc-in db [:ir :files op-data] (assoc schema/file :ns default-expr :proc empty-expr))))

(defn delete-node [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        child-keys (sort (keys (:data (get-in db data-path))))
        deleted-key (last (:focus bookmark))
        idx (.indexOf child-keys deleted-key)]
    (-> db
        (update-in
         data-path
         (fn [expr] (update expr :data (fn [children] (dissoc children deleted-key)))))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus]
           (if (zero? idx)
             (vec (butlast focus))
             (assoc focus (dec (count focus)) (get (vec child-keys) (dec idx)))))))))

(defn unindent-leaf [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        parent-bookmark (update bookmark :focus butlast)
        parent-path (bookmark->path parent-bookmark)
        parent-expr (get-in db parent-path)]
    (if (= 1 (count (:data parent-expr)))
      (-> db
          (update-in parent-path (fn [expr] (first (vals (:data expr)))))
          (update-in
           [:sessions session-id :writer :stack (:pointer writer) :focus]
           (fn [focus] (vec (butlast focus)))))
      db)))
