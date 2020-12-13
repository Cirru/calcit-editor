
(ns app.updater.configs )

(defn update-configs [db op-data session-id op-id op-time]
  (update db :configs (fn [configs] (merge configs op-data))))
