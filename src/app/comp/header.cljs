
(ns app.comp.header
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div a]]
            [respo.comp.space :refer [=<]]
            [app.util.dom :refer [focus-search!]]
            [feather.core :refer [comp-icon]]
            [respo-alerts.core :refer [use-prompt]]))

(def style-entry
  {:cursor :pointer,
   :padding "0 12px",
   :color (hsl 0 0 100 0.6),
   :text-decoration :none,
   :vertical-align :middle})

(def style-highlight {:color (hsl 0 0 100)})

(defn render-entry [page-name this-page router-name on-click]
  (div
   {:on-click on-click,
    :style (merge style-entry (if (= this-page router-name) style-highlight))}
   (<> page-name nil)))

(def style-header
  {:height 32,
   :justify-content :space-between,
   :padding "0 16px",
   :font-size 16,
   :color :white,
   :border-bottom (str "1px solid " (hsl 0 0 100 0.2)),
   :font-family "Josefin Sans",
   :font-weight 300})

(def style-link {:font-size 14, :font-weight 100})

(defcomp
 comp-header
 (states router-name logged-in? stats)
 (let [broadcast-plugin (use-prompt (>> states :broadcast) {:text "Message to broadcast"})]
   (div
    {:style (merge ui/row-center style-header)}
    (div
     {:style ui/row-center}
     (render-entry
      "Files"
      :files
      router-name
      (fn [e d!] (d! :router/change {:name :files})))
     (render-entry
      "Editor"
      :editor
      router-name
      (fn [e d!] (d! :router/change {:name :editor})))
     (render-entry
      "Search"
      :search
      router-name
      (fn [e d!] (d! :router/change {:name :search}) (focus-search!)))
     (render-entry "REPL" :repl router-name (fn [e d!] (d! :router/change {:name :repl})))
     (render-entry
      (str "Members:" (:members-count stats))
      :members
      router-name
      (fn [e d!] (d! :router/change {:name :members})))
     (render-entry
      "Configs"
      :configs
      router-name
      (fn [e d!] (d! :router/change {:name :configs})))
     (a
      {:href "http://snippets.cirru.org", :target "_blank", :style style-entry}
      (<> "Snippets" style-link)
      (<> "↗" {:font-family ui/font-code}))
     (a
      {:href "https://github.com/Cirru/calcit-editor/wiki/Keyboard-Shortcuts",
       :target "_blank",
       :style style-entry}
      (<> "Shortcuts" style-link)
      (<> "↗" {:font-family ui/font-code})))
    (div
     {:style ui/row-middle}
     (comp-icon
      :radio
      {:font-size 18, :color (hsl 200 80 70 0.6), :cursor :pointer}
      (fn [e d!]
        ((:show broadcast-plugin)
         d!
         (fn [result] (if (some? result) (d! :notify/broadcast result))))))
     (=< 12 nil)
     (render-entry
      (if logged-in? "Profile" "Guest")
      :profile
      router-name
      (fn [e d!] (d! :router/change {:name :profile, :data nil, :router nil}))))
    (:ui broadcast-plugin))))
