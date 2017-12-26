
(ns server.schema )

(def ir-file {:package "app", :files {}})

(def expr {:type :expr, :by nil, :at nil, :data {}, :id nil})

(def configs {:storage-key "coir.edn", :extension ".cljs", :output "src", :port 6001})

(def user
  {:name nil, :id nil, :nickname nil, :avatar nil, :password nil, :theme :star-trail})

(def bookmark {:kind :def, :ns nil, :extra nil, :focus []})

(def leaf {:type :leaf, :by nil, :at nil, :text "", :id nil})

(def database {:sessions {}, :users {}, :ir ir-file, :saved-files {}, :configs configs})

(def router {:name nil, :title nil, :data {}, :router nil})

(def file {:ns {}, :defs {}, :proc {}})

(def session
  {:user-id nil,
   :id nil,
   :router {:name :files, :data nil, :router nil},
   :notifications [],
   :writer {:selected-ns nil,
            :draft-ns nil,
            :peek-def nil,
            :pointer 0,
            :stack [],
            :clipboard nil}})

(def notification {:id nil, :kind nil, :text nil})

(def page-data
  {:files {:ns-set #{}, :defs-set #{}, :changed-files {}},
   :editor {:focus [], :others #{}, :expr nil}})
