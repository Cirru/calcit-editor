
(ns app.client
  (:require [respo.core :refer [render! clear-cache! realize-ssr! *changes-logger]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
            [app.client-util :refer [ws-host parse-query!]]
            [app.util.dom :refer [focus!]]
            [app.util.shortcuts :refer [on-window-keydown]]
            [app.client-updater :as updater]
            [ws-edn.client :refer [ws-connect! ws-send! ws-connected?]]
            [recollect.patch :refer [patch-twig]]
            [cumulo-util.core :refer [delay!]]
            [app.config :as config]))

(defonce *connecting? (atom false))

(defonce *states (atom {:states {:cursor []}}))

(defonce *store (atom nil))

(defn send-op! [op op-data] (ws-send! {:kind :op, :op op, :data op-data}))

(defn dispatch! [op op-data]
  (when (and config/dev? (not= op :states))
    (.info js/console "Dispatch" (str op) (clj->js op-data)))
  (case op
    :states
      (reset!
       *states
       (let [[cursor new-state] op-data] (assoc-in @*states (conj cursor :data) new-state)))
    :states/clear (reset! *states {:states {:cursor []}})
    :manual-state/abstract (reset! *states (updater/abstract @*states))
    :manual-state/draft-box (reset! *states (updater/draft-box @*states))
    :effect/save-files
      (do (reset! *states (updater/clear-editor @*states)) (send-op! op op-data))
    :ir/indent (do (reset! *states (updater/clear-editor @*states)) (send-op! op op-data))
    :ir/unindent (do (reset! *states (updater/clear-editor @*states)) (send-op! op op-data))
    :ir/reset-files
      (do (reset! *states (updater/clear-editor @*states)) (send-op! op op-data))
    (send-op! op op-data)))

(defn detect-watching! []
  (let [query (parse-query!)]
    (when (some? (:watching query))
      (dispatch! :router/change {:name :watching, :data (:watching query)}))))

(defn heartbeat! []
  (delay!
   30
   (fn []
     (if (ws-connected?)
       (do (ws-send! {:kind :ping}) (heartbeat!))
       (println "Disabled heartbeat since connection lost.")))))

(defn simulate-login! []
  (let [raw (.getItem js/window.localStorage (:storage-key config/site))]
    (if (some? raw)
      (do (dispatch! :user/log-in (read-string raw)))
      (do (println "Found no storage.")))))

(defn connect! []
  (.info js/console "Connecting...")
  (reset! *connecting? true)
  (ws-connect!
   ws-host
   {:on-open (fn [] (simulate-login!) (detect-watching!) (heartbeat!)),
    :on-close (fn [event]
      (reset! *store nil)
      (reset! *connecting? false)
      (js/console.error "Lost connection!")
      (dispatch! :states/clear nil)),
    :on-data (fn [data]
      (case (:kind data)
        :patch
          (let [changes (:data data)]
            (when config/dev? (js/console.log "Changes" (clj->js changes)))
            (reset! *store (patch-twig @*store changes)))
        (println "unknown kind:" data)))}))

(def mount-target (.querySelector js/document ".app"))

(defn render-app! [renderer]
  (renderer mount-target (comp-container @*states @*store) #(dispatch! %1 %2)))

(defn retry-connect! [] (if (and (nil? @*store) (not @*connecting?)) (connect!)))

(def ssr? (some? (.querySelector js/document "meta.respo-ssr")))

(defn main! []
  (println "Running mode:" (if config/dev? "dev" "release"))
  (if ssr? (render-app! realize-ssr!))
  (comment
   reset!
   *changes-logger
   (fn [global-element element changes] (println "Changes:" changes)))
  (render-app! render!)
  (connect!)
  (add-watch
   *store
   :changes
   (fn [] (render-app! render!) (if (= :editor (get-in @*store [:router :name])) (focus!))))
  (add-watch *states :changes (fn [] (render-app! render!)))
  (.addEventListener
   js/window
   "keydown"
   (fn [event] (on-window-keydown event dispatch! (:router @*store))))
  (.addEventListener js/window "focus" (fn [event] (retry-connect!)))
  (.addEventListener
   js/window
   "visibilitychange"
   (fn [event] (when (= "visible" js/document.visibilityState) (retry-connect!))))
  (println "App started!"))

(defn reload! [] (clear-cache!) (render-app! render!) (println "Code updated."))
