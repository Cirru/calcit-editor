
(ns app.updater.router )

(defn change [db op-data session-id op-id op-time]
  (assoc-in db [:sessions session-id :router] op-data))
