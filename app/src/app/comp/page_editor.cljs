
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(defcomp comp-page-editor (states) (div {:style ui/flex} (<> span "Editor" nil)))
