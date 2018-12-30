
(ns app.util.env
  (:require ["chalk" :as chalk]
            [app.util.detect :refer [port-taken?]]
            ["latest-version" :as latest-version]
            ["path" :as path]
            ["fs" :as fs])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn check-version! []
  (let [pkg (js/JSON.parse (fs/readFileSync (path/join js/__dirname "../package.json")))
        version (.-version pkg)
        pkg-name (.-name pkg)]
    (.then
     (latest-version pkg-name)
     (fn [npm-version]
       (println
        (if (= version npm-version)
          (<< "Running latest version ~{version}")
          (chalk/yellow
           (<< "Update is available tagged ~{npm-version}, current one is ~{version}"))))))))

(defn get-cli-configs! []
  (let [env (.-env js/process)]
    {:compile? (= "compile" (.-op env)), :local-ui? (= "local" (.-ui env))}))

(defn pick-port! [port next-fn]
  (port-taken?
   port
   (fn [err taken?]
     (if (some? err)
       (do (.error js/console err) (.exit js/process 1))
       (if taken?
         (do (println "port" port "is in use.") (pick-port! (inc port) next-fn))
         (do
          (let [link (.blue chalk (<< "http://calcit-editor.cirru.org?port=~{port}"))]
            (println (<< "port ~{port} is ok, please edit on ~{link}")))
          (next-fn port)))))))
