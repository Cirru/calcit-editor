
(ns server.twig.container
  (:require [recollect.bunch :refer [create-twig]] [server.twig.user :refer [twig-user]]))

(def twig-container
  (create-twig
   :container
   (fn [db session]
     (let [logged-in? (some? (:user-id session)), router (:router session)]
       (if logged-in?
         {:session session,
          :logged-in? true,
          :user (twig-user (get-in db [:users (:user-id session)])),
          :router router,
          :ir (:ir db)}
         {:session session, :logged-in? false})))))
