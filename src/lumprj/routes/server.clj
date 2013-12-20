(ns lumprj.routes.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
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


)


