
(ns app.comp.header
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> <> span div a]]
            [respo.comp.space :refer [=<]]
            [app.util.dom :refer [focus-search!]]
            [feather.core :refer [comp-i]]
            [respo-alerts.core :refer [comp-prompt]]))

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
  {:height 40,
   :justify-content :space-between,
   :padding "0 16px",
   :font-size 18,
   :color :white,
   :border-bottom (str "1px solid " (hsl 0 0 100 0.2)),
   :font-family "Josefin Sans",
   :font-weight 100})

(def style-link {:font-size 14, :font-weight 100})

(defcomp
 comp-header
 (states router-name logged-in? stats)
 (div
  {:style (merge ui/row-center style-header)}
  (div
   {:style ui/row-center}
   (render-entry "Files" :files router-name (fn [e d!] (d! :router/change {:name :files})))
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
   (comp-prompt
    (>> states :broadcast)
    {:trigger (comp-i :radio 18 (hsl 200 80 70 0.6)), :text "Message to broadcast"}
    (fn [result d!] (if (some? result) (d! :notify/broadcast result))))
   (=< 12 nil)
   (render-entry
    (if logged-in? "Profile" "Guest")
    :profile
    router-name
    (fn [e d!] (d! :router/change {:name :profile, :data nil, :router nil}))))))
