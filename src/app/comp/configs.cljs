
(ns app.comp.configs
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div a pre code]]
            [respo.comp.space :refer [=<]]
            [cirru-edn.core :as cirru-edn]
            [respo-alerts.core :refer [use-prompt]]))

(defcomp
 comp-configs
 (states configs)
 (let [version-plugin (use-prompt
                       (>> states :version)
                       {:text "Set a version:",
                        :initial (:version configs),
                        :placeholder "a version number...",
                        :input-style {:font-family ui/font-code}})]
   (div
    {:style (merge ui/expand ui/column {:padding "0 16px"})}
    (=< nil 8)
    (div
     {:style (merge ui/expand)}
     (<> "Version:" {:font-family ui/font-fancy})
     (=< 8 nil)
     (span
      {:on-click (fn [e d!]
         ((:show version-plugin) d! (fn [text] (d! :configs/update {:version text}))))}
      (<>
       (if (string/blank? (:version configs)) "-" (:version configs))
       {:cursor :pointer, :font-family ui/font-code, :color (hsl 200 90 80)}))
     (pre
      {:style (merge {:max-width "100%", :overflow :auto, :color (hsl 0 0 60)})}
      (code {:innerHTML (string/trim (cirru-edn/write configs {:inline? false}))})))
    (:ui version-plugin))))
