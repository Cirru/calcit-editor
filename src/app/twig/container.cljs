
(ns app.twig.container
  (:require [recollect.twig :refer [deftwig]]
            [app.twig.user :refer [twig-user]]
            [app.twig.page-files :refer [twig-page-files]]
            [app.twig.page-editor :refer [twig-page-editor]]
            [app.twig.page-members :refer [twig-page-members]]
            [app.twig.search :refer [twig-search]]
            [app.twig.watching :refer [twig-watching]]))

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
                    (:saved-files db)
                    (get-in session [:writer :draft-ns])
                    (:sessions db)
                    (:id session))
                 :editor
                   (twig-page-editor
                    (:files ir)
                    (:saved-files db)
                    (:sessions db)
                    (:users db)
                    writer
                    (:id session)
                    (:repl db))
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
                 :repl (:repl db)
                 :configs (:configs db)
                 {})),
      :stats {:members-count (count (:sessions db))}}
     {:session session, :logged-in? false, :stats {:members-count 0}})))
