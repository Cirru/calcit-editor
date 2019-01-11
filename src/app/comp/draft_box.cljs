
(ns app.comp.draft-box
  (:require [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.core :refer [defcomp <> span div textarea pre button a]]
            [respo.comp.space :refer [=<]]
            [app.comp.modal :refer [comp-modal]]
            [app.style :as style]
            [app.client-util :refer [tree->cirru]]
            [fipp.edn :refer [pprint]]
            [keycode.core :as keycode]))

(defn on-submit [expr? text close-modal! close?]
  (fn [e d! m!]
    (if expr? (d! :ir/draft-expr (read-string text)) (d! :ir/update-leaf text))
    (if close? (do (m! nil) (close-modal! m!)))))

(defn on-wrong [close-modal!] (fn [e d! m!] (close-modal! m!)))

(def style-area
  {:background-color (hsl 0 0 100 0.2),
   :min-height 320,
   :line-height "1.6em",
   :min-width 800,
   :color :white,
   :font-family style/font-code,
   :font-size 14,
   :outline :none,
   :border :none,
   :padding 8,
   :vertical-align :top})

(def style-mode
  {:color (hsl 0 0 100 0.6),
   :background-color (hsl 300 50 50 0.6),
   :padding "0 8px",
   :font-size 12,
   :border-radius "4px"})

(def style-original {:max-height 240, :overflow :auto})

(def style-text
  {:font-family style/font-code,
   :color :white,
   :padding 8,
   :height 60,
   :display :block,
   :width "100%",
   :background-color (hsl 0 0 100 0.2),
   :outline :none,
   :border :none,
   :font-size 14,
   :min-width 800,
   :vetical-align :top})

(def style-toolbar {:justify-content :flex-end})

(def style-wrong
  {:color :red,
   :font-size 24,
   :font-weight 100,
   :font-family "Josefin Sans",
   :cursor :pointer})

(defcomp
 comp-draft-box
 (states expr focus close-modal!)
 (comp-modal
  (fn [m! d!] (m! %cursor nil) (close-modal! m! d!))
  (let [path (->> focus (mapcat (fn [x] [:data x])) (vec))
        node (get-in expr path)
        missing? (nil? node)]
    (if missing?
      (span
       {:style style-wrong,
        :inner-text "Does not edit expression!",
        :on {:click (on-wrong close-modal!)}})
      (let [expr? (= :expr (:type node))
            original-text (if expr? (pr-str (tree->cirru node)) (:text node))
            state (or (:data states) original-text)]
        (div
         {:style ui/column}
         (div
          {:style style-original}
          (if expr?
            (<> span "Cirru Mode" style-mode)
            (textarea {:value original-text, :spellcheck false, :style style-text})))
         (=< nil 8)
         (textarea
          {:style style-area,
           :value (if expr? (with-out-str (pprint (read-string state))) state),
           :class-name "el-draft-box",
           :on-input (fn [e d! m!] (m! (:value e))),
           :on-keydown (fn [e d! m!]
             (cond
               (= keycode/escape (:keycode e)) (close-modal! m! d!)
               (and (= keycode/s (:keycode e)) (.-metaKey (:event e)))
                 (do
                  (.preventDefault (:event e))
                  (if expr?
                    (d! :ir/draft-expr (read-string state))
                    (d! :ir/update-leaf state))
                  (m! nil)
                  (close-modal! m!))))})
         (=< nil 8)
         (div
          {:style (merge ui/row style-toolbar)}
          (button
           {:style style/button,
            :inner-text "Apply",
            :on-click (on-submit expr? state close-modal! false)})
          (button
           {:style style/button,
            :inner-text "Submit",
            :on-click (on-submit expr? state close-modal! true)}))))))))
