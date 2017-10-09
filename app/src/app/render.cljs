
(ns app.render
  (:require [respo.render.html :refer [make-string]]
            [shell-page.core :refer [make-page spit slurp]]
            [app.comp.container :refer [comp-container]]))

(def base-info {:title "Editor", :icon "http://logo.cumulo.org/cumulo.png", :ssr nil})

(defn dev-page []
  (make-page
   ""
   (merge
    base-info
    {:styles [],
     :scripts ["/main.js" "/browser/lib.js" "/browser/main.js"],
     :inline-html "<link rel=\"stylesheet\" href=\"http://127.0.0.1:8100/main.css\" />"})))

(def preview? (= "preview" js/process.env.prod))

(defn prod-page []
  (let [html-content (make-string (comp-container {} nil))
        webpack-info (.parse js/JSON (slurp "dist/webpack-manifest.json"))
        cljs-info (.parse js/JSON (slurp "dist/cljs-manifest.json"))
        cdn (if preview? "" "http://repo-cdn.b0.upaiyun.com/cumulo-editor/")]
    (make-page
     html-content
     (merge
      base-info
      {:styles [(str cdn (aget webpack-info "main.css"))],
       :scripts [(str cdn (-> cljs-info (aget 0) (aget "js-name")))
                 (str cdn (-> cljs-info (aget 1) (aget "js-name")))],
       :inline-html "<link rel=\"stylesheet\" href=\"http://repo-cdn.b0.upaiyun.com/favored-fonts/main.css\" />"}))))

(defn main! []
  (if (= js/process.env.env "dev")
    (spit "target/index.html" (dev-page))
    (spit "dist/index.html" (prod-page))))
