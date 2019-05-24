
(ns app.comp.about
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp cursor-> <> span div pre input button img a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [respo-md.comp.md :refer [comp-md-block]]))

(defcomp
 comp-about
 ()
 (div
  {:style ui/center}
  (img
   {:src "//cdn.tiye.me/logo/cirru.png",
    :style {:width 80, :height 80, :border-radius "8px"}})
  (=< nil 16)
  (<>
   span
   "Need connection..."
   {:font-family "Josefin Sans", :font-weight 100, :font-size 24, :color (hsl 0 80 60)})
  (div
   {:class-name "comp-about"}
   (comp-md-block
    "You are on this page because the server is not connected.\n\nYou may install editor server with:\n\n```bash\nnpm install -g calcit-editor\ncalcit-editor\n```\n\nThis is a syntax tree editor of [Cirru Project](http://cirru.org). Read more at [Calcit Editor](https://github.com/Cirru/calcit-editor).\n"
    {}))))
