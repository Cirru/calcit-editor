
(ns app.comp.login
  (:require [respo.core :refer [defcomp >> <> div input button span]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo-ui.core :as ui]
            [app.style :as style]
            [app.config :as config]))

(def initial-state {:username "", :password ""})

(defn on-input [state cursor k]
  (fn [e dispatch!] (dispatch! cursor (assoc state k (:value e)))))

(defn on-submit [username password signup?]
  (fn [e dispatch!]
    (dispatch! (if signup? :user/sign-up :user/log-in) [username password])
    (.setItem js/window.localStorage (:storage-key config/site) [username password])))

(def style-control (merge ui/flex {:text-align :right}))

(def style-login {:padding 16})

(defcomp
 comp-login
 (states)
 (let [cursor (:cursor states), state (or (:data states) initial-state)]
   (div
    {:style style-login}
    (div
     {:style {}}
     (div
      {}
      (input
       {:placeholder "Username",
        :value (:username state),
        :style style/input,
        :on-input (on-input state cursor :username)}))
     (=< nil 8)
     (div
      {}
      (input
       {:placeholder "Password",
        :value (:password state),
        :style style/input,
        :on-input (on-input state cursor :password)})))
    (=< nil 8)
    (div
     {:style style-control}
     (button
      {:inner-text "Sign up",
       :style style/button,
       :on-click (on-submit (:username state) (:password state) true)})
     (=< 8 nil)
     (button
      {:inner-text "Log in",
       :style style/button,
       :on-click (on-submit (:username state) (:password state) false)})))))
