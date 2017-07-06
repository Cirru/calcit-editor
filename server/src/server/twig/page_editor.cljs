
(ns server.twig.page-editor
  (:require [recollect.bunch :refer [create-twig]] [server.util :refer [same-buffer?]]))

(def twig-page-editor
  (create-twig
   :page-editor
   (fn [files sessions writer session-id]
     (let [pointer (:pointer writer), stack (:stack writer), bookmark (get stack pointer)]
       (if (some? bookmark)
         (let [ns-text (:ns bookmark)]
           {:focus (:focus bookmark),
            :others (dissoc
                     (->> sessions
                          (map
                           (fn [entry]
                             (let [writer (:writer (val entry))
                                   a-bookmark (get (:stack writer) (:pointer writer))]
                               [(key entry)
                                (if (same-buffer? bookmark a-bookmark)
                                  (:focus a-bookmark)
                                  nil)])))
                          (filter (fn [pair] (some? (last pair))))
                          (into {}))
                     session-id),
            :expr (case (:kind bookmark)
              :ns (get-in files [ns-text :ns])
              :proc (get-in files [ns-text :proc])
              :def (get-in files [ns-text :defs (:extra bookmark)])
              nil)})
         nil)))))
