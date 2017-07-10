
(ns server.twig.page-files
  (:require [recollect.bunch :refer [create-twig]] [clojure.set :refer [union]]))

(defn keys-set [x] (set (keys x)))

(defn compare-entry [new-x old-x]
  (cond
    (and (nil? old-x) (some? new-x)) :add
    (and (some? old-x) (nil? new-x)) :remove
    :else :changed))

(defn render-changed-files [files saved-files]
  (->> (union (keys-set files) (keys-set saved-files))
       (filter
        (fn [ns-text] (not (identical? (get files ns-text) (get saved-files ns-text)))))
       (map
        (fn [ns-text]
          (let [file (get files ns-text), saved-file (get saved-files ns-text)]
            [ns-text
             {:ns (compare-entry (:ns file) (:ns saved-file)),
              :proc (compare-entry (:proc file) (:proc saved-file)),
              :defs (let [all-defs (union
                                    (keys-set (:defs file))
                                    (keys-set (:defs saved-file)))
                          defs (:defs file)
                          saved-defs (:defs saved-file)]
                (->> all-defs
                     (filter
                      (fn [def-text] (not= (get defs def-text) (get saved-defs def-text))))
                     (map
                      (fn [def-text]
                        [def-text
                         (compare-entry (get defs def-text) (get saved-defs def-text))]))
                     (into {})))}])))
       (into {})))

(def twig-page-files
  (create-twig
   :page-files
   (fn [files selected-ns saved-files]
     {:ns-set (into #{} (keys files)),
      :defs-set (if (some? selected-ns)
        (do
         (println (get-in files [selected-ns :defs]))
         (->> (get-in files [selected-ns :defs]) (keys) (into #{})))
        #{}),
      :changed-files (render-changed-files files saved-files)})))
