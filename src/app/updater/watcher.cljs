
(ns app.updater.watcher )

(defn file-change [db op-data _ op-id op-time]
  (let [new-files (get-in op-data [:ir :files])]
    (if (= (get-in db [:ir :files]) (:saved-files db))
      (-> db (assoc :saved-files new-files) (assoc-in [:ir :files] new-files))
      (update
       db
       :saved-files
       (fn [old-files]
         (->> new-files
              (map
               (fn [entry]
                 (let [[ns-text file] entry
                       old-file (get old-files ns-text)
                       old-defs (:defs old-file)]
                   [ns-text
                    (if (= file old-file)
                      old-file
                      (-> file
                          (update
                           :ns
                           (fn [expr]
                             (let [old-expr (:ns old-file)]
                               (if (= expr old-expr) old-expr expr))))
                          (update
                           :proc
                           (fn [expr]
                             (let [old-expr (:proc old-file)]
                               (if (= expr old-expr) old-expr expr))))
                          (update
                           :defs
                           (fn [defs]
                             (->> defs
                                  (map
                                   (fn [entry]
                                     (let [[def-text expr] entry
                                           old-expr (get old-file def-text)]
                                       [def-text (if (= expr old-expr) old-expr expr)])))
                                  (into {}))))))])))
              (into {})))))))
