
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a pre]])
  (:require [hsl.core :refer [hsl]]
            [clojure.string :as string]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.bookmark :refer [comp-bookmark]]
            [app.comp.beginner-mode :refer [comp-beginner-mode on-toggle]]
            [app.comp.expr :refer [comp-expr style-expr]]
            [app.comp.leaf :refer [style-leaf]]
            [app.style :as style]
            [app.util.dom :refer [inject-style]]
            [app.comp.rename :refer [comp-rename]]))

(def style-status (merge ui/row {:justify-content :space-between, :padding "0 8px"}))

(def style-watchers (merge ui/row {:display :inline-block}))

(def style-nothing
  {:color (hsl 0 0 100 0.4), :padding "0 16px", :font-family "Josefin Sans"})

(def style-missing
  {:font-family "Josefin Sans", :color (hsl 10 60 50), :font-size 20, :font-weight 100})

(def ui-missing (div {:style style-missing} (<> span "Expression is missing!" nil)))

(def style-stack {:width 200, :overflow :auto, :padding-bottom 120})

(def style-hint {:color (hsl 0 0 100 0.6), :font-family "Josefin Sans"})

(def style-area {:overflow :auto, :padding-bottom 80, :padding-top 40, :flex 1})

(def style-container {:position :relative})

(def style-editor (merge ui/flex ui/column))

(def style-watcher {:color (hsl 0 0 100 0.7), :margin-left 8})

(def initial-state {:beginner? false, :renaming? false})

(defn on-rename [state]
  (fn [e d! m!]
    (m! (update state :renaming? not))
    (js/setTimeout (fn [] (let [el (.querySelector js/document ".el-rename")] (.focus el))))))

(def style-link
  {:font-family "Josefin Sans", :cursor :pointer, :font-size 14, :color (hsl 200 50 80)})

(defn on-delete [bookmark] (fn [e d! m!] (d! :ir/delete-entry (dissoc bookmark :focus))))

(defn render-status [router-data state *cursor* bookmark]
  (div
   {:style style-status}
   (div
    {}
    (<> span (str "Writers(" (count (:others router-data)) ")") style-hint)
    (div
     {:style style-watchers}
     (->> (:others router-data)
          (vals)
          (map (fn [info] [(:session-id info) (<> span (:nickname info) style-watcher)]))))
    (=< 16 nil)
    (<> span (str "Watchers(" (count (:watchers router-data)) ")") style-hint)
    (div
     {:style style-watchers}
     (->> (:watchers router-data)
          (map
           (fn [entry]
             (let [[sid member] entry] [sid (<> span (:nickname member) style-watcher)])))))
    (=< 16 nil)
    (span {:inner-text "Delete", :style style-link, :on {:click (on-delete bookmark)}})
    (=< 8 nil)
    (span {:inner-text "Rename", :style style-link, :on {:click (on-rename state)}}))
   (div
    {}
    (a
     {:inner-text "Shortcuts",
      :href "https://github.com/Cirru/stack-editor/wiki/Keyboard-Shortcuts",
      :target "_blank"})
    (=< 8 nil)
    (comp-beginner-mode state (on-toggle state *cursor*)))))

(defcomp
 comp-page-editor
 (states stack router-data pointer)
 (let [state (or (:data states) initial-state)
       bookmark (get stack pointer)
       readonly? false
       old-name (if (= :def (:kind bookmark))
                  (str (:ns bookmark) "/" (:extra bookmark))
                  (:ns bookmark))
       close-rename! (fn [mutate!] (mutate! *cursor* (assoc state :renaming? false)))]
   (div
    {:style (merge ui/row ui/flex style-container)}
    (div
     {:style style-stack}
     (if (empty? stack)
       (<> span "Nothing selected" style-nothing)
       (->> stack
            (map-indexed
             (fn [idx bookmark] [idx (comp-bookmark bookmark idx (= idx pointer))])))))
    (=< 8 nil)
    (div
     {:style style-editor}
     (let [others (->> (:others router-data) (vals) (map :focus) (into #{}))
           expr (:expr router-data)
           focus (:focus router-data)
           beginner? (:beginner? state)]
       (div
        {:style style-area}
        (inject-style ".cirru-expr" style-expr)
        (inject-style ".cirru-leaf" style-leaf)
        (if (some? expr)
          (cursor->
           (:id expr)
           comp-expr
           states
           expr
           focus
           []
           others
           false
           false
           beginner?
           readonly?)
          (if (not (empty? stack)) ui-missing))))
     (render-status router-data state *cursor* bookmark)
     (if (:renaming? state)
       (cursor-> :rename comp-rename states old-name close-rename! bookmark))
     (comment comp-inspect "Expr" router-data style/inspector)))))
