(ns lumprj.models.db
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [lumprj.models.schema :as schema]))

(defdb db schema/db-spec-sqlite)

(defentity users)

(defentity servers)

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

(defn has-user [username pass]
   (exec-raw ["SELECT * FROM users WHERE username = ? and password=?" [username pass]] :results)

                 )

(defn has-username [username]
  (exec-raw ["SELECT * FROM users WHERE username = ? " [username]] :results)
  )

(defn has-server [servername port]
  (exec-raw ["SELECT * FROM servers WHERE servername = ? and port=?" [servername port]] :results)
  )
