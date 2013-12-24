(ns lumprj.routes.server
  (:use compojure.core)
  (:require [lumprj.models.dboracle :as dboracle]
            [lumprj.controller.server :as servermanager]
            [noir.response :as resp]
            )

  )


(defroutes server-routes

  (GET "/addserver" [servername servervalue parentid]
    (servermanager/addserver servername servervalue parentid)
    )

  (GET "/serverlist" [key start limit]
    (servermanager/serverlist key start limit)
    )

  (GET "/serverport" [serverid ip]
    (servermanager/serverport serverid ip)
    )

  (GET "/orcltest" []
    (resp/json (dboracle/oracltest))
    )


)


