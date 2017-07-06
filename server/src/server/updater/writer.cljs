
(ns server.updater.writer (:require [server.util :refer [bookmark->path]]))

(defn edit [db op-data session-id op-id op-time]
  (let [ns-text (get-in db [:sessions session-id :writer :selected-ns])
        bookmark (assoc op-data :ns ns-text :focus [])]
    (-> db
        (update-in
         [:sessions session-id :writer]
         (fn [writer]
           (let [{stack :stack, pointer :pointer} writer]
             (assoc writer :stack (conj stack bookmark) :pointer (count stack)))))
        (assoc-in [:sessions session-id :router] {:name :editor}))))

(defn point-to [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :writer :pointer] op-data))

(defn focus [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])]
    (assoc-in db [:sessions session-id :writer :stack (:pointer writer) :focus] op-data)))

(defn go-up [db op-data session-id op-id op-time]
  (-> db
      (update-in
       [:sessions session-id :writer]
       (fn [writer]
         (update-in
          writer
          [:stack (:pointer writer) :focus]
          (fn [focus] (if (empty? focus) focus (vec (butlast focus)))))))))

(defn go-down [db op-data session-id op-id op-time]
  (let [writer (get-in db [:sessions session-id :writer])
        bookmark (get (:stack writer) (:pointer writer))
        target-expr (get-in db (bookmark->path bookmark))]
    (if (zero? (count (:data target-expr)))
      db
      (-> db
          (update-in
           [:sessions session-id :writer :stack (:pointer writer) :focus]
           (fn [focus] (conj focus (apply min (keys (:data target-expr))))))))))

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

(defn remove-idx [db op-data session-id op-id op-time]
  (-> db
      (update-in
       [:sessions session-id :writer :stack]
       (fn [stack] (vec (concat (take op-data stack) (drop (inc op-data) stack)))))))
