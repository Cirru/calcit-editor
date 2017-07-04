
(ns app.main
  (:require [respo.core :refer [render! clear-cache! realize-ssr!]]
            [respo.cursor :refer [mutate]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
            [app.network :refer [send! setup-socket!]]
            [app.schema :as schema]))

(def ssr? (some? (.querySelector js/document "meta.respo-ssr")))

(defonce *states (atom {}))

(defn dispatch! [op op-data]
  (.info js/console "Dispatch" (str op) (clj->js op-data))
  (if (= op :states) (reset! *states ((mutate op-data) @*states)) (send! op op-data)))

(defonce *store (atom nil))

(defn simulate-login! []
  (let [raw (.getItem js/localStorage (:storage-key schema/configs))]
    (if (some? raw)
      (do (println "Found storage.") (dispatch! :user/log-in (read-string raw)))
      (do (println "Found no storage.")))))

(def mount-target (.querySelector js/document ".app"))

(defn render-app! [renderer]
  (renderer mount-target (comp-container @*states @*store) dispatch!))

(defn main! []
  (if ssr? (render-app! realize-ssr!))
  (render-app! render!)
  (setup-socket!
   *store
   {:url (str "ws://" (.-hostname js/location) ":" (:port schema/configs)),
    :on-close! (fn [event] (reset! *store nil) (.error js/console "Lost connection!")),
    :on-open! (fn [event] (simulate-login!))})
  (add-watch *store :changes (fn [] (render-app! render!)))
  (add-watch *states :changes (fn [] (render-app! render!)))
  (println "App started!"))

(defn reload! [] (clear-cache!) (render-app! render!) (println "Code updated."))

(set! js/window.onload main!)
