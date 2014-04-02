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

  (GET "/readrealstreamfromcache" [time station]

    (let [data (realstreammanager/readrealstreamfromcache time station)]
      (resp/json {:success true :result  data})
      )
    )

  (GET "/readrealstream" []

    (realstreammanager/makerealstreamfile)
    (resp/json {:success true})

    )

  (GET "/realstream/getrealstream" []
    (realstreammanager/getstreamzerocross)
    )

  (GET "/realstream/realstreamrelations" [rtime rstation stime sstation]
    (realstreammanager/realstreamrelations rtime rstation stime sstation)
    )
  (GET "/realstream/samplescache" [time station]
    (resp/json {:success true :result (realstreammanager/readsamplestreamcache time station)})
    )


)


