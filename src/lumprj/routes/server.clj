(ns lumprj.routes.server
  (:import (lumprj.java Test))
  (:use compojure.core)
  (:require [lumprj.controller.server :as servermanager]
            [noir.response :as resp]
            )

  )


(defroutes server-routes

  (POST "/server/addserver" [servername servervalue parentid type]
    (servermanager/addserver servername servervalue parentid type)
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


