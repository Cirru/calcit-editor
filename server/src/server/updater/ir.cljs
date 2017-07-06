
(ns server.updater.ir
  (:require [server.schema :as schema]
            [bisection-key.core :as bisection]
            [server.util :refer [expr? leaf? bookmark->path]]))

(defn add-ns [db op-data session-id op-id op-time]
  (assoc-in db [:ir :files op-data] schema/file))

(defn add-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])
        user-id (get-in db [:sessions session-id :user-id])]
    (assoc-in
     db
     [:ir :files selected-ns :defs op-data]
     (assoc schema/expr :time op-time :author user-id))))

(defn remove-def [db op-data session-id op-id op-time]
  (let [selected-ns (get-in db [:sessions session-id :writer :selected-ns])]
    (update-in db [:ir :files selected-ns :defs] (fn [defs] (dissoc defs op-data)))))

(defn delete-leaf [db op-data session-id op-id op-time]
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
