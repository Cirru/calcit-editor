
(ns server.twig.page-editor
  (:require [recollect.macros :refer [deftwig]]
            [server.util :refer [same-buffer?]]
            [server.twig.user :refer [twig-user]]))

(deftwig
 twig-page-editor
 (files sessions users writer session-id)
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
          :def (get-in files [ns-text :defs (:extra bookmark)])
          nil)})
     nil)))
