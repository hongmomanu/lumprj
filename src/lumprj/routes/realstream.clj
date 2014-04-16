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

  (GET "/realstream/eqimtest" []
    (realstreammanager/eqim-test)
    )

  (GET "/realstream/rtstest" []
    (realstreammanager/rts-test)
    )


  (GET "/realstream/realstreamrelations" [rtime rstation stime sstation second move]
    (realstreammanager/realstreamrelations rtime rstation stime sstation (read-string second) (read-string move))
    )
  (GET "/realstream/samplescache" [time station]
    (resp/json {:success true :result (realstreammanager/readsamplestreamcache time station)})
    )
  (GET "/realstream/samplescachedetail" [time station second type timesample]
    (let [result (realstreammanager/readsamplestreamcache-detail time station (read-string second) type)
          result1 (realstreammanager/readsamplestreamcache-detail timesample station (read-string second) 0)
          ]

      (resp/json {:success true :result  result
      :result1 result1
                  })
      )

    )
  (GET "/realstream/makesamplescache" [paths type]

    (resp/json {:success true :result  (realstreammanager/make-sampledata-cache (clojure.string/split paths #",") (read-string  type))})
    )

  (POST "/realstream/toolconfig" [filename]
    (resp/json (realstreammanager/relation-tool filename))
    )


)


