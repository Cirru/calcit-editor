
(ns app.updater.repl )

(defn clear-logs [db op-data session-id op-id op-time] (assoc-in db [:repl :logs] {}))

(defn log-value [db op-data session-id op-id op-time]
  (assoc-in db [:repl :logs op-id] {:id op-id, :type :value, :text op-data, :time op-time}))

(defn on-error [db op-data session-id op-id op-time]
  (assoc-in db [:repl :logs op-id] {:id op-id, :type :error, :text op-data, :time op-time}))

(defn on-exit [db op-data session-id op-id op-time]
  (assoc db :repl {:alive? false, :logs {}}))

(defn on-log [db op-data session-id op-id op-time]
  (assoc-in db [:repl :logs op-id] {:id op-id, :type :output, :text op-data, :time op-time}))

(defn on-start [db op-data session-id op-id op-time] (assoc-in db [:repl :alive?] true))
