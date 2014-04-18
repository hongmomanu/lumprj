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
                :subname "mem:session;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0" })


(defn initialized?
  "checks to see if the database schema is present"
  []
  ;;(.exists (new java.io.File (str (io/resource-path) db-store ".h2.db")))
  ;;(create-stations-table)
  (.exists (new java.io.File (str datapath db-store-sqlite "")))
  )


;;数据流缓存表
(defn create-streamcache-table
  []
  (sql/with-connection db-h2-mem
    (sql/create-table
      :streamcache
      [:data "nvarchar(5000)"] ;数据
      [:zerocrossnum "int"]  ;零交点数目
      [:stationname "nvarchar(20)"]   ;台站名
      [:time "TIMESTAMP"]    ;时间
      [:edtime "TIMESTAMP"]    ;结束时间
      ))
  )


;;样本数据缓存表
(defn create-samplecache-table
  []
  (sql/with-connection db-h2-mem
    (sql/create-table
      :samplecache
      [:data "nvarchar(500000)"] ;数据
      [:stationname "nvarchar(20)"]   ;台站名
      [:time "TIMESTAMP"]    ;时间
      [:edtime "TIMESTAMP"]    ;结束时间
      [:type "int"] ;;是否为事件 1是
      [:rate "int"] ;;样本比率
      ))
  )

;;用户表
(defn create-users-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :users
      [:id "integer primary key autoincrement"]  ;;用户id
      [:username "varchar(30)"]    ;用户名
      [:displayname "varchar(30)"] ;显示名陈
      [:telnum "varchar(30)"]   ;电话号码
      [:departments "varchar(30)"] ;部门（未来扩展）
      [:email "varchar(30)"]      ;电子邮箱（未来扩展）
      [:admin "BOOLEAN DEFAULT 0"]  ;是否管理员
      [:last_login "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"] ;登陆时间（未来扩展）
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"] ;时间
      [:is_active "BOOLEAN DEFAULT 0"]    ;是否在线（未来扩展）
      [:password "varchar(100)"])))   ;密码（未来扩展)

;;服务器配置表
(defn create-servers-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :servers
      [:id "integer primary key autoincrement"] ;服务器id
      [:servername "nvarchar(200)"] ;服务器名
      [:machinecss "nvarchar(200)"] ;服务器图标
      [:servervalue "nvarchar(200)"] ;服务应用名地址
      [:parentid "integer DEFAULT -1"] ;父节点
      [:username "nvarchar(200)"] ;服务器用户名
      [:password "nvarchar(200)"] ; 服务器登陆密码
      [:type "integer"]   ;;0:port 1:appname 类型
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"] ;时间
      )))
;;值日枚举表
(defn create-dutyenum-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutyenum
      [:id "integer primary key autoincrement"] ;值日id
      [:day "int"]  ;;1-7 星期
      [:start "DATETIME"] ;开始时间
      [:end "DATETIME"]  ;结束时间
      [:userid "integer"] ;用户id
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"]  ;时间
      ))
  )
;;值班任务表
(defn create-dutymission-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutymission
      [:id "integer primary key autoincrement"]     ;值日任务id
      [:missionname "varchar(100)"]  ;任务名称
      [:missiontime "varchar(100)"]  ;任务执行时间
      [:missioninterval "integer"]   ;任务间隔
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"] ;时间
      ))
  )


;;值日任务记录表
(defn create-dutymissionhistory-table
    []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :dutymissionhistory
      [:id "integer primary key autoincrement"]   ;任务记录id
      [:missionid "integer"]  ;任务id
      [:userid "integer"]    ;用户id
      [:missionstatus "integer"]  ;任务状态
      [:dutylog "varchar(300)"]  ;值日日记
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"] ;时间
      ))
  )

;;系统监测记录表
(defn create-systemwatchlog-table
  []
  (sql/with-connection db-spec-sqlite
    (sql/create-table
      :systemwatchlog
      [:id "integer primary key autoincrement"]   ;服务器日志 id
      [:serverid "integer"]   ;服务器id
      [:statustype "nvarchar(100)"]  ;状态类型
      [:userid "integer"]   ;;jack added 03-11  用户id
      [:logcontent "nvarchar(200)"]    ;日志类型
      [:time "DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))"] ;时间
      ))
  )

;;值班日志记录表  （取消）
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
