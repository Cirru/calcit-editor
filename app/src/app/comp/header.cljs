
(ns app.comp.header
  (:require-macros [respo.macros :refer [defcomp <> span div]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]))

(defn on-profile [e dispatch!]
  (dispatch! :router/change {:name :profile, :data nil, :router nil}))

(def style-logo {:cursor :pointer})

(def style-pointer {:cursor "pointer"})

(def style-header
  {:height 48,
   :background-color colors/motif,
   :justify-content :space-between,
   :padding "0 16px",
   :font-size 16,
   :color :white})

(defn on-home [e dispatch!]
  (dispatch! :router/change {:name :home, :data nil, :router nil}))

(defcomp
 comp-header
 (logged-in?)
 (div
  {:style (merge ui/row-center style-header)}
  (div {:on {:click on-home}, :style style-logo} (<> span "Cumulo" nil))
  (div
   {:style style-pointer, :on {:click on-profile}}
   (<> span (if logged-in? "Me" "Guest") nil))))
