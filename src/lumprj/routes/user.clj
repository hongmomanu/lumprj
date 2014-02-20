(ns lumprj.routes.user
  (:use compojure.core)
  (:require [lumprj.controller.user :as user]
            [noir.response :as resp]
            )

  )


(defroutes user-routes

  (GET "/adduser" [username password displayname admin telnum departments email]
    (user/adduser username password displayname admin telnum departments email)
    )
  (GET "/users" []
    (user/userlist)
    )
  (POST "/login" [username password]

    (user/login username password)

  )

)


