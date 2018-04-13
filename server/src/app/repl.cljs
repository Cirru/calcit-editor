
(ns app.repl
  (:require ["net" :as net]
            [app.util :refer [bookmark->path tree->cirru push-warning]]
            [cirru-sepal.core :as sepal]))

(defonce *repl-instance (atom nil))

(defn connect-socket-repl! [port dispatch!]
  (println)
  (let [client (.createConnection
                net
                (clj->js {:port (js/parseInt port)})
                (fn [] (println "Socket REPL created!") (dispatch! :repl/start nil)))]
    (reset! *repl-instance client)
    (.on client "data" (fn [data] (dispatch! :repl/log (.toString data))))
    (.on
     client
     "end"
     (fn [event]
       (println "Socket REPL ended!")
       (reset! *repl-instance nil)
       (dispatch! :repl/exit)))
    (.on
     client
     "error"
     (fn [event] (.error js/console event) (dispatch! :repl/error (pr-str event))))))

(defn end-repl! [dispatch!]
  (let [client @*repl-instance] (if (some? client) (do (.end client)))))

(defn send-raw-code! [code dispatch!]
  (let [client @*repl-instance]
    (if (some? client)
      (do (.write client (str code "\n")))
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
    (if (some? client) (do (println repl-api) (.write client (str repl-api "\n"))))))
