(ns lumprj.controller.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )
  )


(defn  addserver [servername serverip port  portname ]
         (if (> (count (db/has-server servername port)) 0)
           (resp/json {:success false :result "服务端口已存在"})
           (resp/json {:success true :result (db/create-server
                                               {:servername servername :serverip serverip
                                                :port port :portname portname})})
           )
 )

