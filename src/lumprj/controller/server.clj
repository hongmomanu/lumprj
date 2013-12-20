(ns lumprj.controller.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )
  )


(defn  addserver [servername servervalue parentid]
         (if (> (count (db/has-server servername servervalue)) 0)
           (resp/json {:success false :result "服务端口已存在"})
           (resp/json {:success true :result (db/create-server
                                               {:servername servername :servervalue servervalue
                                                :parentid parentid})})
           )
 )

(defn serverlist [key start limit]
  (resp/json {:results (db/serverlist start limit) :totalCount 1})
  )

