
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> div span]]
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
            [app.comp.about :refer [comp-about]]
            [app.comp.repl-page :refer [comp-repl-page]]
            [app.comp.configs :refer [comp-configs]]
            [app.config :refer [dev?]]))

(def style-body {:padding-top 12})

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
       router (:router store)
       theme (get-in store [:user :theme])]
   (if (nil? store)
     (comp-about)
     (div
      {:style (merge ui/global ui/fullscreen ui/column style-container)}
      (comp-header (>> states :header) (:name router) (:logged-in? store) (:stats store))
      (div
       {:style (merge ui/row ui/expand style-body)}
       (if (:logged-in? store)
         (case (:name router)
           :profile (comp-profile (>> states :profile) (:user store))
           :files (comp-page-files (>> states :files) (:selected-ns writer) (:data router))
           :editor
             (comp-page-editor
              (>> states :editor)
              (:stack writer)
              (:data router)
              (:pointer writer)
              (some? (:picker-mode writer))
              theme)
           :members (comp-page-members (:data router) (:id session))
           :search (comp-search (>> states :search) (:data router))
           :watching (comp-watching (>> states :watching) (:data router) (:theme session))
           :repl (comp-repl-page (>> states :repl) router)
           :configs (comp-configs (>> states :configs) (:data router))
           (div {} (<> (str "404 page: " (pr-str router)))))
         (if (= :watching (:name router))
           (comp-watching (>> states :watching) (:data router) (:theme session))
           (comp-login (>> states :login)))))
      (when dev? (comp-inspect "Session" store style-inspector))
      (comment
       when
       dev?
       (comp-inspect "Router data" states (merge style-inspector {:left 100})))
      (comp-messages (get-in store [:session :notifications]))))))
