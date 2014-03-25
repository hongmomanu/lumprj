(ns lumprj.controller.realstream
  (:import (cn.org.gddsn.liss.client LissClient LissException LissTransferType)
           (edu.iris.miniseedutils.steim GenericMiniSeedRecord GenericMiniSeedRecordOutput)
           (cn.org.gddsn.liss.util LissClientReader)
           (java.io FileInputStream BufferedInputStream)

           )
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [lumprj.funcs.realstream :as realstream]
            [me.raynes.fs :as fs]
            [noir.response :as resp]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-days with-interval-in-minutes]]

            )
  )

(declare getrealstreams)
(defjob realstreamcacheJob
  [ctx]
  (println "Does nothing")
  (let [data (getrealstreams)]
    ;;(db/insert-streamcache data)
    (println (db/get-streamcache))
    )

  )

(defn makerealstreamcache []
  (qs/initialize)
  (qs/start)
  (let [job (j/build
              (j/of-type realstreamcacheJob)
              (j/with-identity (j/key "jobs.noop.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     ;;(with-repeat-count 10)
                                     (with-interval-in-minutes 1))))]
    (qs/schedule job trigger))

  )

(defn makerealstreamfile []
  (.start (new Thread (new LissClientReader "10.33.5.103" "rts" "rts"
                                            "/home/jack/test/testnew11111.BJT" 30 "NJD"
                                            "testsS")))
  )

(defn readsiglefilestream [filepath]

  (let [
         ;path "/home/jack/Downloads/ZJ_HAZ_BHZ_2.mseed"
         path filepath
         bis (new BufferedInputStream (new FileInputStream path))
         buf  (byte-array 512)
         ;;now_ms (.getTime (new Date))
         ]
    (.skip bis  (long 0))
    (println (/ (fs/size path) 512))
    (doall (map #(realstream/decodeminirtdata % bis buf 512)  (take (/ (fs/size path) 512) (iterate inc 0) ) ))
    )



  )


(defn getrealstreams []
  (let [paths ["/home/jack/test/ZJ_HAZ_BHE_1.mseed"
               "/home/jack/test/ZJ_HAZ_BHN_0.mseed"
               "/home/jack/test/ZJ_HAZ_BHZ_2.mseed"
               ]]
    (concat (readsiglefilestream (nth paths 0)) (readsiglefilestream (nth paths 1)) (readsiglefilestream (nth paths 2)))
    ;;(map #(into [] (readsiglefilestream %)) paths)

    )
  )
(defn readrealstream []


  ;(.start (new Thread (new LissClientReader "10.33.5.103" "rts" "rts"
  ;                                 "/home/jack/test/testnew.BJT" 10 "HAZ"
  ;                                 "")))
  ;(let [bis (new BufferedInputStream (new FileInputStream "/home/jack/test/test.BJT") 6665536)]
  ;  (println bis)
  ;  (.printMiniSeedRecordContents (new GenericMiniSeedRecordOutput) bis System/err)
  ;  )
  (let [paths ["/home/jack/test/ZJ_HAZ_BHE_1.mseed"
               "/home/jack/test/ZJ_HAZ_BHN_0.mseed"
               "/home/jack/test/ZJ_HAZ_BHZ_2.mseed"
               ]]
    (map #(into [] (readsiglefilestream %)) paths)

    )

  ;;(GenericMiniSeedRecord/testDecompress "/home/jack/test/test.BJT")

  ;(println (.parseMiniSeedWaveforms (new GenericMiniSeedRecordOutput) "/home/jack/test/testnw.BJT"))

  ;;(slurp "/home/jack/test/test")
  ;(let [lissClient (new LissClient "127.0.0.1" 5000)]

  ;  (.login lissClient "jack" "shayu626")
  ;  )

  ;;(resp/json (dboracle/oracltest))
  )