
(ns app.comp.header
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.macros :refer [defcomp <> span div a]]
            [respo.comp.space :refer [=<]]
            [app.util.dom :refer [focus-search!]]))

(defn on-editor [e d! m!] (d! :router/change {:name :editor}))

(defn on-files [e dispatch! m!] (dispatch! :router/change {:name :files}))

(defn on-members [e d! m!] (d! :router/change {:name :members}))

(defn on-profile [e dispatch!]
  (dispatch! :router/change {:name :profile, :data nil, :router nil}))

(defn on-search [e d! m!] (d! :router/change {:name :search}) (focus-search!))

(def style-entry
  {:cursor :pointer, :padding-right 32, :color (hsl 0 0 100 0.6), :text-decoration :none})

(def style-highlight {:color (hsl 0 0 100)})

(defn render-entry [page-name this-page router-name on-click]
  (div
   {:on {:click on-click},
    :style (merge style-entry (if (= this-page router-name) style-highlight))}
   (<> span page-name nil)))

(def style-header
  {:height 40,
   :justify-content :space-between,
   :padding "0 16px",
   :font-size 18,
   :color :white,
   :border-bottom (str "1px solid " (hsl 0 0 100 0.2)),
   :font-family "Josefin Sans",
   :font-weight 100})

(defcomp
 comp-header
 (router-name logged-in?)
 (div
  {:style (merge ui/row-center style-header)}
  (div
   {:style ui/row-center}
   (render-entry "Files" :files router-name on-files)
   (render-entry "Editor" :editor router-name on-editor)
   (render-entry "Search" :search router-name on-search)
   (render-entry "Members" :members router-name on-members)
   (a
    {:inner-text "Shortcuts",
     :href "https://github.com/Cirru/cumulo-editor/wiki/Keyboard-Shortcuts",
     :target "_blank",
     :style style-entry}))
  (div {} (render-entry (if logged-in? "Profile" "Guest") :profile router-name on-profile))))
