
(ns app.repl
  (:require [app.util :refer [bookmark->path tree->cirru push-warning]]
            [cirru-sepal.core :as sepal]
            ["nrepl-client" :as nrepl-client]
            ["fs" :as fs]
            ["chalk" :as chalk])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defonce *repl-instance (atom nil))

(defonce *repl-session (atom nil))

(defn connect-nrepl! [port dispatch!]
  (println)
  (let [client (.connect nrepl-client (clj->js {:port port}))
        create-session! (fn []
                          (.clone
                           client
                           (fn [err result]
                             (let [data (js->clj result :keywordize-keys true)
                                   new-session (-> data first :new-session)]
                               (println "New session:" new-session)
                               (dispatch! :repl/log (str "New sessions: " new-session))
                               (reset! *repl-session new-session)))))
        on-end (fn []
                 (println "nREPL ended!")
                 (reset! *repl-instance nil)
                 (reset! *repl-session nil)
                 (dispatch! :repl/log "nREPL closed")
                 (dispatch! :repl/exit))
        on-error (fn [event]
                   (.error js/console event)
                   (dispatch! :repl/error (pr-str event)))
        on-connect (fn [error]
                     (if (some? error)
                       (do (js/console.log "error" error))
                       (do
                        (println "nREPL created!")
                        (dispatch! :repl/start nil)
                        (dispatch! :repl/log "nREPL started")
                        (create-session!))))]
    (reset! *repl-instance client)
    (.once client "connect" on-connect)
    (.on client "end" on-end)
    (.on client "error" on-error)))

(defn end-repl! [dispatch!]
  (let [client @*repl-instance]
    (if (some? client)
      (do
       (.close
        client
        (fn [err] (if (some? err) (js/console.log "Failed to close," err) (.end client))))))))

(defn on-eval-result [dispatch!]
  (fn [error result]
    (let [result (js->clj result :keywordize-keys true)
          log! (fn [x] (dispatch! :repl/log x))
          handle-data! (fn [data]
                         (cond
                           (some? (:err data))
                             (dispatch! :repl/error (str "Error " (:err data)))
                           (some? (:out data)) (dispatch! :repl/log (:out data))
                           (some? (:value data)) (dispatch! :repl/value (:value data))
                           :else (println "Unknown message:" (pr-str data))))]
      (if (= "done" (first (:status (last result))))
        (doseq [x (butlast result)] (handle-data! x))
        (println "Unknown state:" (pr-str (last result)))))))

(defn send-raw-code! [op-data dispatch!]
  (let [code (:code op-data), eval-ns (:ns op-data), client @*repl-instance]
    (if (some? client)
      (do (.eval client code eval-ns @*repl-session (on-eval-result dispatch!)))
      (dispatch! :notify/push-message [:warning "REPL not connected"]))))

(defn eval-tree! [db dispatch! sid]
  (let [writer (get-in db [:sessions sid :writer])
        bookmark (get (:stack writer) (:pointer writer))
        data-path (bookmark->path bookmark)
        cirru-piece (tree->cirru (get-in db data-path))
        code (sepal/make-string cirru-piece)]
    (println "code to eval:" code)
    (dispatch! :repl/log (str "eval code: " code))
    (send-raw-code! {:code (str code "\n"), :ns (:ns bookmark)} dispatch!)))

(defn load-nrepl! [d2!]
  (let [nrepl-file ".shadow-cljs/nrepl.port", config-path nrepl-file]
    (if (fs/existsSync config-path)
      (let [port-text (fs/readFileSync nrepl-file "utf8"), port (js/parseInt port-text 10)]
        (connect-nrepl! port d2!))
      (let [warning (<< "~{nrepl-file} not found!")]
        (d2! :notify/push-message [:warn warning])
        (println (chalk/red warning))))))

(defn try-cljs-repl! [dispatch! build-id]
  (let [client @*repl-instance, repl-api (<< "(shadow.cljs.devtools.api/repl ~{build-id})")]
    (if (some? client)
      (do
       (println repl-api)
       (.eval client repl-api "cljs.user" @*repl-session (on-eval-result dispatch!))))))
