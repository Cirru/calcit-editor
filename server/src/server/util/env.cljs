
(ns server.util.env (:require ["chalk" :as chalk]))

(defn pick-configs [configs]
  (let [cli-port (if (some? (aget js/process.env "port"))
                   (js/parseInt (aget js/process.env "port")))
        cli-op (aget js/process.env "op")
        cli-extension (aget js/process.env "extension")
        result (-> configs
                   (update :port (fn [port] (or cli-port port)))
                   (update :op (fn [op] (or cli-op op)))
                   (update :extension (fn [extension] (or cli-extension extension))))]
    (comment println (.gray chalk result))
    result))
