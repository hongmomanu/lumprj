(ns lumprj.models.db
  (:use korma.core
        [korma.db :only [defdb with-db]])
  (:require [lumprj.models.schema :as schema]))

(defdb db schema/db-spec-sqlite)



(declare users dutyenum dutymission dutymissionhistory)

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
  )

(defentity dutylog
  (database db)
  (belongs-to users {:fk :userid})
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
  (insert dutylog
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
    (fields [:id :userid] :displayname :username )

    )
  )

(defn log-list [params]
  (select systemwatchlog
    (with servers
      (fields :servername :servervalue)
      )
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
    (limit (:limit params))
    (offset (:start params))
    (order :id :DESC)
    )
  )
(defn log-duty-count [params]
  (select dutylog
    (aggregate (count :id) :counts)
    )
  )

(defn log-count [params]
  (select systemwatchlog
    (aggregate (count :id) :counts)
    )
  )

(defn duty-list []
  (select dutyenum

    (fields [:id :enumid] :day :userid)
    (with users
      (fields :username :displayname)
      )
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

(defn completedutymission [id]
  (update dutymissionhistory
    (set-fields {:missionstatus 1 :time (sqlfn datetime "now" "localtime")})
    (where {:id id}))
  )
(defn mission-today-list [day]
  (select dutymissionhistory
    (with dutymission
      (fields :missionname :missiontime :missioninterval)
      )
    (where {:time [like (str day "%")]})
    )
  )
(defn mission-history-query [day userid]
  (select dutymissionhistory
    (where {:time [like (str day "%")] :userid userid})
    (aggregate (count :id) :counts)
    )

  )

(defn duty-query-day [day]
  (select dutyenum
    (fields [:id :enumid] :day :userid)
    (with users
      (fields :username :displayname)
      )
    (where {:day day})
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

