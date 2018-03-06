
(ns build.main
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.java.shell :refer [sh]]))

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

