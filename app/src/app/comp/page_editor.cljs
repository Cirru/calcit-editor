
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a pre]])
  (:require [hsl.core :refer [hsl]]
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
            [app.util.dom :refer [inject-style]]))

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

(defcomp
 comp-page-editor
 (states stack router-data pointer)
 (let [state (if (boolean? (:data states)) (:data states) false)
       bookmark (get stack pointer)
       readonly? false]
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
           beginner? state]
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
                (let [[sid member] entry] [sid (<> span (:nickname member) style-watcher)]))))))
      (div
       {}
       (a
        {:inner-text "Shortcuts",
         :href "https://github.com/Cirru/stack-editor/wiki/Keyboard-Shortcuts",
         :target "_blank"})
       (=< 8 nil)
       (comp-beginner-mode state (on-toggle state cursor))))
     (comment comp-inspect "Expr" router-data style/inspector)))))
