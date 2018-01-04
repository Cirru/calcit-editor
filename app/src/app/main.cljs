
(ns app.main
  (:require [respo.core :refer [render! clear-cache! realize-ssr! *changes-logger]]
            [respo.cursor :refer [mutate]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
            [app.network :refer [send! setup-socket!]]
            [app.schema :as schema]
            [app.util :refer [ws-host parse-query!]]
            [app.util.dom :refer [focus!]]
            [app.util.shortcuts :refer [on-window-keydown]]
            [app.updater :as updater]))

(defonce *states (atom {}))

(defonce *store (atom nil))

(defn dispatch! [op op-data]
  (.info js/console "Dispatch" (str op) (clj->js op-data))
  (case op
    :states (reset! *states ((mutate op-data) @*states))
    :states/clear (reset! *states {})
    :manual-state/abstract (reset! *states (updater/abstract @*states))
    (send! op op-data)))

(defn detect-watching! []
  (let [query (parse-query!)]
    (if (some? (:watching query))
      (do (dispatch! :router/change {:name :watching, :data (:watching query)})))))

(defn simulate-login! []
  (let [raw (.getItem js/window.localStorage (:storage-key schema/configs))]
    (if (some? raw)
      (do (dispatch! :user/log-in (read-string raw)))
      (do (println "Found no storage.")))))

(defn connect []
  (.info js/console "Connecting...")
  (setup-socket!
   *store
   {:url ws-host,
    :on-close! (fn [event]
      (reset! *store nil)
      (.error js/console "Lost connection!")
      (dispatch! :states/clear nil)),
    :on-open! (fn [event] (simulate-login!) (detect-watching!))}))

(def mount-target (.querySelector js/document ".app"))

(defn render-app! [renderer]
  (renderer mount-target (comp-container @*states @*store) #(dispatch! %1 %2)))

(def ssr? (some? (.querySelector js/document "meta.respo-ssr")))

(defn main! []
  (if ssr? (render-app! realize-ssr!))
  (comment
   reset!
   *changes-logger
   (fn [global-element element changes] (println "Changes:" changes)))
  (render-app! render!)
  (connect)
  (add-watch
   *store
   :changes
   (fn [] (render-app! render!) (if (= :editor (get-in @*store [:router :name])) (focus!))))
  (add-watch *states :changes (fn [] (render-app! render!)))
  (.addEventListener js/window "keydown" (fn [event] (on-window-keydown event dispatch!)))
  (.addEventListener js/window "focus" (fn [event] (if (nil? @*store) (connect))))
  (println "App started!"))

(defn reload! [] (clear-cache!) (render-app! render!) (println "Code updated."))

(set! js/window.onload main!)
