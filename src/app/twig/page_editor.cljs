
(ns app.twig.page-editor
  (:require [recollect.twig :refer [deftwig]]
            [app.util :refer [same-buffer? tree->cirru]]
            [app.twig.user :refer [twig-user]]
            [app.util.list :refer [compare-entry]]))

(defn pick-from-ns [ns-info]
  (let [var-names (set (keys (:defs ns-info)))
        rules (->> (tree->cirru (:ns ns-info))
                   (drop 2)
                   (mapcat
                    (fn [rule]
                      (if (and (vector? rule)
                               (contains? #{":require" ":require-macros"} (first rule)))
                        (rest rule)
                        nil))))
        import-names (->> rules
                          (mapcat
                           (fn [rule]
                             (filter
                              (fn [x] (not= x "[]"))
                              (if (string? (last rule)) (list (last rule)) (last rule)))))
                          set)]
    {:imported import-names, :defined var-names}))

(deftwig
 twig-page-editor
 (files old-files sessions users writer session-id repl)
 (let [pointer (:pointer writer), stack (:stack writer), bookmark (get stack pointer)]
   (if (some? bookmark)
     (let [ns-text (:ns bookmark)]
       {:focus (:focus bookmark),
        :others (dissoc
                 (->> sessions
                      (map
                       (fn [entry]
                         (let [session (val entry)
                               writer (:writer session)
                               router (:router session)
                               a-bookmark (get (:stack writer) (:pointer writer))]
                           [(key entry)
                            (if (and (= :editor (:name router))
                                     (same-buffer? bookmark a-bookmark))
                              {:focus (:focus a-bookmark),
                               :nickname (get-in users [(:user-id session) :nickname]),
                               :session-id (:id session)}
                              nil)])))
                      (filter (fn [pair] (some? (last pair))))
                      (into {}))
                 session-id),
        :watchers (->> sessions
                       (filter
                        (fn [entry]
                          (let [[k other-session] entry, router (:router other-session)]
                            (and (= :watching (:name router)) (= (:data router) session-id)))))
                       (map
                        (fn [entry]
                          (let [[k other-session] entry]
                            [k (twig-user (get users (:user-id other-session)))])))
                       (into {})),
        :expr (case (:kind bookmark)
          :ns (get-in files [ns-text :ns])
          :proc (get-in files [ns-text :proc])
          :def (get-in files [ns-text :defs (:extra bookmark)])),
        :peek-def (let [peek-def (:peek-def writer)]
          (if (some? peek-def) (get-in files [(:ns peek-def) :defs (:def peek-def)]) nil)),
        :picker-choices (if (some? (:picker-mode writer))
          (pick-from-ns (get files (:ns bookmark)))),
        :changed (let [file (get files ns-text), old-file (get old-files ns-text)]
          (case (:kind bookmark)
            :ns (compare-entry (:ns file) (:ns old-file))
            :proc (compare-entry (:proc file) (:proc old-file))
            :def
              (compare-entry
               (get (:defs file) (:extra bookmark))
               (get (:defs old-file) (:extra bookmark)))
            (do :unknown))),
        :repl repl})
     nil)))
