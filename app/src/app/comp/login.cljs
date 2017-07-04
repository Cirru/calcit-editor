
(ns app.comp.login
  (:require-macros [respo.macros :refer [defcomp <> div input button span]])
  (:require [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo-ui.style :as ui]
            [app.schema :as schema]))

(defn on-submit [username password signup?]
  (fn [e dispatch!]
    (dispatch! (if signup? :user/sign-up :user/log-in) [username password])
    (.setItem js/localStorage (:storage-key schema/configs) [username password])))

(defn on-input [state k] (fn [e dispatch! mutate!] (mutate! (assoc state k (:value e)))))

(def initial-state {:username "", :password ""})

(defcomp
 comp-login
 (states)
 (let [state (or (:data states) initial-state)]
   (div
    {}
    (div
     {:style {}}
     (div
      {}
      (input
       {:placeholder "Username",
        :value (:username state),
        :style ui/input,
        :on {:input (on-input state :username)}}))
     (=< nil 8)
     (div
      {}
      (input
       {:placeholder "Password",
        :value (:password state),
        :style ui/input,
        :on {:input (on-input state :password)}})))
    (=< nil 8)
    (div
     {:style ui/flex}
     (button
      {:inner-text "Sign up",
       :style (merge ui/button {:outline :none, :border :none}),
       :on {:click (on-submit (:username state) (:password state) true)}})
     (=< 8 nil)
     (button
      {:inner-text "Sign in",
       :style (merge ui/button {:outline :none, :border :none}),
       :on {:click (on-submit (:username state) (:password state) false)}})))))
