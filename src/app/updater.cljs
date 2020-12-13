
(ns app.updater
  (:require [app.updater.session :as session]
            [app.updater.user :as user]
            [app.updater.router :as router]
            [app.updater.ir :as ir]
            [app.updater.writer :as writer]
            [app.updater.notify :as notify]
            [app.updater.analyze :as analyze]
            [app.updater.watcher :as watcher]
            [app.updater.repl :as repl]
            [app.updater.configs :as configs]))

(defn updater [db op op-data sid op-id op-time]
  (let [f (case op
            :session/connect session/connect
            :session/disconnect session/disconnect
            :session/select-ns session/select-ns
            :user/nickname user/nickname
            :user/log-in user/log-in
            :user/sign-up user/sign-up
            :user/log-out user/log-out
            :user/change-theme user/change-theme
            :router/change router/change
            :writer/edit writer/edit
            :writer/edit-ns writer/edit-ns
            :writer/select writer/select
            :writer/point-to writer/point-to
            :writer/focus writer/focus
            :writer/go-up writer/go-up
            :writer/go-down writer/go-down
            :writer/go-left writer/go-left
            :writer/go-right writer/go-right
            :writer/remove-idx writer/remove-idx
            :writer/paste writer/paste
            :writer/save-files writer/save-files
            :writer/collapse writer/collapse
            :writer/move-next writer/move-next
            :writer/move-previous writer/move-previous
            :writer/move-order writer/move-order
            :writer/finish writer/finish
            :writer/draft-ns writer/draft-ns
            :writer/hide-peek writer/hide-peek
            :writer/picker-mode writer/picker-mode
            :writer/pick-node writer/pick-node
            :ir/add-ns ir/add-ns
            :ir/add-def ir/add-def
            :ir/remove-def ir/remove-def
            :ir/remove-ns ir/remove-ns
            :ir/prepend-leaf ir/prepend-leaf
            :ir/append-leaf ir/append-leaf
            :ir/delete-node ir/delete-node
            :ir/leaf-after ir/leaf-after
            :ir/leaf-before ir/leaf-before
            :ir/expr-before ir/expr-before
            :ir/expr-after ir/expr-after
            :ir/expr-replace ir/expr-replace
            :ir/indent ir/indent
            :ir/unindent ir/unindent
            :ir/unindent-leaf ir/unindent-leaf
            :ir/update-leaf ir/update-leaf
            :ir/duplicate ir/duplicate
            :ir/rename ir/rename
            :ir/cp-ns ir/cp-ns
            :ir/mv-ns ir/mv-ns
            :ir/delete-entry ir/delete-entry
            :ir/reset-files ir/reset-files
            :ir/reset-at ir/reset-at
            :ir/reset-ns ir/reset-ns
            :ir/draft-expr ir/draft-expr
            :ir/replace-file ir/replace-file
            :ir/file-config ir/file-config
            :ir/clone-ns ir/clone-ns
            :ir/toggle-comment ir/toggle-comment
            :notify/push-message notify/push-message
            :notify/clear notify/clear
            :notify/broadcast notify/broadcast
            :analyze/goto-def analyze/goto-def
            :analyze/abstract-def analyze/abstract-def
            :analyze/peek-def analyze/peek-def
            :watcher/file-change watcher/file-change
            :repl/start repl/on-start
            :repl/log repl/on-log
            :repl/value repl/log-value
            :repl/error repl/on-error
            :repl/exit repl/on-exit
            :repl/clear-logs repl/clear-logs
            :ping identity
            :configs/update configs/update-configs
            (do (println "Unknown op:" op) identity))]
    (f db op-data sid op-id op-time)))
