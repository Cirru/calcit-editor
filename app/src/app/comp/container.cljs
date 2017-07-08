
(ns app.comp.container
  (:require-macros [respo.macros :refer [defcomp cursor-> <> div span]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.header :refer [comp-header]]
            [app.comp.profile :refer [comp-profile]]
            [app.comp.login :refer [comp-login]]
            [respo-message.comp.msg-list :refer [comp-msg-list]]
            [app.comp.page-files :refer [comp-page-files]]
            [app.comp.page-editor :refer [comp-page-editor]]
            [app.comp.page-members :refer [comp-page-members]]))

(def style-alert {:font-family "Josefin Sans", :font-weight 100, :font-size 40})

(def style-body {:padding-top 16})

(def style-container {:background-color :black, :color :white})

(def style-inspector
  {:bottom 0,
   :left 0,
   :max-width "100%",
   :background-color (hsl 0 0 50),
   :color :black,
   :opacity 1})

(defcomp
 comp-container
 (states store)
 (let [state (:data states), session (:session store), writer (:writer session)]
   (if (nil? store)
     (div
      {:style (merge ui/global ui/fullscreen ui/center)}
      (<> span "No connection!" style-alert))
     (div
      {:style (merge ui/global ui/fullscreen ui/column style-container)}
      (comp-header (:logged-in? store))
      (div
       {:style (merge ui/row ui/flex style-body)}
       (if (:logged-in? store)
         (let [router (:router store)]
           (case (:name router)
             :profile (comp-profile (:user store))
             :files
               (cursor-> :files comp-page-files states (:selected-ns writer) (:data router))
             :editor
               (cursor->
                :editor
                comp-page-editor
                states
                (:stack writer)
                (:data router)
                (:pointer writer))
             :members (comp-page-members)
             (div {} (<> span (str "404 page: " (pr-str router)) nil))))
         (comp-login states)))
      (comp-inspect "Store" store style-inspector)
      (comp-msg-list (get-in store [:session :notifications]) :session/remove-notification)))))
