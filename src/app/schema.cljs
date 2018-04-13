
(ns app.schema )

(def bookmark {:kind :def, :ns nil, :extra nil, :focus []})

(def configs
  {:storage-key "calcit.edn",
   :local-storage-key "calcit-storage",
   :extension ".cljs",
   :output "src",
   :port 6001})

(def ir-file {:package "app", :files {}})

(def database
  {:sessions {},
   :users {},
   :ir ir-file,
   :saved-files {},
   :configs configs,
   :repl {:alive? false, :logs {}}})

(def expr {:type :expr, :by nil, :at nil, :data {}, :id nil})

(def file {:ns {}, :defs {}, :proc {}})

(def leaf {:type :leaf, :by nil, :at nil, :text "", :id nil})

(def notification {:id nil, :kind nil, :text nil, :time nil})

(def page-data
  {:files {:ns-set #{}, :defs-set #{}, :changed-files {}},
   :editor {:focus [], :others #{}, :expr nil}})

(def repl-log {:id nil, :type nil, :text "", :time nil})

(def router {:name nil, :title nil, :data {}, :router nil})

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

(def user
  {:name nil, :id nil, :nickname nil, :avatar nil, :password nil, :theme :star-trail})
