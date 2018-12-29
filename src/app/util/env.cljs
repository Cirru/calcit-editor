
(ns app.util.env
  (:require ["chalk" :as chalk]
            [app.util.detect :refer [port-taken?]]
            ["latest-version" :as latest-version]))

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

(defn get-env! [property] (aget (.-env js/process) property))

(defn pick-configs [configs]
  (let [cli-port (if (some? (aget js/process.env "port"))
                   (js/parseInt (aget js/process.env "port") 10))
        cli-op (aget js/process.env "op")
        cli-extension (aget js/process.env "extension")
        result (-> configs
                   (update :port (fn [port] (or cli-port port)))
                   (update :op (fn [op] (or cli-op op)))
                   (update :extension (fn [extension] (or cli-extension extension))))]
    (comment println (.gray chalk result))
    result))

(defn pick-port! [port next-fn]
  (port-taken?
   port
   (fn [err taken?]
     (if (some? err)
       (do (.error js/console err) (.exit js/process 1))
       (if taken?
         (do (println "port" port "is in use.") (pick-port! (inc port) next-fn))
         (do
          (let [link (str "http://calcit-editor.cirru.org?port=" port)]
            (println "port" port "is ok, please edit on" (.blue chalk link)))
          (next-fn port)))))))
