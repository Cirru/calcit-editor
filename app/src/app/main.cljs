
(ns app.main
  (:require [respo.core :refer [render! clear-cache! realize-ssr!]]
            [respo.cursor :refer [mutate]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
            [app.network :refer [send! setup-socket!]]
            [app.schema :as schema]
            [app.util :refer [ws-host parse-query!]]
            [app.util.dom :refer [focus!]]
            [app.util.shortcuts :refer [on-window-keydown]]))

(def ssr? (some? (.querySelector js/document "meta.respo-ssr")))

(defonce *states (atom {}))

(defn dispatch! [op op-data]
  (.info js/console "Dispatch" (str op) (clj->js op-data))
  (if (= op :states) (reset! *states ((mutate op-data) @*states)) (send! op op-data)))

(defonce *store (atom nil))

(defn simulate-login! []
  (let [raw (.getItem js/window.localStorage (:storage-key schema/configs))]
    (if (some? raw)
      (do (println "Found storage.") (dispatch! :user/log-in (read-string raw)))
      (do (println "Found no storage.")))))

(defn detect-watching! []
  (let [query (parse-query!)]
    (if (some? (:watching query))
      (do
       (println "say watching")
       (dispatch! :router/change {:name :watching, :data (:watching query)})))))

(def mount-target (.querySelector js/document ".app"))

(defn render-app! [renderer]
  (renderer mount-target (comp-container @*states @*store) dispatch!))

(defn main! []
  (if ssr? (render-app! realize-ssr!))
  (render-app! render!)
  (setup-socket!
   *store
   {:url ws-host,
    :on-close! (fn [event] (reset! *store nil) (.error js/console "Lost connection!")),
    :on-open! (fn [event] (simulate-login!) (detect-watching!))})
  (add-watch
   *store
   :changes
   (fn [] (render-app! render!) (if (= :editor (get-in @*store [:router :name])) (focus!))))
  (add-watch *states :changes (fn [] (render-app! render!)))
  (.addEventListener js/window "keydown" (fn [event] (on-window-keydown event dispatch!)))
  (println "App started!"))

(defn reload! [] (clear-cache!) (render-app! render!) (println "Code updated."))

(set! js/window.onload main!)
