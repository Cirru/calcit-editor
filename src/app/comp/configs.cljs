
(ns app.comp.configs
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div a pre code]]
            [respo.comp.space :refer [=<]]
            [cirru-edn.core :as cirru-edn]
            [respo-alerts.core :refer [use-prompt]]))

(def style-value {:cursor :pointer, :font-family ui/font-code, :color (hsl 200 90 80)})

(defcomp
 comp-configs
 (states configs)
 (let [version-plugin (use-prompt
                       (>> states :version)
                       {:text "Set a version:",
                        :initial (:version configs),
                        :placeholder "a version number...",
                        :input-style {:font-family ui/font-code}})
       modules-plugin (use-prompt
                       (>> states :modules)
                       {:text "Add modules:",
                        :initial (string/join " " (:modules configs)),
                        :placeholder "module/compact.cirru etc.",
                        :input-style {:font-family ui/font-code},
                        :multiline? true})]
   (div
    {:style (merge ui/expand ui/column {:padding "0 16px"})}
    (=< nil 8)
    (div
     {}
     (<> "Version:" {:font-family ui/font-fancy})
     (=< 8 nil)
     (span
      {:on-click (fn [e d!]
         ((:show version-plugin) d! (fn [text] (d! :configs/update {:version text}))))}
      (<> (if (string/blank? (:version configs)) "-" (:version configs)) style-value)))
    (div
     {}
     (<> "Modules:" {:font-family ui/font-fancy})
     (=< 8 nil)
     (span
      {:on-click (fn [e d!]
         ((:show modules-plugin)
          d!
          (fn [text]
            (d!
             :configs/update
             {:modules (vec (remove string/blank? (string/split (string/trim text) " ")))}))))}
      (<>
       (let [content (string/join " " (:modules configs))]
         (if (string/blank? content) "-" content))
       style-value)))
    (pre
     {:style (merge {:max-width "100%", :overflow :auto, :color (hsl 0 0 60)})}
     (code {:innerHTML (string/trim (cirru-edn/write configs {:inline? false}))}))
    (:ui version-plugin)
    (:ui modules-plugin))))
