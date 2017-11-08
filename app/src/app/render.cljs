
(ns app.render
  (:require [respo.render.html :refer [make-string]]
            [shell-page.core :refer [make-page spit slurp]]
            [app.comp.container :refer [comp-container]]))

(def base-info
  {:title "Editor",
   :icon "http://cdn.tiye.me/logo/cirru.png",
   :ssr nil,
   :inline-styles [(slurp "entry/main.css")]})

(defn dev-page []
  (make-page
   ""
   (merge
    base-info
    {:styles ["http://127.0.0.1:8100/main.css"],
     :scripts ["/browser/lib.js" "/browser/main.js"]})))

(def preview? (= "preview" js/process.env.prod))

(def local? (= "true" js/process.env.local))

(defn prod-page []
  (let [html-content (make-string (comp-container {} nil))
        cljs-info (.parse js/JSON (slurp "dist/cljs-manifest.json"))
        cdn (if (or local? preview?) "" "http://cdn.tiye.me/cumulo-editor/")
        font-styles (if local?
                      "favored-fonts/main.css"
                      "http://cdn.tiye.me/favored-fonts/main.css")]
    (make-page
     html-content
     (merge
      base-info
      {:styles [font-styles],
       :scripts [(str cdn (-> cljs-info (aget 0) (aget "js-name")))
                 (str cdn (-> cljs-info (aget 1) (aget "js-name")))]}))))

(defn main! []
  (if (= js/process.env.env "dev")
    (spit "target/index.html" (dev-page))
    (spit "dist/index.html" (prod-page))))
