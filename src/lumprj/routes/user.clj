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

  (POST "/login" [username,password]


       (let [results (db/has-user  username password)]


         ;;(true? (> (:num results) 0) (print "yes") (print "no") )
        ( if (> (count results) 0) (resp/json {:success true }) (resp/json {:success false}))

         )

       ;;(resp/jsonp "showUsers" {:username name :pass passwd})
       ;;(json {:response "ok"})

       )

  (POST "/ajaxlogin" [name,passwd]
       (println name)
       (println passwd)
       (resp/json (first (db/has-user  name passwd)))

       )

  (POST "/logintest" [name,passwd]

       (resp/json {:success true :result {:userDesktop {:launchers {} :modules {} :style {:wallpaper {}}}}})

       )

  )


