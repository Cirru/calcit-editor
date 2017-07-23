
(ns app.util.dom
  (:require-macros [respo.macros :refer [style]])
  (:require [respo.core :refer [create-element]] [respo.render.html :refer [style->string]]))

(defn focus! []
  (js/requestAnimationFrame
   (fn [timestamp]
     (let [current-focused (.-activeElement js/document)
           cirru-focused (.querySelector js/document ".cirru-focused")]
       (if (some? cirru-focused)
         (if (not= current-focused cirru-focused) (.focus cirru-focused))
         (println "[Editor] .cirru-focused not found" cirru-focused))))))

(defn focus-search! []
  (js/setTimeout
   (fn [timestamp]
     (let [target (.querySelector js/document ".search-input")]
       (if (some? target) (.focus target))))
   200))

(defn inject-style [class-name styles]
  (style {:innerHTML (str class-name " {" (style->string styles) "}")}))
