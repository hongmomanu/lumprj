(ns lumprj.models.schema
  (:require [clojure.java.jdbc :as sql]
            [noir.io :as io]))


(declare create-dutymission-table)
(def db-store "site.db")
(def db-store-sqlite "sqlite.db3")

(def datapath (str (System/getProperty "user.dir") "/"))

;;(defdb korma-db
;;  (oracle { :user "xxx"
;;            :password "xxx"
;;            :host "my.oracle.db"
;;            :port 1521
;;            :make-pool? true }))

(def db-oracle  {:classname "oracle.jdbc.OracleDriver"
                 :subprotocol "oracle"
                 :subname "thin:@192.168.2.141:1521:orcl"
                 :user "CIVILAFFAIRS_ZS"
                 :password "hvit"
                 :naming {:keys clojure.string/lower-case :fields clojure.string/upper-case}})

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
  ;;(create-dutymission-table)
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
      [:id "integer primary key autoincrement"]
      [:servername "varchar(30)"]
      [:servervalue "varchar(30)"]
      [:parentid "integer DEFAULT -1"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      )))
;;值日枚举表
(defn create-dutyenum-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutyenum
      [:id "integer primary key autoincrement"]
      [:day "int"]  ;;1-7
      [:userid "integer"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )
;;值班任务表
(defn create-dutymission-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutymission
      [:id "integer primary key autoincrement"]
      [:missionname "varchar(100)"]
      [:missiontime "varchar(100)"]
      [:missioninterval "integer"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )

;;值日登记记录表
(defn create-dutyhistory-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutyhistory
      [:id "integer primary key autoincrement"]
      [:userid "integer"]
      [:isonduty "integer"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )

;;值日任务记录表
(defn create-dutymissionhistory-table
    []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutymissionhistory
      [:id "integer primary key autoincrement"]
      [:dutyhistory "integer"]
      [:missionid "integer"]
      [:missionstatus "integer"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )

(defn create-tables
  "creates the database tables used by the application"
  []
  (create-users-table)
  (create-servers-table)
  (create-dutyenum-table)
  (create-dutymission-table)
  )
