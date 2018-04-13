
(ns app.util.detect (:require ["net" :as net]))

(defn port-taken? [port next-fn]
  (let [tester (.createServer net)]
    (.. tester
        (once
         "error"
         (fn [err]
           (if (not= (.-code err) "EADDRINUSE") (next-fn err false) (next-fn nil true))))
        (once
         "listening"
         (fn [] (.. tester (once "close" (fn [] (next-fn nil false))) (close))))
        (listen port))))
