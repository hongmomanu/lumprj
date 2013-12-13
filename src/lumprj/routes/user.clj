(ns lumprj.routes.user
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )

  )


(defroutes user-routes

  (GET "/adduser" [name,passwd]
       (println name)
       (println passwd)

       (db/create-user {:username name :pass passwd})

       (resp/jsonp "showUsers" {:username name :pass passwd})
       ;;(json {:response "ok"})

       )
  )


