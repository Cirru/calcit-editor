
(ns app.page
  (:require [respo.render.html :refer [make-string]]
            [shell-page.core :refer [make-page spit slurp]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]))

(def base-info
  {:title "Editor",
   :icon "//cdn.tiye.me/logo/cirru.png",
   :ssr nil,
   :inline-styles [(slurp "entry/main.css")]})

(defn dev-page []
  (make-page
   ""
   (merge base-info {:styles ["http://127.0.0.1:8100/main.css"], :scripts ["/client.js"]})))

(def local? (= "true" js/process.env.local))

(def preview? (= "preview" js/process.env.prod))

(defn prod-page []
  (let [html-content (make-string (comp-container {} nil))
        assets (read-string (slurp "dist/assets.edn"))
        cdn (if (or local? preview?) "" "//cdn.tiye.me/calcit-editor/")
        font-styles (if local?
                      "favored-fonts/main.css"
                      "//cdn.tiye.me/favored-fonts/main.css")
        prefix-cdn #(str cdn %)]
    (make-page
     html-content
     (merge
      base-info
      {:styles [font-styles], :scripts (map #(-> % :output-name prefix-cdn) assets)}))))

(defn main! []
  (if (= js/process.env.env "dev")
    (spit "target/index.html" (dev-page))
    (spit "dist/index.html" (prod-page))))
