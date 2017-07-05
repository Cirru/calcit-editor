
(ns server.schema )

(def user {:name nil, :id nil, :nickname nil, :avatar nil, :password nil})

(def router {:name nil, :title nil, :data {}, :router nil})

(def configs {:storage-key "coir.edn", :port (or (.-port js/process.env) 5021)})

(def ir-file {:pkg "app", :files {}})

(def database {:sessions {}, :users {}, :ir ir-file})

(def session
  {:user-id nil,
   :id nil,
   :nickname nil,
   :router {:name :files, :data nil, :router nil},
   :notifications [],
   :writer {:pointer 0, :stack []}})

(def notification {:id nil, :kind nil, :text nil})

(def focus {:ns nil, :kind :def, :extra nil, :coord []})
