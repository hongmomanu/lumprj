(ns lumprj.routes.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [lumprj.controller.server :as servermanager]
            [noir.response :as resp]
            )

  )


(defroutes server-routes

  (GET "/addserver" [servername serverip port portname]
    (servermanager/addserver servername serverip port portname)
    )

)


