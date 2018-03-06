
(ns build.main
  (:require [shadow.cljs.devtools.api :as shadow]))

(defn watch []
  (shadow/watch :app))

(defn build []
  (shadow/release :app))
