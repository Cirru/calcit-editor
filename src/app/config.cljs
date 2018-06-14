
(ns app.config (:require [app.util.env :refer [get-env!]] [app.schema :as schema]))

(def bundle-builds #{"release" "local-bundle"})

(def dev?
  (if (exists? js/window)
    (do ^boolean js/goog.DEBUG)
    (not (contains? bundle-builds (get-env! "mode")))))

(def site schema/configs)
