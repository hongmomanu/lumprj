(ns lumprj.controller.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [lumprj.funcs.system :as  system]
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
  (let  [results (db/serverlist start limit)]
    (resp/json {:results (map #(conj % {:isping (system/ping (:servervalue %))}) results)
                :totalCount (count (db/servercount))})
    )

  )
(defn serverport [serverid ip]
  (let  [results (db/serverport serverid)]
    (resp/json (map #(conj % {:isconnect (system/checkport ip (:servervalue %))}) results))
    )
  ;;(resp/json [{:servername "test1" :value 1} {:servername "test2" :value 0} {:servername "test3" :value 1}])

  )

