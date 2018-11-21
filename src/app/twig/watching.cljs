
(ns app.twig.watching
  (:require [recollect.twig :refer [deftwig]]
            [app.util :refer [to-bookmark]]
            [app.twig.user :refer [twig-user]]))

(deftwig
 twig-watching
 (session my-sid files users)
 (let [writer (:writer session)
       bookmark (to-bookmark writer)
       self? (= my-sid (:id session))
       working? (some? bookmark)]
   {:member (twig-user (get users (:user-id session))),
    :bookmark bookmark,
    :router (:router session),
    :self? self?,
    :working? (and working? (not self?)),
    :focus (:focus bookmark),
    :expr (if working?
      (let [path (if (= :def (:kind bookmark))
                   [(:ns bookmark) :defs (:extra bookmark)]
                   [(:ns bookmark) (:kind bookmark)])]
        (get-in files path)))}))
