(ns lumprj.models.schema
  (:require [clojure.java.jdbc :as sql]
            [noir.io :as io]))

(def db-store "site.db")
(def db-store-sqlite "sqlite.db3")

(def datapath (str (System/getProperty "user.dir") "/"))



(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :subname (str (io/resource-path) db-store)
              :user "sa"
              :password ""
              :naming {:keys clojure.string/lower-case
                       :fields clojure.string/upper-case}})

(def db-spec-sqlite {:classname "org.sqlite.JDBC"
                     :subprotocol "sqlite"
                     :subname (str datapath db-store-sqlite)
                     })


(defn initialized?
  "checks to see if the database schema is present"
  []
  ;;(.exists (new java.io.File (str (io/resource-path) db-store ".h2.db")))

  (.exists (new java.io.File (str datapath db-store-sqlite "")))
  )


(defn create-users-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :users
      [:id "integer primary key autoincrement"]  ;;
      [:username "varchar(30)"]
      [:displayname "varchar(30)"]
      [:telnum "varchar(30)"]
      [:departments "varchar(30)"]
      [:email "varchar(30)"]
      [:admin "BOOLEAN DEFAULT 0"]
      [:last_login "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      [:is_active "BOOLEAN DEFAULT 0"]
      [:password "varchar(100)"])))

(defn create-servers-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :servers
      [:id "integer primary key autoincrement"]  ;;
      [:servername "varchar(30)"]
      [:serverip "varchar(30)"]
      [:port "varchar(30)"]
      [:portname "varchar(30)"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      )))

(defn create-tables
  "creates the database tables used by the application"
  []
  (create-users-table)
  (create-servers-table)
  )
