
(ns app.theme.star-trail
  (:require [hsl.core :refer [hsl]]
            [clojure.string :as string]
            [respo-ui.core :as ui]
            [polyfill.core :refer [text-width*]]
            [app.style :as style]))

(def style-expr
  {:border-width "0 0 0px 1px",
   :border-style :solid,
   :border-color (hsl 0 0 100 0.3),
   :min-height 24,
   :outline :none,
   :padding-left 10,
   :font-family "Menlo,monospace",
   :font-size 14,
   :margin-bottom 4,
   :margin-right 2,
   :margin-left 12,
   :margin-top 0})

(defn base-style-expr [] style-expr)

(def style-leaf
  {:line-height "24px",
   :height 24,
   :margin "2px 2px",
   :padding "0px 4px",
   :background-color :transparent,
   :min-width 8,
   :color (hsl 200 14 60),
   :font-family style/font-code,
   :font-size 15,
   :vertical-align :baseline,
   :text-align :left,
   :border-width "1px 1px 1px 1px",
   :resize :none,
   :white-space :nowrap,
   :outline :none,
   :border :none})

(defn base-style-leaf [] style-leaf)

(def style-expr-simple
  {:display :inline-block,
   :border-width "0 0 1px 0",
   :min-width 32,
   :padding-left 11,
   :padding-right 11,
   :vertical-align :top})

(def style-expr-tail {:display :inline-block, :vertical-align :top, :padding-left 10})

(defn decide-expr-style [expr has-others? focused? tail? layout-mode length depth]
  (merge
   {}
   (if has-others? {:border-color (hsl 0 0 100 0.6)})
   (if focused? {:border-color (hsl 0 0 100 0.9)})
   (if (and (pos? length) (not tail?) (not= layout-mode :block)) style-expr-simple)
   (if tail? style-expr-tail)))

(def style-big {:border-right (str "16px solid " (hsl 0 0 30))})

(def style-highlight {:background-color (hsl 0 0 100 0.2)})

(def style-number {:color (hsl 0 70 40)})

(def style-partial {:border-right (str "8px solid " (hsl 0 0 30)), :padding-right 0})

(def style-space {:background-color (hsl 0 0 100 0.12)})

(defn decide-leaf-style [text focused? first? by-other?]
  (let [has-blank? (or (= text "") (string/includes? text " "))
        best-width (+
                    10
                    (text-width* text (:font-size style-leaf) (:font-family style-leaf)))
        max-width 240]
    (merge
     {:width (min best-width max-width)}
     (if first? {:color (hsl 40 85 60)})
     (if (string/starts-with? text ":") {:color (hsl 240 30 64)})
     (if (or (string/starts-with? text "|") (string/starts-with? text "\""))
       {:color (hsl 120 60 56)})
     (if (string/starts-with? text "#\"") {:color (hsl 300 60 56)})
     (if (or (= text "true") (= text "false")) {:color (hsl 250 50 60)})
     (if (= text "nil") {:color (hsl 310 60 40)})
     (if (> best-width max-width) style-partial)
     (if (string/includes? text "\n") style-big)
     (if (re-find (re-pattern "^-?\\d") text) style-number)
     (if has-blank? style-space)
     (if (or focused? by-other?) style-highlight))))
