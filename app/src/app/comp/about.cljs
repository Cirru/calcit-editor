
(ns app.comp.about
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div pre input button a]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [respo-markdown.comp.md-article :refer [comp-md-article]]))

(defcomp
 comp-about
 ()
 (div
  {}
  (<>
   span
   "Editor server is not started!"
   {:font-family "Josefin Sans", :font-weight 100, :font-size 40, :color (hsl 0 80 60)})
  (div
   {:class-name "comp-about"}
   (comp-md-article
    "You are on this page because the server is not connected.\n\nYou may install editor server with:\n\n```bash\nnpm install -g calcit-editor\ncalcit-editor\n```\n\nThis is a syntax tree editor of [Cirru Project](http://cirru.org). Read more at [Cumulo Editor](https://github.com/Cirru/calcit-editor).\n"
    {}))))
