
(ns server.twig.container
  (:require [recollect.bunch :refer [create-twig]]
            [server.twig.user :refer [twig-user]]
            [server.twig.page-files :refer [twig-page-files]]))

(def twig-container
  (create-twig
   :container
   (fn [db session]
     (let [logged-in? (some? (:user-id session)), router (:router session), ir (:ir db)]
       (if logged-in?
         {:session (dissoc session :router),
          :logged-in? true,
          :user (twig-user (get-in db [:users (:user-id session)])),
          :router (assoc
                   router
                   :data
                   (case (:name router)
                     :files
                       (twig-page-files (:files ir) (get-in session [:writer :selected-ns]))
                     nil))}
         {:session session, :logged-in? false})))))
