
(ns build.release
  (:require [shadow.cljs.devtools.api :as api]
            [shadow.cljs.devtools.server :as server]
            [clojure.java.shell :refer [sh]]))

(defn -main []
  (println (sh "rm" "-rf" "dist/*"))
  (api/release :browser)
  (api/compile :ssr)
  (println (sh "node" "target/ssr.js"))
  (println (sh "cp" "entry/manifest.json" "dist/")))
