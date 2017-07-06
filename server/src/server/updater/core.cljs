
(ns server.updater.core
  (:require [server.updater.session :as session]
            [server.updater.user :as user]
            [server.updater.router :as router]
            [server.updater.ir :as ir]
            [server.updater.writer :as writer]))

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
    :writer/point-to (writer/point-to db op-data session-id op-id op-time)
    :writer/focus (writer/focus db op-data session-id op-id op-time)
    :ir/add-ns (ir/add-ns db op-data session-id op-id op-time)
    :ir/add-def (ir/add-def db op-data session-id op-id op-time)
    :ir/remove-def (ir/remove-def db op-data session-id op-id op-time)
    :ir/append-leaf (ir/append-leaf db op-data session-id op-id op-time)
    :ir/delete-leaf (ir/delete-leaf db op-data session-id op-id op-time)
    db))
