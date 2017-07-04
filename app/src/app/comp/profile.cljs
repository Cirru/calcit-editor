
(ns app.comp.profile
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(defn on-log-out [e dispatch!]
  (dispatch! :user/log-out nil)
  (.removeItem js/localStorage (:storage-key schema/configs)))

(def style-trigger
  {:font-size 14,
   :cursor :pointer,
   :background-color colors/motif-light,
   :color :white,
   :padding "0 8px"})

(defcomp
 comp-profile
 (user)
 (div
  {:style ui/flex}
  (<> span (str "Hello! " (:name user)) nil)
  (=< 8 nil)
  (a {:style style-trigger, :on {:click on-log-out}} (<> span "Log out" nil))))
