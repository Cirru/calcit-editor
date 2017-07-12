
(ns app.comp.profile
  (:require-macros [respo.macros :refer [defcomp <> span div button a]])
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(defn on-log-out [e dispatch!]
  (dispatch! :user/log-out nil)
  (.removeItem js/window.localStorage (:storage-key schema/configs)))

(def style-profile {:padding "0 16px"})

(defcomp
 comp-profile
 (user)
 (div
  {:style (merge ui/flex style-profile)}
  (<> span (str "Hello! " (:name user)) nil)
  (=< 8 nil)
  (button {:inner-text "Log out", :style style/button, :on {:click on-log-out}})))
