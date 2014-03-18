(ns lumprj.routes.realstream
  (:use compojure.core)
  (:require [lumprj.controller.realstream :as realstreammanager]
            [noir.response :as resp]
            [clojure.data.json :as json]
            )

  )

(defroutes realstream-routes

  (GET "/readrealstream" []

    (realstreammanager/readrealstream)
    (resp/json {:success false})

    )


)


