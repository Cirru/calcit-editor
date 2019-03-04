
(ns app.repl
  (:require [app.util :refer [bookmark->path tree->cirru push-warning]]
            [cirru-sepal.core :as sepal]
            ["nrepl-client" :as nrepl-client]))

(defonce *repl-instance (atom nil))

(defonce *repl-session (atom nil))

(defn connect-socket-repl! [port dispatch!]
  (println)
  (let [client (.connect nrepl-client (clj->js {:port (js/parseInt port 10)}))
        create-session! (fn []
                          (.clone
                           client
                           (fn [err result]
                             (let [data (js->clj result :keywordize-keys true)
                                   new-session (-> data first :new-session)]
                               (println "New session:" new-session)
                               (reset! *repl-session new-session)))))
        on-end (fn []
                 (println "nREPL ended!")
                 (reset! *repl-instance nil)
                 (dispatch! :repl/exit))
        on-error (fn [event]
                   (.error js/console event)
                   (dispatch! :repl/error (pr-str event)))
        on-connect (fn [error]
                     (js/console.log "error" error)
                     (println "nREPL created!")
                     (dispatch! :repl/start nil)
                     (create-session!))]
    (reset! *repl-instance client)
    (.once client "connect" on-connect)
    (.on client "end" on-end)
    (.on client "error" on-error)))

(defn end-repl! [dispatch!]
  (let [client @*repl-instance]
    (if (some? client)
      (do (.close client (fn [err] (js/console.log "ending error") (.end client)))))))

(defn on-eval-result [error result] (println "eval result:" error result))

(defn send-raw-code! [code dispatch!]
  (let [client @*repl-instance]
    (if (some? client)
      (do (.eval client code "app.main" @*repl-session on-eval-result))
      (dispatch! :notify/push-message [:warning "REPL not connected"]))))

(defn eval-tree! [db dispatch! sid]
  (let [writer (get-in db [:sessions sid :writer])
        bookmark (get (:stack writer) (:pointer writer))
        data-path (bookmark->path bookmark)
        cirru-piece (tree->cirru (get-in db data-path))
        code (sepal/make-string ["println" cirru-piece])]
    (println "code to eval:" code)
    (send-raw-code! (str code "\n") dispatch!)))

(defn try-cljs-repl! [dispatch! build-id]
  (let [client @*repl-instance, repl-api (str "(shadow.cljs.devtools.api/repl " build-id ")")]
    (if (some? client)
      (do
       (println repl-api)
       (.eval client repl-api "cljs.user" @*repl-session on-eval-result)))))
