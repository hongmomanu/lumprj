(ns lumprj.models.db
  (:use korma.core
        [korma.db :only [defdb with-db]])
  (:require [lumprj.models.schema :as schema]))

(defdb db schema/db-spec-sqlite)
(defdb dbmysql schema/db-mysql)
(defdb memdb schema/db-h2-mem)



(declare users dutyenum dutymission dutymissionhistory)


(defentity streamcache
  (database memdb)
  )

(defentity dutyenum
  (database db)
  (belongs-to users {:fk :userid})
  )
(defentity users
  (database db)
  ;;(has-one dutyenum {:fk :userid})
  )
(defentity dutymission
  (database db)
  )
(defentity dutymissionhistory
  (database db)
  (belongs-to dutymission {:fk :missionid})
  )


(defentity servers
  (database db)
  )

(defentity systemwatchlog
  (database db)
  (belongs-to servers {:fk :serverid})
  (belongs-to users {:fk :userid})
  )

(defentity dutylog
  (database db)
  (belongs-to users {:fk :userid})
  )

(defentity stations
  (database db)
  )

(defn insert-streamcache [caches]
  (insert streamcache
    (values caches)
    )
  )

(defn update-streamcache [caches]
  (update streamcache
    (set-fields caches)
    (where {:time (:time caches) :stationname (:stationname caches)}
      )
    )
  )

(defn del-streamcache []
  (delete streamcache
    (where {:time [< (sqlfn DATEADD "MINUTE"  -3 (sqlfn now) )]})
    )
  )

(defn has-streamcache [time stationname]

  (select streamcache
    (fields :stationname)
    (where {:time time :stationname stationname}
             )
    )
  )
(defn get-streamcache []
  (select streamcache
    (fields :stationname :time)
    (where (and {:time [<= (sqlfn now)]}
             {:time [>= (sqlfn DATEADD "MINUTE"  -3 (sqlfn now) )]}
             ))
    )
  )
(defn get-streamcacheall[]
  (select streamcache

    (order :time)
    )
  )
(defn create-user [user]

  (insert users
          (values user))

  )

(defn add-systemlog [systemlog]
  (insert systemwatchlog
    (values systemlog)
    )
  )

(defn add-dutylog [dutylogs]
  ;;(insert dutylog
  (insert systemwatchlog
    (values dutylogs)
    )
  )

(defn create-server [server]

  (insert servers
    (values server))
  )

(defn update-server [data id]
  (update servers
    (set-fields data)
    (where {:id id}))

  )
(defn update-mission [data id]
  (update dutymission
    (set-fields data)
    (where {:id id})
    )
  )
(defn update-user [data id]
  (update users
  (set-fields data)
  (where {:id id})))

(defn get-user [id]
  (first (select users
                 (where {:id id})
                 (limit 1))))

(defn user-list []
  (select users
    (fields [:id :userid]  :id [:displayname :title] :displayname :username :telnum :password :admin)
    ;(limit 1)
    )
  )

(defn log-system-statics [params]
  (select systemwatchlog
    (fields  [(sqlfn strftime "%Y-%m-%d" :time) :date])
    (where (and {:time [> (:bgday params)]}
             {:time [< (:edday params)]}
             ))
    (aggregate (count :id) :counts)
    (group :date)
    )
  )
(defn log-duty-statics [params]
  (select dutylog
    (fields  [(sqlfn strftime "%Y-%m-%d" :time) :date])
    (where (and {:time [> (:bgday params)]}
             {:time [< (:edday params)]}
             ))
    (aggregate (count :id) :counts)
    (group :date)
    )
  )

(defn log-duty-statics-dayinfo [day]
  (select dutylog
    (fields  :statustype)
    (where {:time [like (str day "%")]})
    (aggregate (count :id) :counts)
    (group :statustype)
    )
  )

(defn log-system-statics-dayinfo [day]
  (select systemwatchlog
    (fields  :statustype)
    (where {:time [like (str day "%")]})
    (aggregate (count :id) :counts)
    (group :statustype)
    )
  )
(defn log-del [params]
  (delete systemwatchlog
    (where (and {:time [> (:bgday params)]}
             {:time [< (:edday params)]}
             )
      )
    (where {:logcontent [like (str "%" (:keyword params) "%") ]})
    (where {:statustype [like (str (:statustype params) "%")]})

    )
  )
(defn log-list [params]

    (select systemwatchlog
      (with servers
        (fields :servername :servervalue)
        )
      (with users
        (fields :username :displayname)
        )
      (where (and {:time [> (:bgday params)]}
               {:time [< (:edday params)]}
               )
        )

      (where {:logcontent [like (str "%" (:keyword params) "%") ]})
      (where {:statustype [like (str (:statustype params) "%")]})
      (limit (:limit params))
      (offset (:start params))
      (order :id :DESC)
      )

  )
(defn log-duty-list [params]
  (select dutylog
    (with users
      (fields :username)
      )
    (where (and {:time [> (:bgday params)]}
             {:time [< (:edday params)]}
             ))
    (where {:logcontent [like (str "%" (:keyword params) "%") ]})
    (limit (:limit params))
    (offset (:start params))
    (order :id :DESC)
    )
  )

(defn stationcode-list []
  (select stations
    (fields :stationcode :stationname)
    )
  )
(defn stations-list [keyword starts limits]
  (select stations
    (where {:stationname [like (str "%" keyword "%") ]})

    (limit limits)
    (offset starts)
    (order :id :DESC)
    )
  )

(defn stations-count [keyword start limit]
  (select stations
    (where {:stationname [like (str "%" keyword "%") ]})
    (aggregate (count :id) :counts)
    )
  )

(defn savenewstation [key-values]
  (insert stations
    (values  key-values)
    )

  )
(defn log-duty-count [params]
  (select dutylog
    (where (and {:time [> (:bgday params)]}
             {:time [< (:edday params)]}
             ))
    (where {:logcontent [like (str "%" (:keyword params) "%") ]})
    (aggregate (count :id) :counts)
    )
  )

(defn log-count [params]
  (select systemwatchlog
    (where (and {:time [>= (:bgday params)]}
                {:time [<= (:edday params)]}
             ))
    (where {:logcontent [like (str "%" (:keyword params) "%") ]})
    (where {:statustype [like (str (:statustype params) "%")]})
    (aggregate (count :id) :counts)
    )
  )

(defn duty-list []
  (select dutyenum

    (fields [:id :enumid] :day :userid)
    (with users
      (fields :username :displayname)
      )
    (order :day)
    )
  )
(defn duty-del-byids [ids]
  (delete dutyenum
    (where {:id (if(instance? String ids)ids [in ids])})
    )
  )
(defn mission-query []
  (select dutymission)
  )

(defn completedutymission [id dutylog]
  (update dutymissionhistory
    (set-fields {:missionstatus 1 :time (sqlfn datetime "now" "localtime") :dutylog dutylog})
    (where {:id id}))
  )
(defn mission-today-list [day]
  (select dutymissionhistory
    (with dutymission
      (fields :missionname :missiontime :missioninterval )
      )
    (where {:time [like (str day "%")]})
    )
  )
(defn mission-history-query [day userid]
  (select dutymissionhistory
    (where {:time [like (str day "%")] }) ;;:userid userid
    (aggregate (count :id) :counts)
    )

  )

(defn addworkmanagerevents [cid start end]
  (insert dutyenum
    (values {:start start :end end :userid cid }))
  )
(defn saveworkmanagerevents [id data]
  (update dutyenum
    (set-fields data)
    (where {:id id})
    )
  )
(defn deleteworkmanagerevents [id]
  (delete dutyenum
    (where {:id id})
    )
  )
(defn getworkmanagerevents [startDate endDate]
  (select dutyenum
    (fields [:id :enumid] :id  :day :userid [:userid :cid] :start :end )
    (with users
      (fields [:displayname :title] :username)
      )
    (where (or (and {:start [>= startDate]}
             {:start [<= endDate]}
             )
             (and {:end [>= startDate]}
               {:end [<= endDate]}
               )
             )

      )
    )
  )

(defn duty-query-day [day date]
  (select dutyenum
    (fields [:id :enumid] :day :userid)
    (with users
      (fields :username :displayname)
      )
    ;(where {:day day})
    (where (and {(sqlfn strftime "%Y-%m-%d" :start) [<= date]}
                 {(sqlfn strftime "%Y-%m-%d" :end) [>= date]}
                 )
      )
    )
  )
(defn duty-insert [day userid]
  (insert dutyenum
    (values {:day day :userid userid}))
  )
(defn mission-history-insert [missionlist]
  (insert dutymissionhistory
    (values missionlist)
    )
  )

(defn mission-insert [missionname missiontime missioninterval]
  (insert dutymission
    (values {:missionname missionname :missiontime missiontime :missioninterval missioninterval})
    )
  )

(defn has-user [username pass]
  (select users
    (where {:username username :password pass})
    ))
  ;;(with-db db
  ;;  (exec-raw ["SELECT * FROM users WHERE username = ? and password=?" [username pass]] :results))
  ;;               )

(defn has-username [username]
  (with-db db
    (exec-raw ["SELECT * FROM users WHERE username = ? " [username]] :results))
  )

(defn has-server [servername servervalue]
  (with-db db
    (select servers (where {:servervalue servervalue :servername servername}) (aggregate (count :id) :counts)))
    ;;(exec-raw ["SELECT * FROM servers WHERE servername = ? and servervalue=?" [servername servervalue]] :results))
  )
(defn has-system [servervalue]
    (with-db db
      (select servers (where {:servervalue servervalue}) (aggregate (count :id) :counts)))

  )
(defn servertree [parentid]
  (select servers
    (where {:parentid parentid})
    (order :id))

  )
(defn serverlist [start limits]


  (select servers
    (fields [:id :key] :servername :servervalue :machinecss :type :parentid :time :username :password)
    (where {:parentid -1})
    (order :id)
    (limit limits)
    (offset start))

    ;;(exec-raw ["SELECT * FROM servers WHERE parentid=-1 limit ? offset ?" [limit start]] :results))
  )


(defn servercount []
  (with-db db
    (select servers (where {:parentid -1}) (aggregate (count :id) :counts)))
    ;;(exec-raw ["SELECT * FROM servers WHERE parentid=-1" []] :results))
 )

(defn serverport [serverid]
  (with-db db
    (exec-raw ["SELECT * FROM servers WHERE parentid=?" [serverid]] :results))
  )

(defn mysqlalert []
  (with-db dbmysql
    (exec-raw ["SELECT * FROM rt where MATCH ('\"320902\"')" []] :results))
  )

