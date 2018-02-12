
(ns build.main
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.java.shell :refer [sh]]))

(def configs {:orgization "Cirru"
              :name "calcit-editor"})

(defn sh! [command]
  (println command)
  (println (sh "bash" "-c" command)))

(defn watch []
  (shadow/watch :browser))

(defn build []
  (sh! "rm -rf dist/*")
  (shadow/release :browser)
  (shadow/compile :ssr)
  (sh! "node target/ssr.js")
  (sh! "cp entry/manifest.json dist/"))

(defn build-local []
  (sh! "rm -rf dist/*")
  (shadow/release :browser)
  (shadow/compile :ssr)
  (sh! "prod=preview node target/ssr.js")
  (sh! "cp entry/manifest.json dist/"))

(defn html []
  (shadow/compile :ssr)
  (sh! "env=dev node target/ssr.js")
  (sh! "cp entry/manifest.json target/"))

(defn upload []
  (sh! (str "rsync -avr --progress dist/* tiye.me:cdn/" (:name configs)))
  (sh!
    (str "rsync -avr --progress dist/{index.html,manifest.json} tiye.me:repo/"
      (:orgization configs) "/"
      (:name configs) "/")))
