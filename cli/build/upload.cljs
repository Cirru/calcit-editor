
(ns build.upload
  (:require ["child_process" :as cp]))

(def configs {:orgization "Cirru"
              :name "calcit-editor"
              :cdn "calcit-editor"})

(defn sh! [command]
  (println command)
  (println (.toString (cp/execSync command))))

(defn -main []
  (sh! (str "rsync -avr --progress dist/* tiye.me:cdn/" (:cdn configs)))
  (sh!
    (str "rsync -avr --progress dist/{index.html,manifest.json} tiye.me:repo/"
      (:orgization configs) "/"
      (:name configs) "/")))
