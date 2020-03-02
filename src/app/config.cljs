
(ns app.config (:require [app.schema :as schema]))

(def cdn?
  (cond
    (exists? js/window) false
    (exists? js/process) (= "true" js/process.env.cdn)
    :else false))

(def dev?
  (let [debug? (do ^boolean js/goog.DEBUG)]
    (if debug?
      (cond
        (exists? js/window) true
        (exists? js/process) (not= "true" js/process.env.release)
        :else true)
      false)))

(def site
  {:port nil,
   :title "Calcit Editor",
   :icon "https://cdn.tiye.me/logo/cirru.png",
   :dev-ui "http://localhost:8100/main-fonts.css",
   :release-ui "//cdn.tiye.me/favored-fonts/main-fonts.css",
   :cdn-url "https://cdn.tiye.me/calcit-editor/",
   :theme "#eeeeff",
   :storage-key "calcit-storage",
   :storage-file "calcit.cirru"})
