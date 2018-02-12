
(ns build.main
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.java.shell :refer [sh]]))

(def configs {:orgization "Cirru"
              :name "calcit-editor"})

(defn sh! [command]
  (println command)
  (println (sh "bash" "-c" command)))

(defn watch []
  (shadow/watch :app))

(defn build []
  (shadow/release :app))

(defn assets []
  (sh! "mv dist/app/favored-fonts tmp")
  (sh! "cp -r ../app/dist dist/app")
  (sh! "mv tmp dist/app/favored-fonts"))
