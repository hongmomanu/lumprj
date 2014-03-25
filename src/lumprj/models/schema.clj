(ns lumprj.models.schema
  (:require [clojure.java.jdbc :as sql]
            [noir.io :as io]))


(declare create-dutymission-table create-dutymissionhistory-table
  create-servers-table create-systemwatchlog-table create-dutylog-table create-stations-table)
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

(def db-mysql {:subprotocol "mysql"
               :subname "//127.0.0.1:9306?characterEncoding=utf8&maxAllowedPacket=512000"
               ;;:subname "//127.0.0.1:3306"
               ;;:user "root"
               ;;:password "shayu626"
               })

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

(def db-h2-mem {:classname "org.h2.Driver"
                :subprotocol "h2"
                :subname "mem:session;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false" })


(defn initialized?
  "checks to see if the database schema is present"
  []
  ;;(.exists (new java.io.File (str (io/resource-path) db-store ".h2.db")))
  ;;(create-stations-table)
  (.exists (new java.io.File (str datapath db-store-sqlite "")))
  )



(defn create-streamcache-table
  []
  (sql/with-connection db-h2-mem
    (sql/create-table
      :streamcache
      [:data "nvarchar(5000)"]
      [:stationname "nvarchar(20)"]
      [:time "TIMESTAMP"]
      ))
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
      [:servername "nvarchar(200)"]
      [:machinecss "nvarchar(200)"]
      [:servervalue "nvarchar(200)"]
      [:parentid "integer DEFAULT -1"]
      [:username "nvarchar(200)"]
      [:password "nvarchar(200)"]
      [:type "integer"]   ;;0:port 1:appname
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
      [:start "DATETIME"]
      [:end "DATETIME"]
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


;;值日任务记录表
(defn create-dutymissionhistory-table
    []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutymissionhistory
      [:id "integer primary key autoincrement"]
      [:missionid "integer"]
      [:userid "integer"]
      [:missionstatus "integer"]
      [:dutylog "varchar(300)"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )

;;系统监测记录表
(defn create-systemwatchlog-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :systemwatchlog
      [:id "integer primary key autoincrement"]
      [:serverid "integer"]
      [:statustype "nvarchar(100)"]
      [:userid "integer"]   ;;jack added 03-11
      [:logcontent "nvarchar(200)"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )

;;值班日志记录表
(defn create-dutylog-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutylog
      [:id "integer primary key autoincrement"]
      [:userid "integer"]
      [:statustype "nvarchar(100)"]
      [:logcontent "nvarchar(200)"]
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]
      ))
  )
;;台站表

(defn create-stations-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :stations
      [:id "integer primary key autoincrement"]
      [:stationname "nvarchar(100)"] ;台站名称
      [:networkname "nvarchar(100)"] ;台网名称
      [:stationcode "nvarchar(100)"] ;台站代码
      [:dataaddr "nvarchar(100)"] ;数采地址
      [:gatewayaddr "nvarchar(100)"] ;网关地址
      [:connecttype "nvarchar(100)"] ;通讯类型
      [:contact "nvarchar(100)"] ;联系人
      [:phone "nvarchar(100)"] ;联系人电话
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
  (create-dutymissionhistory-table)
  )
