(ns lumprj.controller.user
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )
  )


(defn  adduser [username password displayname admin telnum departments email]
         (if (> (count (db/has-username username)) 0)
           (resp/json {:success false :result "用户已存在la!"})
           (resp/json {:success true :result (db/create-user
                                               {:username username :password password
                                                :displayname displayname :admin admin
                                                :telnum telnum :departments departments :email email})})
           )
 )

(defn login [username password]
    (let [results (db/has-user  username password)]
      ( if (> (count results) 0) (resp/json {:success true :result (first results) })
          (resp/json {:success false :msg "用户名密码错误"})
      )
    )

  )