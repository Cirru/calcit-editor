
(ns app.page
  (:require [respo.render.html :refer [make-string]]
            [shell-page.core :refer [make-page spit slurp]]
            [app.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
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
    {:styles [(<< "http://~(get-ip!):8100/main-fonts.css") "/entry/main.css"],
     :scripts [{:src "/client.js", :defer? true}],
     :inline-styles []})))

(defn prod-page []
  (let [html-content (make-string (comp-container {} nil))
        assets (read-string (slurp "dist/assets.edn"))
        cdn (if config/cdn? (:cdn-url config/site) "")
        font-styles (if config/cdn?
                      (:release-ui config/site)
                      "favored-fonts/main-fonts.css")
        prefix-cdn (fn [x] x)]
    (make-page
     html-content
     (merge
      base-info
      {:styles [font-styles],
       :scripts (map (fn [x] {:src (-> x :output-name prefix-cdn), :defer? true}) assets)}))))

(defn main! []
  (if (= js/process.env.release "true")
    (spit "dist/index.html" (prod-page))
    (spit "target/index.html" (dev-page))))
