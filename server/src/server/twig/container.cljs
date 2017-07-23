
(ns server.twig.container
  (:require [recollect.bunch :refer [create-twig]]
            [server.twig.user :refer [twig-user]]
            [server.twig.page-files :refer [twig-page-files]]
            [server.twig.page-editor :refer [twig-page-editor]]
            [server.twig.page-members :refer [twig-page-members]]
            [server.twig.search :refer [twig-search]]
            [server.twig.watching :refer [twig-watching]]))

(def twig-container
  (create-twig
   :container
   (fn [db session]
     (let [logged-in? (some? (:user-id session))
           router (:router session)
           writer (:writer session)
           ir (:ir db)]
       (if logged-in?
         {:session (dissoc session :router),
          :logged-in? true,
          :user (twig-user (get-in db [:users (:user-id session)])),
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
                       (twig-watching
                        (get-in db [:sessions (:data router)])
                        (:id session)
                        (:files ir)
                        (:users db))
                     nil))}
         {:session session, :logged-in? false})))))
