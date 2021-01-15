
(ns app.updater.writer
  (:require [app.util :refer [bookmark->path to-writer to-bookmark push-info cirru->tree]]
            [app.util.stack :refer [push-bookmark]]
            [app.util.list :refer [dissoc-idx]]
            [app.schema :as schema]
            [app.util :refer [push-info]]
            [app.util :refer [stringify-s-expr]]))

(defn collapse [db op-data session-id op-id op-time]
  (-> db
      (update-in
       [:sessions session-id :writer]
       (fn [writer]
         (-> writer (update :stack (fn [stack] (subvec stack op-data))) (assoc :pointer 0))))))

(defn draft-ns [db op-data sid op-id op-time]
  (-> db (update-in [:sessions sid :writer] (fn [writer] (assoc writer :draft-ns op-data)))))

(defn edit [db op-data session-id op-id op-time]
  (let [ns-text (if (some? (:ns op-data))
                  (:ns op-data)
                  (do
                   (comment
                    "in old behavoir there's a default ns, could be misleading. need to be compatible..")
                   (get-in db [:sessions session-id :writer :selected-ns])))
        bookmark (assoc op-data :ns ns-text :focus [])]
    (-> db
        (update-in [:sessions session-id :writer] (push-bookmark bookmark))
        (assoc-in [:sessions session-id :router] {:name :editor}))))

(defn edit-ns [db op-data sid op-id op-time]
  (let [writer (to-writer db sid), bookmark (to-bookmark writer), ns-text (:ns bookmark)]
    (if (contains? #{:def :proc} (:kind bookmark))
      (-> db
          (update-in
           [:sessions sid :writer]
           (push-bookmark (assoc schema/bookmark :kind :ns :ns ns-text))))
      db)))

(defn finish [db op-data sid op-id op-time]
  (-> db
      (update-in
       [:sessions sid :writer]
       (fn [writer]
         (let [pointer (:pointer writer)]
           (-> writer
               (update
                :stack
                (fn [stack] (if (> (count stack) pointer) (dissoc-idx stack pointer) stack)))
               (assoc :pointer (if (pos? pointer) (dec pointer) pointer))))))))

(defn focus [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])]
    (assoc-in db [:sessions session-id :writer :stack (:pointer writer) :focus] op-data)))

(defn go-down [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        tail? (:tail? op-data)
        bookmark (get (:stack writer) (:pointer writer))
        target-expr (get-in db (bookmark->path bookmark))]
    (if (zero? (count (:data target-expr)))
      db
      (-> db
          (update-in
           [:sessions session-id :writer :stack (:pointer writer) :focus]
           (fn [focus] (conj focus (apply (if tail? max min) (keys (:data target-expr))))))))))

(defn go-left [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        parent-bookmark (update bookmark :focus butlast)
        parent-path (bookmark->path parent-bookmark)
        last-coord (last (:focus bookmark))
        base-expr (get-in db parent-path)
        child-keys (vec (sort (keys (:data base-expr))))
        idx (.indexOf child-keys last-coord)]
    (if (empty? (:focus bookmark))
      db
      (-> db
          (update-in
           [:sessions session-id :writer :stack (:pointer writer) :focus]
           (fn [focus]
             (conj
              (vec (butlast focus))
              (if (zero? idx) last-coord (get child-keys (dec idx))))))))))

(defn go-right [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        parent-bookmark (update bookmark :focus butlast)
        parent-path (bookmark->path parent-bookmark)
        last-coord (last (:focus bookmark))
        base-expr (get-in db parent-path)
        child-keys (vec (sort (keys (:data base-expr))))
        idx (.indexOf child-keys last-coord)]
    (if (empty? (:focus bookmark))
      db
      (-> db
          (update-in
           [:sessions session-id :writer :stack (:pointer writer) :focus]
           (fn [focus]
             (conj
              (vec (butlast focus))
              (if (= idx (dec (count child-keys))) last-coord (get child-keys (inc idx))))))))))

(defn go-up [db op-data session-id op-id op-time]
  (-> db
      (update-in
       [:sessions session-id :writer]
       (fn [writer]
         (update-in
          writer
          [:stack (:pointer writer) :focus]
          (fn [focus] (if (empty? focus) focus (vec (butlast focus)))))))))

(defn hide-peek [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :writer :peek-def] nil))

(defn move-next [db op-data sid op-id op-time]
  (-> db
      (update-in
       [:sessions sid :writer]
       (fn [writer]
         (let [pointer (:pointer writer)]
           (assoc
            writer
            :pointer
            (if (>= pointer (dec (count (:stack writer)))) pointer (inc pointer))))))))

(defn move-order [db op-data sid op-id op-time]
  (-> db
      (update-in
       [:sessions sid :writer]
       (fn [writer]
         (let [from-idx (:from op-data), to-idx (:to op-data)]
           (-> writer
               (update
                :pointer
                (fn [pointer]
                  (cond
                    (= pointer from-idx) to-idx
                    (or (< pointer (min from-idx to-idx)) (> pointer (max from-idx to-idx)))
                      pointer
                    :else (if (> from-idx to-idx) (inc pointer) (dec pointer)))))
               (update
                :stack
                (fn [stack]
                  (vec
                   (if (< from-idx to-idx)
                     (concat
                      (subvec stack 0 from-idx)
                      (subvec stack (inc from-idx) (inc to-idx))
                      (list (get stack from-idx))
                      (subvec stack (inc to-idx)))
                     (concat
                      (subvec stack 0 to-idx)
                      (list (get stack from-idx))
                      (subvec stack to-idx from-idx)
                      (if (>= (inc from-idx) (count stack))
                        (list)
                        (subvec stack (inc from-idx))))))))))))))

(defn move-previous [db op-data sid op-id op-time]
  (-> db
      (update-in
       [:sessions sid :writer]
       (fn [writer]
         (let [pointer (:pointer writer)]
           (assoc writer :pointer (if (pos? pointer) (dec pointer) 0)))))))

(defn paste [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        bookmark (to-bookmark writer)
        data-path (bookmark->path bookmark)
        user-id (get-in db [:sessions sid :user-id])]
    (if (vector? op-data)
      (-> db (assoc-in data-path (cirru->tree op-data user-id op-time)))
      db)))

(defn pick-node [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])
        writer (get-in db [:sessions sid :writer])
        bookmark (:picker-mode writer)
        data-path (bookmark->path bookmark)]
    (-> db
        (assoc-in data-path (cirru->tree op-data user-id op-time))
        (update-in [:sessions sid :writer] (fn [writer] (assoc writer :picker-mode nil)))
        (update-in
         [:sessions sid :notifications]
         (push-info
          op-id
          op-time
          (str
           "picked "
           (if (string? op-data)
             op-data
             (let [code (stringify-s-expr op-data)]
               (if (> (count code) 40) (str (subs code 0 40) "...") code)))))))))

(defn picker-mode [db op-data session-id op-id op-time]
  (update-in
   db
   [:sessions session-id :writer]
   (fn [writer]
     (if (some? (:picker-mode writer))
       (dissoc writer :picker-mode)
       (assoc writer :picker-mode (to-bookmark writer))))))

(defn point-to [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :writer :pointer] op-data))

(defn remove-idx [db op-data session-id op-id op-time]
  (-> db
      (update-in
       [:sessions session-id :writer]
       (fn [writer]
         (-> writer
             (update :stack (fn [stack] (dissoc-idx stack op-data)))
             (update
              :pointer
              (fn [pointer]
                (if (and (> pointer 0) (<= op-data pointer)) (dec pointer) pointer))))))))

(defn save-files [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])
        user-name (get-in db [:users user-id :name])]
    (-> db
        (update
         :saved-files
         (fn [saved-files]
           (if (some? op-data)
             (let [target (get-in db [:ir :files op-data])]
               (if (some? target)
                 (assoc saved-files op-data target)
                 (dissoc saved-files op-data)))
             (get-in db [:ir :files]))))
        (update
         :sessions
         (fn [sessions]
           (->> sessions
                (map
                 (fn [[k session]]
                   [k
                    (update
                     session
                     :notifications
                     (push-info
                      op-id
                      op-time
                      (str
                       user-name
                       (if (some? op-data)
                         (str " modified ns " op-data "!")
                         " saved files!"))))]))
                (into {})))))))

(defn select [db op-data session-id op-id op-time]
  (let [bookmark (assoc op-data :focus [])]
    (-> db
        (update-in [:sessions session-id :writer] (push-bookmark bookmark))
        (assoc-in [:sessions session-id :router] {:name :editor}))))
