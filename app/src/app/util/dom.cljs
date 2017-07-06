
(ns app.util.dom )

(defn focus! []
  (js/requestAnimationFrom
   (fn [timestamp]
     (let [current-focused (.-activeElement js/document)
           cirru-focused (.querySelector js/document ".cirru-focused")]
       (if (some? cirru-focused)
         (if (not= current-focused cirru-focused) (.focus (cirru-focused)))
         (println "[Editor] .cirru-focused not found"))))))
