
(ns server.util.env )

(defn pick-configs [configs]
  (let [cli-port (if (some? (.-port js/process.env)) (js/parseInt (.-port js/process.env)))
        cli-op (.-op js/process.env)
        result (-> configs
                   (update :port (fn [port] (or cli-port port)))
                   (update :op (fn [op] (or cli-op op))))]
    (println "Using configs:" result)
    result))
