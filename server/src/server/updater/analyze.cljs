
(ns server.updater.analyze
  (:require [clojure.string :as string]
            [server.util
             :refer
             [bookmark->path to-writer to-bookmark parse-deps tree->cirru parse-def]]
            [server.util.stack :refer [push-bookmark]]
            [server.schema :as schema]))

(defn goto-def [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        pkg (get-in db [:ir :package])
        bookmark (to-bookmark writer)
        ns-text (:ns bookmark)
        ns-expr (tree->cirru (get-in db [:ir :files ns-text :ns]))
        deps-info (parse-deps (subvec ns-expr 2))
        def-info (parse-def op-data)
        bookmark (if (and (contains? deps-info (:key def-info))
                          (= (:method def-info) (:method (get deps-info (:key def-info)))))
                   (let [rule (get deps-info (:key def-info))]
                     (merge
                      schema/bookmark
                      (if (= :refer (:method def-info))
                        {:kind :def, :ns (:ns rule), :extra (:key def-info)}
                        {:kind :ns, :ns (:ns rule), :extra (:extra def-info)})))
                   nil)]
    (comment println "deps" deps-info def-info bookmark)
    (if (some? bookmark)
      (if (string/starts-with? (:ns bookmark) (str pkg "."))
        (-> db (update-in [:sessions sid :writer] (push-bookmark bookmark)))
        (-> db
            (update-in
             [:sessions sid :notifications]
             (fn [xs]
               (conj
                xs
                (merge
                 schema/notification
                 {:id op-id, :kind :attentive, :text (str "External dep:" (:ns bookmark))}))))))
      (-> db
          (update-in
           [:sessions sid :notifications]
           (fn [xs]
             (conj
              xs
              (merge
               schema/notification
               {:id op-id, :kind :attentive, :text (str "Cannot locate:" def-info)}))))))))
