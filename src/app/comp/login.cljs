
(ns app.comp.login
  (:require [respo.core :refer [defcomp <> div input button span]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo-ui.core :as ui]
            [app.schema :as schema]
            [app.style :as style]))

(def initial-state {:username "", :password ""})

(defn on-input [state k] (fn [e dispatch! mutate!] (mutate! (assoc state k (:value e)))))

(defn on-submit [username password signup?]
  (fn [e dispatch!]
    (dispatch! (if signup? :user/sign-up :user/log-in) [username password])
    (.setItem
     js/window.localStorage
     (:local-storage-key schema/configs)
     [username password])))

(def style-control (merge ui/flex {:text-align :right}))

(def style-login {:padding 16})

(defcomp
 comp-login
 (states)
 (let [state (or (:data states) initial-state)]
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
        :on {:input (on-input state :username)}}))
     (=< nil 8)
     (div
      {}
      (input
       {:placeholder "Password",
        :value (:password state),
        :style style/input,
        :on {:input (on-input state :password)}})))
    (=< nil 8)
    (div
     {:style style-control}
     (button
      {:inner-text "Sign up",
       :style style/button,
       :on {:click (on-submit (:username state) (:password state) true)}})
     (=< 8 nil)
     (button
      {:inner-text "Log in",
       :style style/button,
       :on {:click (on-submit (:username state) (:password state) false)}})))))
