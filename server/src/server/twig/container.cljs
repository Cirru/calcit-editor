
(ns server.twig.container
  (:require [recollect.macros :refer [deftwig]]
            [server.twig.user :refer [twig-user]]
            [server.twig.page-files :refer [twig-page-files]]
            [server.twig.page-editor :refer [twig-page-editor]]
            [server.twig.page-members :refer [twig-page-members]]
            [server.twig.search :refer [twig-search]]
            [server.twig.watching :refer [twig-watching]]))

(deftwig
 twig-container
 (db session)
 (let [logged-in? (some? (:user-id session))
       router (:router session)
       writer (:writer session)
       ir (:ir db)]
   (if (or logged-in? (= :watching (:name router)))
     {:session (dissoc session :router),
      :logged-in? logged-in?,
      :user (if logged-in? (twig-user (get-in db [:users (:user-id session)]))),
      :router (assoc
               router
               :data
               (case (:name router)
                 :files
                   (twig-page-files
                    (:files ir)
                    (get-in session [:writer :selected-ns])
                    (:saved-files db))
                 :editor
                   (twig-page-editor
                    (:files ir)
                    (:sessions db)
                    (:users db)
                    writer
                    (:id session))
                 :members (twig-page-members (:sessions db) (:users db))
                 :search (twig-search (:files ir))
                 :watching
                   (let [sessions (:sessions db), his-sid (:data router)]
                     (if (contains? sessions his-sid)
                       (twig-watching
                        (get sessions his-sid)
                        (:id session)
                        (:files ir)
                        (:users db))
                       nil))
                 nil))}
     {:session session, :logged-in? false})))
