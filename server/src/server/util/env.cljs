
(ns server.util.env )

(defn pick-configs [configs]
  (let [cli-port (if (some? (aget js/process.env "port"))
                   (js/parseInt (aget js/process.env "port")))
        cli-op (aget js/process.env "op")
        result (-> configs
                   (update :port (fn [port] (or cli-port port)))
                   (update :op (fn [op] (or cli-op op))))]
    (println "Using configs:" result)
    result))
