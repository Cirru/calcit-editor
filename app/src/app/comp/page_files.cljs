
(ns app.comp.page-files
  (:require-macros [respo.macros :refer [defcomp <> span div pre input button a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [app.style :as style]))

(def style-list {:width 200})

(defcomp
 comp-page-files
 (states files)
 (div
  {:style (merge ui/flex ui/row)}
  (div
   {:style style-list}
   (div {:style style/header} (<> span "Namespaces" nil))
   (div {} (<> pre files nil)))
  (div
   {:style ui/flex}
   (div
    {}
    (input {:value "demo", :placeholder "a namespace", :style style/input})
    (=< 8 nil)
    (button {:inner-text "Add ns", :style style/button}))
   (div {}))))
