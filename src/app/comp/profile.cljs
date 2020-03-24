
(ns app.comp.profile
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div button input a]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.config :as config]
            [feather.core :refer [comp-i comp-icon]]
            [respo-alerts.core :refer [comp-prompt]]))

(defn on-log-out [e dispatch!]
  (dispatch! :user/log-out nil)
  (.removeItem js/window.localStorage (:storage-key config/site)))

(def style-greet
  {:font-family "Josefin Sans", :font-size 40, :font-weight 100, :color (hsl 0 0 100 0.8)})

(def style-id {:font-family "Josefin Sans", :font-weight 100, :color (hsl 0 0 60)})

(def style-profile {:padding "0 16px"})

(defcomp
 comp-profile
 (states user)
 (div
  {:style (merge ui/flex style-profile)}
  (div
   {}
   (<> (str "Hello! " (:nickname user)) style-greet)
   (=< 4 nil)
   (comp-prompt
    (>> states :rename)
    {:trigger (comp-i :edit-2 14 (hsl 0 0 40)),
     :initial (:nickname user),
     :text "Pick a nickname:"}
    (fn [result d!] (d! :user/nickname (string/trim result))))
   (=< 8 nil)
   (<> (str "id: " (:name user)) style-id))
  (=< nil 80)
  (div {} (button {:inner-text "Log out", :style style/button, :on {:click on-log-out}}))))
