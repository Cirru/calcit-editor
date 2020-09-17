
(ns app.updater.analyze
  (:require [clojure.string :as string]
            [app.util
             :refer
             [bookmark->path
              to-writer
              to-bookmark
              parse-deps
              tree->cirru
              cirru->tree
              parse-def
              push-warning]]
            [app.util.stack :refer [push-bookmark]]
            [app.schema :as schema]))

(defn abstract-def [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        files (get-in db [:ir :files])
        bookmark (to-bookmark writer)
        ns-text (:ns bookmark)
        def-text op-data
        def-existed? (some? (get-in files [(:ns bookmark) :defs def-text]))
        user-id (get-in db [:sessions sid :user-id])
        new-bookmark (merge schema/bookmark {:ns ns-text, :kind :def, :extra def-text})]
    (if def-existed?
      (-> db
          (update-in
           [:sessions sid :notifications]
           (push-warning op-id op-time (str def-text " already defined!")))
          (update-in [:sessions sid :writer] (push-bookmark new-bookmark)))
      (let [target-path (->> (:focus bookmark) (mapcat (fn [x] [:data x])))
            target-expr (-> files
                            (get-in [ns-text :defs (:extra bookmark)])
                            (get-in target-path))]
        (-> db
            (update-in
             [:ir :files ns-text :defs]
             (fn [defs]
               (comment
                println
                target-path
                (cons def-text target-path)
                (tree->cirru target-expr)
                (keys defs))
               (-> defs
                   (assoc
                    def-text
                    (cirru->tree ["def" def-text (tree->cirru target-expr)] user-id op-time))
                   (assoc-in
                    (cons (:extra bookmark) target-path)
                    (cirru->tree def-text user-id op-time)))))
            (update-in [:sessions sid :writer] (push-bookmark new-bookmark)))))))

(defn goto-def [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        pkg (get-in db [:ir :package])
        bookmark (to-bookmark writer)
        ns-text (:ns bookmark)
        ns-expr (tree->cirru (get-in db [:ir :files ns-text :ns]))
        deps-info (parse-deps (subvec ns-expr 2))
        def-info (parse-def (:text op-data))
        forced? (:forced? op-data)
        new-bookmark (merge
                      schema/bookmark
                      {:focus ["r"]}
                      (if (and (contains? deps-info (:key def-info))
                               (=
                                (:method def-info)
                                (:method (get deps-info (:key def-info)))))
                        (let [rule (get deps-info (:key def-info))]
                          (if (= :refer (:method def-info))
                            {:kind :def, :ns (:ns rule), :extra (:key def-info)}
                            {:kind :def, :ns (:ns rule), :extra (:def def-info)}))
                        {:kind :def, :ns (:ns bookmark), :extra (:def def-info)}))
        def-existed? (some?
                      (get-in
                       db
                       [:ir :files (:ns new-bookmark) :defs (:extra new-bookmark)]))
        user-id (get-in db [:sessions sid :user-id])
        warn (fn [x]
               (-> db
                   (update-in [:sessions sid :notifications] (push-warning op-id op-time x))))]
    (comment println "deps" deps-info def-info new-bookmark def-existed?)
    (if (some? new-bookmark)
      (if (string/starts-with? (:ns new-bookmark) (str pkg "."))
        (if def-existed?
          (-> db (update-in [:sessions sid :writer] (push-bookmark new-bookmark true)))
          (if forced?
            (let [new-expr (if (vector? (:args op-data))
                             ["defn" (:extra new-bookmark) (vec (:args op-data))]
                             ["def" (:extra new-bookmark) []])]
              (-> db
                  (assoc-in
                   [:ir :files (:ns new-bookmark) :defs (:extra new-bookmark)]
                   (cirru->tree new-expr user-id op-time))
                  (update-in [:sessions sid :writer] (push-bookmark new-bookmark))))
            (warn (str "Does not exist: " (:ns new-bookmark) " " (:extra new-bookmark)))))
        (warn (str "From external ns: " (:ns new-bookmark))))
      (warn (str "Cannot locate: " def-info)))))

(defn peek-def [db op-data sid op-id op-time]
  (let [writer (to-writer db sid)
        pkg (get-in db [:ir :package])
        bookmark (to-bookmark writer)
        ns-text (:ns bookmark)
        ns-expr (tree->cirru (get-in db [:ir :files ns-text :ns]))
        deps-info (parse-deps (subvec ns-expr 2))
        def-info (parse-def op-data)
        new-bookmark (merge
                      schema/bookmark
                      (if (and (contains? deps-info (:key def-info))
                               (=
                                (:method def-info)
                                (:method (get deps-info (:key def-info)))))
                        (let [rule (get deps-info (:key def-info))]
                          (if (= :refer (:method def-info))
                            {:kind :def, :ns (:ns rule), :extra (:key def-info)}
                            {:kind :def, :ns (:ns rule), :extra (:def def-info)}))
                        {:kind :def, :ns (:ns bookmark), :extra (:def def-info)}))
        def-existed? (some?
                      (get-in
                       db
                       [:ir :files (:ns new-bookmark) :defs (:extra new-bookmark)]))
        user-id (get-in db [:sessions sid :user-id])
        warn (fn [x]
               (update-in db [:sessions sid :notifications] (push-warning op-id op-time x)))]
    (comment println "deps" deps-info def-info new-bookmark def-existed?)
    (if (some? new-bookmark)
      (if (string/starts-with? (:ns new-bookmark) (str pkg "."))
        (if def-existed?
          (-> db
              (assoc-in
               [:sessions sid :writer :peek-def]
               {:ns (:ns new-bookmark), :def (:extra new-bookmark)}))
          (warn (str "Does not exist: " (:ns new-bookmark) " " (:extra new-bookmark))))
        (warn (str "External dep:" (:ns new-bookmark))))
      (warn (str "Cannot locate:" def-info)))))
