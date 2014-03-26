(ns lumprj.routes.realstream
  (:use compojure.core)

  (:require [lumprj.controller.realstream :as realstreammanager]
            [noir.response :as resp]
            [clojure.data.json :as json]
            )

  )

(defroutes realstream-routes

  (GET "/readrealstreambyfilename" []


    (resp/json {:success true
                :msg  "测试实时miniSeed"
                :result (realstreammanager/readrealstream)})

    )

  (GET "/readrealstreamfromcache" []


    (resp/json {:success true :result (realstreammanager/readrealstreamfromcache)})

    )

  (GET "/readrealstream" []

    (realstreammanager/makerealstreamfile)
    (resp/json {:success true})

    )


)


