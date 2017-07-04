
(ns server.schema )

(def user {:name nil, :id nil, :nickname nil, :avatar nil, :password nil})

(def router {:name nil, :title nil, :data {}, :router nil})

(def configs {:storage-key "/data/cumulo/workflow-storage.edn", :port 5021})

(def database {:sessions {}, :users {}, :topics {}})

(def session
  {:user-id nil,
   :id nil,
   :nickname nil,
   :router {:name :home, :data nil, :router nil},
   :notifications []})

(def notification {:id nil, :kind nil, :text nil})
