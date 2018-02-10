
(ns server.repl (:require ["net" :as net]))

(defonce *repl-instance (atom nil))

(defn connect-socket-repl! [dispatch!]
  (let [client (.createConnection net (clj->js {:point 51053}))]
    (reset! *repl-instance client)
    (.on client "data" (fn [data] (dispatch! :repl/log (.toString data))))
    (.on client "exit" (fn [event] (reset! *repl-instance nil) (dispatch! :repl/exit)))
    (.on client "error" (fn [event] (dispatch! :repl/error (pr-str event))))))

(defn eval-tree! [db dispatch!] (println "eval tree"))

(defn send-raw-code! [code dispatch!]
  (let [client @*repl-instance] (if (some? client) (do (.write client (str code "\n"))))))

(defn try-cljs-repl! [dispatch!]
  (println "switching to cljs")
  (let [client @*repl-instance]
    (if (some? client)
      (do
       (println "try switching cljs...")
       (.write
        client
        (str
         "(shadow.cljs.devtools.cli/from-remote \"some-uuid\" \"another-uuid\" [\"cljs-repl\" \"browser\"])"
         "\n"))))))
