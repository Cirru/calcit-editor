
(ns app.comp.repl-page
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp action-> <> span div a]]
            [respo.comp.space :refer [=<]]
            [app.util.dom :refer [focus-search!]]))

(defcomp comp-repl-page (states router) (div {:style {:padding "0 16px"}} (<> "REPL")))
