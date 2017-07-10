
(ns server.updater.core
  (:require [server.updater.session :as session]
            [server.updater.user :as user]
            [server.updater.router :as router]
            [server.updater.ir :as ir]
            [server.updater.writer :as writer]
            [server.updater.notify :as notify]))

(defn updater [db op op-data session-id op-id op-time]
  (case op
    :session/connect (session/connect db op-data session-id op-id op-time)
    :session/disconnect (session/disconnect db op-data session-id op-id op-time)
    :user/log-in (user/log-in db op-data session-id op-id op-time)
    :user/sign-up (user/sign-up db op-data session-id op-id op-time)
    :user/log-out (user/log-out db op-data session-id op-id op-time)
    :session/remove-notification
      (session/remove-notification db op-data session-id op-id op-time)
    :session/select-ns (session/select-ns db op-data session-id op-id op-time)
    :router/change (router/change db op-data session-id op-id op-time)
    :writer/edit (writer/edit db op-data session-id op-id op-time)
    :writer/select (writer/select db op-data session-id op-id op-time)
    :writer/point-to (writer/point-to db op-data session-id op-id op-time)
    :writer/focus (writer/focus db op-data session-id op-id op-time)
    :writer/go-up (writer/go-up db op-data session-id op-id op-time)
    :writer/go-down (writer/go-down db op-data session-id op-id op-time)
    :writer/go-left (writer/go-left db op-data session-id op-id op-time)
    :writer/go-right (writer/go-right db op-data session-id op-id op-time)
    :writer/remove-idx (writer/remove-idx db op-data session-id op-id op-time)
    :writer/copy (writer/copy db op-data session-id op-id op-time)
    :writer/cut (writer/cut db op-data session-id op-id op-time)
    :writer/paste (writer/paste db op-data session-id op-id op-time)
    :writer/save-files (writer/save-files db op-data session-id op-id op-time)
    :ir/add-ns (ir/add-ns db op-data session-id op-id op-time)
    :ir/add-def (ir/add-def db op-data session-id op-id op-time)
    :ir/remove-def (ir/remove-def db op-data session-id op-id op-time)
    :ir/remove-ns (ir/remove-ns db op-data session-id op-id op-time)
    :ir/append-leaf (ir/append-leaf db op-data session-id op-id op-time)
    :ir/delete-node (ir/delete-node db op-data session-id op-id op-time)
    :ir/leaf-after (ir/leaf-after db op-data session-id op-id op-time)
    :ir/leaf-before (ir/leaf-before db op-data session-id op-id op-time)
    :ir/expr-before (ir/expr-before db op-data session-id op-id op-time)
    :ir/expr-after (ir/expr-after db op-data session-id op-id op-time)
    :ir/indent (ir/indent db op-data session-id op-id op-time)
    :ir/unindent (ir/unindent db op-data session-id op-id op-time)
    :ir/unindent-leaf (ir/unindent-leaf db op-data session-id op-id op-time)
    :ir/update-leaf (ir/update-leaf db op-data session-id op-id op-time)
    :ir/duplicate (ir/duplicate db op-data session-id op-id op-time)
    :ir/rename (ir/rename db op-data session-id op-id op-time)
    :notify/push-error (notify/push-error db op-data session-id op-id op-time)
    db))
