(ns lumprj.models.db
  (:use korma.core
        [korma.db :only [defdb with-db]])
  (:require [lumprj.models.schema :as schema]))

(defdb db schema/db-spec-sqlite)



(declare users dutyenum)

(defentity dutyenum
  (database db)
  (belongs-to users {:fk :userid})
  )
(defentity users
  (database db)
  (has-one dutyenum {:fk :userid})

  )

(defentity servers
  (database db)
  )

(defn create-user [user]

  (insert users
          (values user))

  )

(defn create-server [server]

  (insert servers
    (values server))
  )

(defn update-user [id first-name last-name email]
  (update users
  (set-fields {:first_name first-name
               :last_name last-name
               :email email})
  (where {:id id})))

(defn get-user [id]
  (first (select users
                 (where {:id id})
                 (limit 1))))

(defn user-list []
  (select users
    (fields [:id :userid] :displayname :username )
    (with dutyenum
      (fields :day)
      )

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

(defn has-user [username pass]
  (with-db db
    (exec-raw ["SELECT * FROM users WHERE username = ? and password=?" [username pass]] :results))


                 )

(defn has-username [username]
  (with-db db
    (exec-raw ["SELECT * FROM users WHERE username = ? " [username]] :results))
  )

(defn has-server [servername servervalue]
  (with-db db
    (exec-raw ["SELECT * FROM servers WHERE servername = ? and servervalue=?" [servername servervalue]] :results))
  )
(defn serverlist [start limits]


  (select servers
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

