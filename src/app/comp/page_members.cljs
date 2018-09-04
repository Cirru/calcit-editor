
(ns app.comp.page-members
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp <> list-> span div a]]
            [respo.comp.space :refer [=<]]))

(defn on-watch [session-id]
  (fn [e d! m!] (d! :router/change {:name :watching, :data session-id})))

(def style-bookmark
  {:font-family "Menlo,monospace", :min-width 200, :display :inline-block})

(def style-members {:padding "0 16px"})

(def style-name {:min-width 160, :display :inline-block})

(def style-page {:min-width 160, :display :inline-block})

(def style-row {:cursor :pointer})

(defcomp
 comp-page-members
 (router-data session-id)
 (div
  {:style (merge ui/flex style-members)}
  (list->
   :div
   {}
   (->> router-data
        (map
         (fn [entry]
           (let [[k member] entry
                 member-name (if (some? (:user member))
                               (get-in member [:user :nickname])
                               "Guest")]
             [k
              (div
               {:style style-row, :on {:click (on-watch k)}}
               (<> span (str member-name (if (= k session-id) " (yourself)")) style-name)
               (=< 32 nil)
               (<> span (:page member) style-page)
               (=< 32 nil)
               (let [bookmark (:bookmark member)]
                 (if (some? bookmark)
                   (<>
                    span
                    (str
                     (:kind bookmark)
                     " "
                     (:ns bookmark)
                     " "
                     (:extra bookmark)
                     " _"
                     (string/join "_" (:focus bookmark))
                     "_")
                    style-bookmark)))
               (=< 32 nil)
               (if (= k session-id)
                 (a
                  {:href (str (.-href js/location) "?watching=" k),
                   :target "_blank",
                   :style {:color (hsl 240 80 80)}}
                  (<> span "Watching url" nil))))])))))))
