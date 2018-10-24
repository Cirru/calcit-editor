
(ns app.comp.watching
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros :refer [defcomp cursor-> <> span div input pre a]]
            [respo.comp.space :refer [=<]]
            [keycode.core :as keycode]
            [app.client-util :as util]
            [app.style :as style]
            [app.comp.expr :refer [comp-expr]]
            [app.theme.star-trail :refer [base-style-leaf base-style-expr]]
            [app.util.dom :refer [inject-style]]))

(def style-container {:padding "0 16px"})

(def style-tip
  {:font-family "Josefin Sans",
   :background-color (hsl 0 0 100 0.3),
   :border-radius "4px",
   :padding "4px 8px"})

(def style-title {:font-family "Josefin Sans"})

(defcomp
 comp-watching
 (states router-data theme)
 (let [expr (:expr router-data)
       focus (:focus router-data)
       bookmark (:bookmark router-data)
       others {}
       member-name (get-in router-data [:member :nickname])
       readonly? true]
   (if (nil? router-data)
     (div {:style style-container} (<> span "Session is missing!" nil))
     (if (:self? router-data)
       (div {:style style-container} (<> span "Watching at yourself :)" style-title))
       (div
        {:style style-container}
        (div
         {}
         (<> span "Watching mode" style-tip)
         (=< 16 nil)
         (<> span member-name nil)
         (=< 16 nil)
         (<> span (:kind bookmark) nil)
         (=< 16 nil)
         (<> span (str (:ns bookmark) "/" (:extra bookmark)) nil))
        (=< nil 16)
        (if (:working? router-data)
          (div
           {}
           (inject-style ".cirru-expr" (base-style-expr))
           (inject-style ".cirru-leaf" (base-style-leaf))
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
            readonly?
            theme
            0))))))))
