
(ns app.comp.page-members
  (:require-macros [respo.macros :refer [defcomp <> span div a]])
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(def style-bookmark
  {:font-family "Menlo,monospace", :min-width 200, :display :inline-block})

(def style-name {:min-width 160, :display :inline-block})

(def style-page {:min-width 160, :display :inline-block})

(def style-members {:padding "0 16px"})

(def style-row {:cursor :pointer})

(defn on-watch [session-id]
  (fn [e d! m!] (d! :router/change {:name :watching, :data session-id})))

(defcomp
 comp-page-members
 (router-data session-id)
 (div
  {:style (merge ui/flex style-members)}
  (div
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
                    style-bookmark))))])))))))
