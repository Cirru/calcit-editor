
(ns server.repl
  (:require ["net" :as net]
            [server.util :refer [bookmark->path tree->cirru]]
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

(defn send-raw-code! [code dispatch!]
  (let [client @*repl-instance] (if (some? client) (do (.write client (str code "\n"))))))

(defn eval-tree! [db dispatch! sid]
  (let [writer (get-in db [:sessions sid :writer])
        bookmark (get (:stack writer) (:pointer writer))
        data-path (bookmark->path bookmark)
        cirru-piece (tree->cirru (get-in db data-path))
        code (if (string? cirru-piece)
               (str "(println " (pr-str (sepal/transform-x cirru-piece)) ")")
               (sepal/make-string cirru-piece))]
    (println "code to eval:" code)
    (send-raw-code! (str code "\n") dispatch!)))

(defn try-cljs-repl! [dispatch!]
  (let [client @*repl-instance
        private-api "(shadow.cljs.devtools.cli/from-remote \"ID-X\" \"ID-Y\" [\"cljs-repl\" \"browser\"])"]
    (if (some? client)
      (do
       (println "Call shadow-cljs api:" private-api)
       (.write client (str private-api "\n"))))))
