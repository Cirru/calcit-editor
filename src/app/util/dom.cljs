
(ns app.util.dom
  (:require [respo.core :refer [style]]
            [respo.render.html :refer [style->string]]
            [cumulo-util.core :refer [delay!]]))

(defn copy-silently! [x]
  (-> js/navigator
      .-clipboard
      (.writeText x)
      (.then (fn [] (println "Copied.")))
      (.catch (fn [error] (.error js/console "Failed to copy:" error)))))

(defn do-copy-logics! [d! x message]
  (-> js/navigator
      .-clipboard
      (.writeText x)
      (.then (fn [] (d! :notify/push-message [:info message])))
      (.catch
       (fn [error]
         (.error js/console "Failed to copy:" error)
         (d! :notify/push-message [:error (str "Failed to copy! " error)])))))

(defn focus! []
  (js/requestAnimationFrame
   (fn [timestamp]
     (let [current-focused (.-activeElement js/document)
           cirru-focused (.querySelector js/document ".cirru-focused")]
       (if (some? cirru-focused)
         (if (not= current-focused cirru-focused) (.focus cirru-focused))
         (println "[Editor] .cirru-focused not found" cirru-focused))))))

(defn focus-search! []
  (delay!
   0.2
   (fn [timestamp]
     (let [target (.querySelector js/document ".search-input")]
       (if (some? target) (.focus target))))))

(defn inject-style [class-name styles]
  (style {:innerHTML (str class-name " {" (style->string styles) "}")}))
