
(ns app.util.env
  (:require ["chalk" :as chalk]
            [app.util.detect :refer [port-taken?]]
            ["latest-version" :as latest-version]
            ["path" :as path]
            ["fs" :as fs]
            [applied-science.js-interop :as j])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn check-version! []
  (let [pkg (js/JSON.parse (fs/readFileSync (path/join js/__dirname "../package.json")))
        version (j/get pkg :version)
        pkg-name (j/get pkg :name)]
    (-> (latest-version pkg-name)
        (.then
         (fn [npm-version]
           (println
            (if (= version npm-version)
              (<< "Running latest version ~{version}")
              (chalk/yellow
               (<< "Update is available tagged ~{npm-version}, current one is ~{version}"))))))
        (.catch (fn [e] (println "failed to request version:" e))))))

(defn get-cli-configs! []
  (let [env js/process.env]
    {:compile? (= "compile" (j/get env :op)),
     :local-ui? (= "local" (j/get env :ui)),
     :compact? (= "true" (j/get env :compact))}))

(defn pick-port! [port next-fn]
  (port-taken?
   port
   (fn [err taken?]
     (if (some? err)
       (do (js/console.error err) (js/process.exit 1))
       (if taken?
         (do (println (<< "port ~{port} is in use.")) (pick-port! (inc port) next-fn))
         (do
          (let [link (chalk/blue (<< "http://calcit-editor.cirru.org?port=~{port}"))]
            (println (<< "port ~{port} is ok, please edit on ~{link}")))
          (next-fn port)))))))
