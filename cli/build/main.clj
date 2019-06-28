
(ns build.main
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.java.shell :refer [sh]]))

(defn sh! [command]
  (println command)
  (println (:out (sh "bash" "-c" command))))

(defn build-cdn []
  (sh! "rm -rf dist/*")
  (shadow/release :client)
  (shadow/compile :page)
  (sh! "release=true cdn=true node target/page.js")
  (sh! "cp entry/manifest.json dist/"))

(defn build []
  (sh! "rm -rf dist/*")
  (shadow/release :client)
  (shadow/release :server)
  (shadow/compile :page)
  (sh! "release=true node target/page.js")
  (sh! "cp -r entry/favored-fonts dist/")
  (sh! "cp entry/manifest.json dist/"))

(defn page []
  (shadow/compile :page)
  (sh! "node target/page.js")
  (sh! "cp entry/manifest.json target/"))
