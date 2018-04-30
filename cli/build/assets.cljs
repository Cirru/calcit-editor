
(ns build.assets
  (:require ["child_process" :as cp]))

(defn sh! [command]
  (println command)
  (println (.toString (cp/execSync command))))

(defn -main []
  (sh! "mv dist/app/favored-fonts tmp")
  (sh! "rm -r dist/app")
  (sh! "cp -r ../app/dist dist/app")
  (sh! "mv tmp dist/app/favored-fonts"))
