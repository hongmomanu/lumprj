(ns lumprj.routes.server
  (:import (lumprj.java Test))
  (:use compojure.core)
  (:require [lumprj.controller.server :as servermanager]
            [noir.response :as resp]
            [clojure.data.json :as json]
            )

  )


(defroutes server-routes

  (POST "/server/addserver" [servername servervalue parentid type]
    (servermanager/addserver servername servervalue parentid type)
    )
  (POST "/server/sendsystemlogs" [systemlogs]
    (println(json/read-str systemlogs
      :key-fn keyword))
    (servermanager/addsystemlog (json/read-str systemlogs
                                  :key-fn keyword))
    )

  (GET "/serverlist" [key start limit]
    (servermanager/serverlist key start limit)
    )

  (GET "/server/getsystems" [node]
    (servermanager/systemmachines node)
    )

  (GET "/serverport" [serverid ip]
    (servermanager/serverport serverid ip)
    )
  (GET "/getcpuratio" []
    (servermanager/getcpuratio)
    )

  (GET "/getmemoryratio" []
    (servermanager/getmemoryratio)
    )


  (GET "/orcltest" []
    (println (.say (new Test)))
    ;;(resp/json (dboracle/oracltest))
    )


)


