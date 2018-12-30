
(ns app.comp.profile
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.core :refer [defcomp <> span div button input a]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [app.config :as config]))

(defn on-input [e d! m!] (m! (:value e)))

(defn on-log-out [e dispatch!]
  (dispatch! :user/log-out nil)
  (.removeItem js/window.localStorage (:storage-key config/site)))

(defn on-rename [state]
  (fn [e d! m!] (let [name-text (string/trim state)] (d! :user/nickname name-text) (m! ""))))

(def style-greet
  {:font-family "Josefin Sans", :font-size 40, :font-weight 100, :color (hsl 0 0 100 0.8)})

(def style-id {:font-family "Josefin Sans", :font-weight 100, :color (hsl 0 0 60)})

(def style-profile {:padding "0 16px"})

(defcomp
 comp-profile
 (states user)
 (let [state (or (:data states) "")]
   (div
    {:style (merge ui/flex style-profile)}
    (div
     {}
     (<> span (str "Hello! " (:nickname user)) style-greet)
     (=< 8 nil)
     (<> span (str "id: " (:name user)) style-id))
    (div
     {}
     (input
      {:placeholder "A nickname", :value state, :style style/input, :on {:input on-input}})
     (=< 8 nil)
     (button {:inner-text "Change", :style style/button, :on {:click (on-rename state)}}))
    (=< nil 100)
    (div {} (button {:inner-text "Log out", :style style/button, :on {:click on-log-out}})))))
