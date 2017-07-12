
(ns server.updater.analyze
  (:require [clojure.string :as string]
            [server.util
             :refer
             [bookmark->path
              to-writer
              to-bookmark
              parse-deps
              tree->cirru
              cirru->tree
              parse-def
              add-warning]]
            [server.util.stack :refer [push-bookmark]]
            [server.schema :as schema]))

(defn goto-def [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        pkg (get-in db [:ir :package])
        bookmark (to-bookmark writer)
        ns-text (:ns bookmark)
        ns-expr (tree->cirru (get-in db [:ir :files ns-text :ns]))
        deps-info (parse-deps (subvec ns-expr 2))
        def-info (parse-def (:text op-data))
        forced? (:forced? op-data)
        new-bookmark (if (and (contains? deps-info (:key def-info))
                              (=
                               (:method def-info)
                               (:method (get deps-info (:key def-info)))))
                       (let [rule (get deps-info (:key def-info))]
                         (merge
                          schema/bookmark
                          (if (= :refer (:method def-info))
                            {:kind :def, :ns (:ns rule), :extra (:key def-info)}
                            {:kind :def, :ns (:ns rule), :extra (:def def-info)})))
                       {:kind :def, :ns (:ns bookmark), :extra (:def def-info)})
        def-existed? (some?
                      (get-in
                       db
                       [:ir :files (:ns new-bookmark) :defs (:extra new-bookmark)]))
        user-id (get-in db [:sessions sid :user-id])]
    (comment println "deps" deps-info def-info new-bookmark def-existed?)
    (if (some? new-bookmark)
      (if (string/starts-with? (:ns new-bookmark) (str pkg "."))
        (if def-existed?
          (-> db (update-in [:sessions sid :writer] (push-bookmark new-bookmark)))
          (if forced?
            (-> db
                (assoc-in
                 [:ir :files (:ns new-bookmark) :defs (:extra new-bookmark)]
                 (cirru->tree ["defn" (:extra new-bookmark) []] user-id op-time))
                (update-in [:sessions sid :writer] (push-bookmark new-bookmark)))
            (-> db
                (update-in
                 [:sessions sid :notifications]
                 (add-warning
                  op-id
                  (str "Does not exist: " (:ns new-bookmark) " " (:extra new-bookmark)))))))
        (-> db
            (update-in
             [:sessions sid :notifications]
             (add-warning op-id (str "External dep:" (:ns new-bookmark))))))
      (-> db
          (update-in
           [:sessions sid :notifications]
           (add-warning op-id (str "Cannot locate:" def-info)))))))
