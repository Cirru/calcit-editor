
(ns build.assets
  (:require [clojure.java.shell :refer [sh]]))

(defn sh! [command]
  (println command)
  (println (sh "bash" "-c" command)))

(defn -main []
  (sh! "mv dist/app/favored-fonts tmp")
  (sh! "rm -r dist/app")
  (sh! "cp -r ../app/dist dist/app")
  (sh! "mv tmp dist/app/favored-fonts")
  (shutdown-agents))
