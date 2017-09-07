
(ns server.util.env )

(defn pick-configs [configs]
  (let [cli-port (if (some? (aget js/process.env "port"))
                   (js/parseInt (aget js/process.env "port")))
        cli-op (aget js/process.env "op")
        cli-extension (aget js/process.env "extension")
        result (-> configs
                   (update :port (fn [port] (or cli-port port)))
                   (update :op (fn [op] (or cli-op op)))
                   (update :extension (fn [extension] (or cli-extension extension))))]
    (println "Using configs:" result)
    result))
