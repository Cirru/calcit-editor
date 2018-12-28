
(ns app.page
  (:require [respo.render.html :refer [make-string]]
            [shell-page.core :refer [make-page spit slurp]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
            [app.schema :as schema]
            [app.config :as config]
            [cumulo-util.build :refer [get-ip!]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(def base-info
  {:title (:title config/site),
   :icon (:icon config/site),
   :ssr nil,
   :inline-styles [(slurp "entry/main.css")]})

(defn dev-page []
  (make-page
   ""
   (merge
    base-info
    {:styles [(<< "http://~(get-ip!):8100/main.css") "/entry/main.css"],
     :scripts ["/client.js"],
     :inline-styles []})))

(defn prod-page []
  (let [html-content (make-string (comp-container {} nil))
        assets (read-string (slurp "dist/assets.edn"))
        cdn (if config/cdn? (:cdn-url config/site) "")
        font-styles (if config/cdn?
                      "//cdn.tiye.me/favored-fonts/main.css"
                      "favored-fonts/main.css")
        prefix-cdn #(str cdn %)]
    (make-page
     html-content
     (merge
      base-info
      {:styles [(:release-ui config/site) font-styles],
       :scripts (map #(-> % :output-name prefix-cdn) assets)}))))

(defn main! []
  (if (= js/process.env.env "dev")
    (spit "target/index.html" (dev-page))
    (spit "dist/index.html" (prod-page))))
