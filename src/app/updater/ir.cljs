
(ns app.updater.ir
  (:require [app.schema :as schema]
            [bisection-key.core :as bisection]
            [app.util
             :refer
             [expr?
              leaf?
              bookmark->path
              to-writer
              to-bookmark
              to-keys
              cirru->tree
              cirru->file]]
            [app.util.list :refer [dissoc-idx]]
            [bisection-key.util
             :refer
             [key-before
              key-after
              key-prepend
              key-append
              assoc-prepend
              key-nth
              assoc-nth
              val-nth]]
            [clojure.string :as string]
            [app.util :refer [push-warning]]))

(defn add-def [db op-data session-id op-id op-time]
  (assert (vector? op-data) "expects op-data of [ns text]")
  (let [[ns-part def-part] op-data
        user-id (get-in db [:sessions session-id :user-id])
        cirru-expr ["defn" def-part []]]
    (when (nil? ns-part)
      (println (get-in db [:sessions session-id :writer]))
      (throw (js/Error. "Empty ns target.")))
    (assoc-in
     db
     [:ir :files ns-part :defs def-part]
     (cirru->tree cirru-expr user-id op-time))))

(defn add-ns [db op-data session-id op-id op-time]
  (let [user-id (get-in db [:sessions session-id :user-id])
        cirru-expr ["ns" op-data]
        default-expr (cirru->tree cirru-expr user-id op-time)
        empty-expr (cirru->tree [] user-id op-time)]
    (-> db
        (assoc-in
         [:ir :files op-data]
         (assoc schema/file :ns default-expr :proc empty-expr))
        (assoc-in [:sessions session-id :writer :selected-ns] op-data))))

(defn append-leaf [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        focus (:focus bookmark)
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :by user-id :at op-time)
        expr-path (bookmark->path bookmark)
        target-expr (get-in db expr-path)
        new-id (key-append (:data target-expr))]
    (-> db
        (update-in
         expr-path
         (fn [expr] (if (expr? expr) (assoc-in expr [:data new-id] new-leaf) expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj focus new-id))))))

(defn call-replace-expr [expr from to]
  (case (:type expr)
    :expr
      (update
       expr
       :data
       (fn [data]
         (->> data
              (map (fn [[k v]] [k (call-replace-expr v from to)]))
              (remove (fn [[k v]] (and (= (:type v) :leaf) (string/blank? (:text v)))))
              (into {}))))
    :leaf
      (cond
        (= (:text expr) from) (assoc expr :text to)
        (= (:text expr) (str "@" from)) (assoc expr :text (str "@" to))
        :else expr)
    (do (println "Unknown expr" expr))))

(defn clone-ns [db op-data sid op-id op-time]
  (let [writer (get-in db [:sessions sid :writer])
        selected-ns (:selected-ns writer)
        files (get-in db [:ir :files])
        warn (fn [x]
               (update-in db [:sessions sid :notifications] (push-warning op-id op-time x)))
        new-ns op-data]
    (cond
      (not (and (string? new-ns) (string/includes? new-ns "."))) (warn "Not a valid string!")
      (contains? files op-data) (warn (str new-ns " already existed!"))
      (not (contains? files selected-ns)) (warn (warn "No selected namespace!"))
      :else
        (-> db
            (update-in
             [:ir :files]
             (fn [files]
               (let [the-file (get files selected-ns)
                     ns-expr (:ns the-file)
                     new-file (update
                               the-file
                               :ns
                               (fn [expr]
                                 (let [name-field (key-nth (:data ns-expr) 1)]
                                   (assert
                                    (=
                                     selected-ns
                                     (get-in ns-expr [:data name-field :text]))
                                    (str "old namespace to change:" selected-ns " " ns-expr))
                                   (assoc-in expr [:data name-field :text] new-ns))))]
                 (assoc files new-ns new-file))))
            (assoc-in [:sessions sid :writer :selected-ns] new-ns)))))

(defn cp-ns [db op-data session-id op-id op-time]
  (update-in
   db
   [:ir :files]
   (fn [files] (-> files (assoc (:to op-data) (get files (:from op-data)))))))

(defn delete-entry [db op-data session-id op-id op-time]
  (comment println "delete" op-data)
  (case (:kind op-data)
    :def
      (-> db
          (update-in
           [:ir :files (:ns op-data) :defs]
           (fn [defs] (dissoc defs (:extra op-data))))
          (update-in
           [:sessions session-id :writer]
           (fn [writer]
             (-> writer
                 (update :stack (fn [stack] (dissoc-idx stack (:pointer writer))))
                 (update :pointer dec)))))
    :ns
      (-> db
          (update-in [:ir :files] (fn [files] (dissoc files (:ns op-data))))
          (update-in
           [:sessions session-id :writer]
           (fn [writer]
             (-> writer
                 (update :stack (fn [stack] (dissoc-idx stack (:pointer writer))))
                 (update :pointer dec)))))
    (do (println "[warning] no entry to delete") db)))

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

(defn draft-expr [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        data-path (bookmark->path bookmark)
        user-id (get-in db [:sessions session-id :user-id])]
    (-> db (update-in data-path (fn [expr] (cirru->tree op-data user-id op-time))))))

(defn duplicate [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        target-expr (get-in db (bookmark->path bookmark))
        parent-path (bookmark->path (update bookmark :focus butlast))
        parent-expr (get-in db parent-path)
        next-id (key-after (:data parent-expr) (last (:focus bookmark)))]
    (-> db
        (update-in
         parent-path
         (fn [expr] (update expr :data (fn [data] (assoc data next-id target-expr)))))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id))))))

(defn expr-after [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        next-id (key-after (:data target-expr) (last (:focus bookmark)))
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :at op-time :by user-id)
        new-expr (-> schema/expr
                     (assoc :at op-time :by user-id)
                     (assoc-in [:data bisection/mid-id] new-leaf))]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id bisection/mid-id))))))

(defn expr-before [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        next-id (key-before (:data target-expr) (last (:focus bookmark)))
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :at op-time :by user-id)
        new-expr (-> schema/expr
                     (assoc :at op-time :by user-id)
                     (assoc-in [:data bisection/mid-id] new-leaf))]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id bisection/mid-id))))))

(defn expr-replace [db op-data session-id op-id op-time]
  (let [from (:from op-data)
        to (:to op-data)
        bookmark (:bookmark op-data)
        data-path (bookmark->path bookmark)]
    (update-in db data-path (fn [expr] (call-replace-expr expr from to)))))

(defn file-config [db op-data sid op-id op-time]
  (let [ns-text (get-in db [:sessions sid :writer :selected-ns])]
    (if (some? ns-text)
      (update-in db [:ir :files ns-text :configs] (fn [configs] (merge configs op-data)))
      db)))

(defn indent [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        data-path (bookmark->path bookmark)
        user-id (get-in db [:sessions session-id :user-id])
        new-expr (assoc schema/expr :at op-time :by user-id)]
    (-> db
        (update-in data-path (fn [node] (assoc-in new-expr [:data bisection/mid-id] node)))
        (update-in
         [:sessions session-id :writer :stack pointer :focus]
         (fn [focus]
           (if (empty? focus)
             [bisection/mid-id]
             (vec (concat (butlast focus) [(last focus) bisection/mid-id]))))))))

(defn leaf-after [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        next-id (key-after (:data target-expr) (last (:focus bookmark)))
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :at op-time :by user-id)]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-leaf)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id))))))

(defn leaf-before [db op-data session-id op-id op-time]
  (let [writer (to-writer db session-id)
        bookmark (to-bookmark writer)
        parent-bookmark (update bookmark :focus butlast)
        data-path (bookmark->path parent-bookmark)
        target-expr (get-in db data-path)
        next-id (key-before (:data target-expr) (last (:focus bookmark)))
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :at op-time :by user-id)]
    (-> db
        (update-in data-path (fn [expr] (assoc-in expr [:data next-id] new-leaf)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj (vec (butlast focus)) next-id))))))

(defn mv-ns [db op-data session-id op-id op-time]
  (update-in
   db
   [:ir :files]
   (fn [files]
     (-> files (dissoc (:from op-data)) (assoc (:to op-data) (get files (:from op-data)))))))

(defn prepend-leaf [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        {stack :stack, pointer :pointer} writer
        bookmark (get stack pointer)
        focus (:focus bookmark)
        user-id (get-in db [:sessions session-id :user-id])
        new-leaf (assoc schema/leaf :by user-id :at op-time)
        expr-path (bookmark->path bookmark)
        target-expr (get-in db expr-path)
        new-id (key-prepend (:data target-expr))]
    (-> db
        (update-in
         expr-path
         (fn [expr] (if (expr? expr) (assoc-in expr [:data new-id] new-leaf) expr)))
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (conj focus new-id))))))

(defn remove-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])]
    (update-in db [:ir :files selected-ns :defs] (fn [defs] (dissoc defs op-data)))))

(defn remove-ns [db op-data session-id op-id op-time]
  (-> db
      (update-in [:ir :files] (fn [files] (dissoc files op-data)))
      (update-in
       [:sessions session-id :writer :selected-ns]
       (fn [x] (if (= x op-data) nil x)))))

(defn rename [db op-data session-id op-id op-time]
  (let [{kind :kind, ns-info :ns, extra-info :extra} op-data
        idx (get-in db [:sessions session-id :writer :pointer])
        user-id (get-in db [:sessions session-id :user-id])]
    (cond
      (= :ns kind)
        (let [{old-ns :from, new-ns :to} ns-info
              expr (get-in db [:ir :files old-ns :ns])
              next-id (key-nth (:data expr) 1)]
          (-> db
              (update-in
               [:ir :files]
               (fn [files] (-> files (dissoc old-ns) (assoc new-ns (get files old-ns)))))
              (assoc-in [:sessions session-id :writer :stack idx :ns] new-ns)
              (update-in [:ir :files new-ns :ns :data next-id :text] (fn [x] new-ns))))
      (= :def kind)
        (let [{old-ns :from, new-ns :to} ns-info
              {old-def :from, new-def :to} extra-info
              expr (get-in db [:ir :files old-ns :defs old-def])
              next-id (key-nth (:data expr) 1)
              files (get-in db [:ir :files])]
          (if (contains? files new-ns)
            (-> db
                (update-in
                 [:ir :files]
                 (fn [files]
                   (-> files
                       (update-in [old-ns :defs] (fn [file] (dissoc file old-def)))
                       (assoc-in
                        [new-ns :defs new-def]
                        (get-in files [old-ns :defs old-def])))))
                (update-in
                 [:sessions session-id :writer :stack idx]
                 (fn [bookmark] (-> bookmark (assoc :ns new-ns) (assoc :extra new-def))))
                (update-in
                 [:ir :files new-ns :defs new-def :data]
                 (fn [def-data]
                   (let [try-1 (:text (val-nth def-data 1))]
                     (if (and (string? try-1) (= "^" (first try-1)))
                       (assoc-nth def-data 2 (cirru->tree new-def user-id op-time))
                       (assoc-nth def-data 1 (cirru->tree new-def user-id op-time)))))))
            (-> db
                (update-in
                 [:sessions session-id :notifications]
                 (push-warning op-id op-time (str "no namespace: " new-ns))))))
      :else (do (println "Unexpected kind:" kind) db))))

(defn replace-file [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])
        ns-text (get-in db [:sessions sid :writer :draft-ns])]
    (if (some? ns-text)
      (assoc-in db [:ir :files ns-text] (cirru->file op-data user-id op-time))
      (do (println "undefined draft-ns") db))))

(defn reset-at [db op-data session-id op-id op-time]
  (let [saved-files (:saved-files db), old-file (get saved-files (:ns op-data))]
    (update-in
     db
     [:ir :files (:ns op-data)]
     (fn [file]
       (case (:kind op-data)
         :ns (assoc file :ns (:ns old-file))
         :proc (assoc file :proc (:proc old-file))
         :def
           (let [def-text (:extra op-data)]
             (assoc-in file [:defs def-text] (get-in old-file [:defs def-text])))
         (throw (js/Error. (str "Malformed data: " (pr-str op-data)))))))))

(defn reset-files [db op-data session-id op-id op-time]
  (assoc-in db [:ir :files] (:saved-files db)))

(defn reset-ns [db op-data session-id op-id op-time]
  (let [ns-text op-data]
    (assoc-in db [:ir :files ns-text] (get-in db [:saved-files ns-text]))))

(defn toggle-comment [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        bookmark (to-bookmark writer)
        data-path (bookmark->path bookmark)
        user-id (get-in db [:sessions sid :user-id])]
    (update-in
     db
     data-path
     (fn [node]
       (if (= :expr (:type node))
         (update
          node
          :data
          (fn [data]
            (let [k0 (apply min (keys data))]
              (if (and (some? k0) (= ";" (get-in data [k0 :text])))
                (dissoc data k0)
                (assoc-prepend data (cirru->tree ";" user-id op-time))))))
         (do (println "Toggle comment at wrong place," node) node))))))

(defn unindent [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        parent-bookmark (update bookmark :focus butlast)
        last-coord (last (:focus bookmark))
        parent-path (bookmark->path parent-bookmark)]
    (-> db
        (update-in
         [:sessions session-id :writer :stack (:pointer writer) :focus]
         (fn [focus] (vec (butlast focus))))
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

(defn update-leaf [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        data-path (bookmark->path bookmark)
        user-id (get-in db [:sessions session-id :user-id])]
    (-> db
        (update-in
         data-path
         (fn [leaf]
           (if (and (some? (:at op-data))
                    (some? (:text op-data))
                    (> (:at op-data) (:at leaf)))
             (assoc leaf :text (:text op-data) :at (:at op-data) :by user-id)
             (do (println "invalid updata op:" op-data) leaf)))))))
