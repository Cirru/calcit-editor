
(ns app.util.dom )

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
   300))
