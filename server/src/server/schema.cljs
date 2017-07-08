
(ns server.schema )

(def ir-file {:pkg "app", :files {}})

(def expr {:type :expr, :author nil, :time nil, :data {}})

(def configs
  {:storage-key "coir.edn",
   :extension ".cljs",
   :output "src",
   :port (or (.-port js/process.env) 6001)})

(def user {:name nil, :id nil, :nickname nil, :avatar nil, :password nil})

(def bookmark {:kind :def, :ns nil, :extra nil, :focus []})

(def leaf {:type :leaf, :author nil, :time nil, :text ""})

(def database {:sessions {}, :users {}, :ir ir-file, :saved-files {}})

(def router {:name nil, :title nil, :data {}, :router nil})

(def file {:ns {}, :defs {}, :proc {}})

(def session
  {:user-id nil,
   :id nil,
   :nickname nil,
   :router {:name :files, :data nil, :router nil},
   :notifications [],
   :writer {:selected-ns nil, :pointer 0, :stack [], :clipboard nil}})

(def notification {:id nil, :kind nil, :text nil})

(def page-data
  {:files {:ns-set #{}, :defs-set #{}, :changed-files {}},
   :editor {:focus [], :others #{}, :expr nil}})
