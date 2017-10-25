
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> div span]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.header :refer [comp-header]]
            [app.comp.profile :refer [comp-profile]]
            [app.comp.login :refer [comp-login]]
            [app.comp.page-files :refer [comp-page-files]]
            [app.comp.page-editor :refer [comp-page-editor]]
            [app.comp.page-members :refer [comp-page-members]]
            [app.comp.search :refer [comp-search]]
            [app.comp.messages :refer [comp-messages]]
            [app.comp.watching :refer [comp-watching]]
            [app.comp.about :refer [comp-about]]))

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
 (let [state (:data states)
       session (:session store)
       writer (:writer session)
       router (:router store)]
   (if (nil? store)
     (div {:style (merge ui/global ui/fullscreen ui/center)} (comp-about))
     (div
      {:style (merge ui/global ui/fullscreen ui/column style-container)}
      (comp-header (:name router) (:logged-in? store))
      (div
       {:style (merge ui/row ui/flex style-body)}
       (if (:logged-in? store)
         (case (:name router)
           :profile (cursor-> :profile comp-profile states (:user store))
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
           :members (comp-page-members (:data router) (:id session))
           :search (cursor-> :search comp-search states (:data router))
           :watching (cursor-> :watching comp-watching states (:data router))
           (div {} (<> span (str "404 page: " (pr-str router)) nil)))
         (if (= :watching (:name router))
           (cursor-> :watching comp-watching states (:data router))
           (comp-login states))))
      (comment comp-inspect "Session" store style-inspector)
      (comment
       comp-inspect
       "Router data"
       (get-in store [:router :data])
       (merge style-inspector {:left 100}))
      (comp-messages (get-in store [:session :notifications]))))))
