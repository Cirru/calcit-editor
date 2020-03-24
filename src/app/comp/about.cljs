
(ns app.comp.about
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> span div pre input button img a br]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]
            [respo-md.comp.md :refer [comp-md-block]]
            [app.util.dom :refer [copy-silently!]]))

(def install-commands "npm install -g calcit-editor\ncalcit-editor\n")

(defcomp
 comp-about
 ()
 (div
  {:style (merge ui/global ui/fullscreen ui/column)}
  (div
   {:style (merge ui/flex ui/center)}
   (img
    {:src "//cdn.tiye.me/logo/cirru.png",
     :style {:width 64, :height 64, :border-radius "8px"}})
   (=< nil 16)
   (<>
    "No connection to server..."
    {:font-family "Josefin Sans", :font-weight 300, :font-size 24, :color (hsl 0 80 60)})
   (div
    {:class-name "comp-about"}
    (<> "Get editor server running with:")
    (pre
     {:innerHTML install-commands,
      :class-name "copy-commands",
      :style {:cursor :pointer, :padding "0 8px"},
      :title "Click to copy.",
      :on-click (fn [e d!] (copy-silently! install-commands))})))
  (div
   {:class-name "comp-about",
    :style (merge ui/center {:padding "8px 8px", :color (hsl 0 0 50)})}
   (comp-md-block
    "Calcit Editor is a syntax tree editor of [Cirru Project](http://cirru.org). Read more at [Calcit Editor](https://github.com/Cirru/calcit-editor).\n"
    {}))))
