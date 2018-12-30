
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
  {:port 6001,
   :title "Calcit Editor",
   :icon "http://cdn.tiye.me/logo/cirru.png",
   :dev-ui "http://localhost:8100/main.css",
   :release-ui "http://cdn.tiye.me/favored-fonts/main.css",
   :cdn-url "http://cdn.tiye.me/calcit-editor/",
   :cdn-folder "tiye.me:cdn/calcit-editor",
   :upload-folder "tiye.me:repo/Cirru/calcit-editor/",
   :server-folder "tiye.me:servers/calcit-editor",
   :theme "#eeeeff",
   :storage-key "calcit-storage",
   :storage-file "calcit.edn"})
