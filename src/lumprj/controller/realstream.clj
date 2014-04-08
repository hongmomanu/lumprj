(ns lumprj.controller.realstream
  (:import (cn.org.gddsn.liss.client LissClient LissException LissTransferType)
           (edu.iris.miniseedutils.steim GenericMiniSeedRecord GenericMiniSeedRecordOutput)
           (cn.org.gddsn.liss.util LissClientReader)
           (java.io FileInputStream BufferedInputStream PushbackInputStream File DataInputStream)
           (java.util HashSet Date Calendar)
           (java.sql Timestamp)
           ;;(edu.iris.timeutils TimeStamp)
           (cn.org.gddsn.seis.evtformat.seed SeedVolume SeedVolumeNativePlugin)
           (cn.org.gddsn.jopens.entity.seed Dataless)
           (cn.org.gddsn.jopens.client SeedVolumeImporter Migration)

           )
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [lumprj.funcs.realstream :as realstream]
            [lumprj.funcs.conmmon :as conmmon]
            [me.raynes.fs :as fs]
            [noir.response :as resp]
            [taoensso.timbre :as timbre]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-days with-interval-in-minutes]]

            )
  )

(declare getrealstreams readrealstreamfromcache readsamplestreamcache
  make-milltime-data make-milltime-data-cross readrealstreamfromcacheall-filter)

(def REAL_STREAM_CLIENT (atom {

                                :ip  "10.33.5.103"
                                :port 5000
                                :timelong 61
                                :user "rts"
                                :pass "rts"}))





(defn make-average-type [type data]
  (conmmon/average(map #(:zerocrossnum %) (filter (fn [x]
                                            (> (.indexOf (:stationname x) type) 0))
                                    data
                                    )))

  )

(defn make-average-no-type [data]
  (conmmon/average(map #(:zerocrossnum %) data))
  )


(defn getstreamzerocross-fn [station]
  ;(.importIt (new SeedVolumeImporter)  (new FileInputStream "/home/jack/test/ZJ.201402130341.0002.seed"))
  ;;(println (readrealstreamfromcache))


  (let [
        alldata-bhe   (reverse (readrealstreamfromcacheall-filter (str (:stationcode station) "/BHE")))
        alldata-bhz   (reverse (readrealstreamfromcacheall-filter (str (:stationcode station) "/BHZ")))
        alldata-bhn   (reverse (readrealstreamfromcacheall-filter (str (:stationcode station) "/BHN")))
        ;;stationdata (filter (fn [x] (= (.indexOf (:stationname x) (:stationcode station)) 0)) alldata)
        bhesub  (take-last (quot  (count alldata-bhe) 2) alldata-bhe)
        bhelast (take  (quot  (count alldata-bhe) 2) alldata-bhe)
        bhzsub  (take-last (quot  (count alldata-bhz) 2) alldata-bhz)
        bhzlast (take  (quot  (count alldata-bhz) 2) alldata-bhz)
        bhnsub  (take-last (quot  (count alldata-bhz) 2) alldata-bhn)
        bhnlast (take  (quot  (count alldata-bhz) 2) alldata-bhn)
        ]

                          {
                            :crossavgbhe (make-average-no-type bhesub)
                            :crossavgbhz (make-average-no-type bhzsub)
                            :crossavgbhn (make-average-no-type bhnsub)
                            :crossnowbhe (make-average-no-type bhelast)
                            :crossnowbhz (make-average-no-type bhzlast)
                            :crossnowbhn (make-average-no-type bhnlast)
                            :stationname (:stationname station)
                            :stationcode (:stationcode station)
                            :time  (:time (first bhesub))
                           }

    )
  )
;;读取头文件
(defn dataless []
  (let [in (new FileInputStream "/home/jack/test/ZJ.201402130341.0002.seed" )
        dl (new Dataless)
        ]
    (.readSeedVolumeDataless dl (new PushbackInputStream in 4096))
    (.close in)
    (let [
           station (-> (.getSeedConfig dl) (.getStation)(.get 0))
           channel (-> (.getChannel station)(.get 0))

           ]
      ;(println (-> (.getResponse channel)(.get 0) (.getBlockette061)))
      )

    )

  )
;根据震中获取样本数据
(defn get-epicenter-sampledata [ time station]
  ;;(println (db/get))
  ;;(readrealstreamfromcache)
  (readsamplestreamcache time station)
  )

;;相关分析业务
(defn realstreamrelations [rtime rstaton stime sstation second move]

  (let [sampledata (get-epicenter-sampledata  stime sstation)
        realstreamdata (get-epicenter-sampledata   rtime rstaton) ;(:data (first (readrealstreamfromcache)))
        ]

    (resp/json {
                 :success true
                 :sstation sstation
                 :rstation rstaton
                 :relations (map #(realstream/correlation-analysis realstreamdata 0 sampledata % (* second 100)) (range 0 move))
                })

    )


  )

(defn getstreamzerocross []
  ;;(println (db/stationcode-list))
  (resp/json {:success true
              :results  (map getstreamzerocross-fn  (db/stationcode-list))
              }   )
  )

(defn readrealstreamfromcacheall-filter [stationname]
  (map #(conj {:time (:time % )}
          {:stationname (:stationname %)}
          {:data (read-string (:data %))}
          {:zerocrossnum (:zerocrossnum %)}
          )
    (db/get-streamcacheall-data stationname))
  )
(defn readrealstreamfromcache [time station]
  (map #(conj {:time (:time % )}
          {:edtime (:edtime % )}
          {:stationname (:stationname %)}
          {:data (read-string (:data %))}
          {:zerocrossnum (:zerocrossnum %)}
          )
    (db/get-streamcacheall time station))
  )

(defn readsamplestreamcache [time station]
  ;;(println time station)
  ;;(println (db/get-samplecache time station))
  (map #(read-string (:data %)) (db/get-samplecache time station))
  )

(defn caculate-zerocross-num [data]
  (count (for [x (range 0 (count data))
        :let [y 0]
        :when (and (> x 0) (< (* (nth data x)(nth data (- x 1))) 0))]
    y))
  )

(defn realstream-data-func [data]
  ;;(println (map #(make-milltime-data-cross data (:time data) %) (range 0 (count (:data data)))))
  (db/insert-streamcache (map #(make-milltime-data-cross data (:time data) %) (range 0 (count (:data (doall data))))) )
  )

(defn realstream-data-update-func [data]
  ;;(println (map #(make-milltime-data-cross data (:time data) %) (range 0 (count (:data data)))))
  (map #(db/update-streamcache %) (map #(make-milltime-data-cross data (:time data) %) (range 0 (count (:data data)))) )
  )


(defn realstreamcacheJob-child-dataprocess [data]
  (doall(map #(if(> (count (db/has-streamcache (:time %) (:stationname %))) 0)
                ;(db/update-streamcache ( conj {:zerocrossnum (caculate-zerocross-num (:data %))} %))
                ;(db/insert-streamcache ( conj {:zerocrossnum (caculate-zerocross-num (:data %))} %))
                (realstream-data-update-func %)
                (realstream-data-func %)
                )
          data))
  ;;(db/insert-streamcache data)
  (let [result (db/get-streamcache)]
    ;(println (>  (count result) 0))
    (when (>  (count result) 0) (db/del-streamcache))
    )
  )
(defn make-milltime-data [data time n]
  (let [cal (Calendar/getInstance)]
    (.setTimeInMillis cal (.getTime time))
    (.add cal Calendar/MILLISECOND (* 10 n))
    {:time (new Timestamp (->(.getTime cal)(.getTime))) :data (nth (:data data) n) :stationname (:stationname data) }
    )

  )
(defn make-milltime-data-cross [data time n]
  (let [cal (Calendar/getInstance)]
    ;(.setTimeInMillis cal (.getTime (Timestamp/valueOf time)))
    ;(.add cal Calendar/MILLISECOND (* 10 n))
    (.setTimeInMillis cal (.getTime time))
    (.add cal Calendar/MILLISECOND (* 10 n))
    {:time (new Timestamp (->(.getTime cal)(.getTime))) :data (nth (:data data) n)
     :stationname (:stationname data)
     :zerocrossnum (caculate-zerocross-num (:data data))
     }
    )


  )
(defn sampledata-child-process [data]
  ;(println (map #(make-milltime-data data (:time data) %) (range 0 (count (:data data)))))
  (db/del-samplecache data)
  (db/insert-samplecache  (map #(make-milltime-data data (:time data) %) (range 0 (count (:data data)))))
  )

(defjob realstreamcacheJob
  [ctx]    ;;realstreamcacheJob
  (println "定时获取实时数据")
  (let [data  (getrealstreams)] ;;(getrealstreams)

    (realstreamcacheJob-child-dataprocess data)

    )
  )

(defjob realstream-nofile-Job
  [ctx]
  (println "定时获取实时数据la")
  (try
    (let [lissClient (new LissClient (:ip @REAL_STREAM_CLIENT) (:port @REAL_STREAM_CLIENT))
          buf  (byte-array 512)
          firstTime (new Date)
          ]

      (.login lissClient (:user @REAL_STREAM_CLIENT) (:pass @REAL_STREAM_CLIENT))
      (timbre/info (str "Logged into Server: " (:ip @REAL_STREAM_CLIENT)))

      (.setType lissClient LissTransferType/BINARY)
      (.setRtServerPassiveMode lissClient false)
      (timbre/info "Enter the passive transport mod")
      (timbre/info (str "Retrieving  MiniSeed data from " (:ip @REAL_STREAM_CLIENT)))
      (let [lissInputStream  (new DataInputStream (.retrieveRealTimeStream lissClient (into-array (map #(:stationcode %) (db/stationcode-list)))))]

        (loop [nCurrent 0 test 1]

          (if (< (:timelong @REAL_STREAM_CLIENT) nCurrent)
            (
              do
              (timbre/info "读取数据完成")
              (.abortRealTimeStreamTransport lissClient)
              (.quit lissClient)
              )
            (recur (quot (- (.getTime (new Date))  (.getTime firstTime))  1000)
              (
                do
                (println "读取数据中...")
                (.readFully lissInputStream buf)
                (GenericMiniSeedRecord/buildMiniSeedRecord buf)
                (realstreamcacheJob-child-dataprocess
                  (doall (map #(realstream/decodeminirtbufdata % buf)  (take 1 (iterate inc 0) ) )))
                )  )))

        )

      )
    true
    (catch Exception e false)
    )



  )

(defn makerealstreamcache []



  (qs/initialize)
  (qs/start)
  (let [job (j/build
              (j/of-type  realstreamcacheJob);realstream-nofile-Job realstreamcacheJob
              (j/with-identity (j/key "jobs.noop.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     ;;(with-repeat-count 10)
                                     (with-interval-in-minutes 1))))]
    (qs/schedule job trigger))

  )

(defn make-sampledata-cache [paths]
  (let [path paths]
    (doall(map #(let [seedplugin (new SeedVolumeNativePlugin)
                ]
            (.setFile  seedplugin (new File %))                  ;/home/jack/test/ZJ.201402130341.0002.seed
            (loop [gmsRec (.getNextMiniSeedData seedplugin) test 1]
              (if (nil? gmsRec)
                (println "解码完成la")
                (recur (.getNextMiniSeedData seedplugin)
                  (
                    do
                    ;(println (.getStartTimes gmsRec))

                    ;;(println (-> (Calendar/getInstance)(.add Calendar/MILLISECOND 32)))
                    ;;(println (.toString (new Timestamp (* (.getStartTime gmsRec) 1000))))
                    ;;(println (.toString (new Timestamp (* (.getEndTime gmsRec) 1000))))

                    (sampledata-child-process {:stationname (str (.getStation gmsRec) "/"  (.getChannel gmsRec))
                                             :data (into [] (.getData gmsRec))
                                             :time (new Timestamp (* (.getStartTime gmsRec) 1000))
                                             :edtime (new Timestamp (* (.getEndTime gmsRec) 1000))
                                             }   )))) )

            ) path))

    )


  )

(defn makerealstreamfile []
  (.start (new Thread (new LissClientReader "10.33.5.103" "rts" "rts"
                                            "/home/jack/test/testnew11111.HAZ" 30 "HAZ"
                                            "testsS")))
  )

(defn makerealstream-nofile [ip  user pass timelong stnCodes]
  (try
    (let [lissClient (new LissClient ip 5000)
          lissInputStream  (new DataInputStream (.retrieveRealTimeStream lissClient stnCodes))
          buf  (byte-array 512)
          firstTime (new Date)
          ]
      (.login lissClient user pass)
      (timbre/info (str "Logged into Server: " ip))
      (.setType lissClient LissTransferType/BINARY)
      (.setRtServerPassiveMode lissClient false)
      (timbre/info "Enter the passive transport mod")
      (timbre/info (str "Retrieving  MiniSeed data from " ip))

      (loop [nCurrent 0 test 1]
        (if (< timelong nCurrent)
          (do
            (timbre/info "读取数据完成")
            (.abortRealTimeStreamTransport lissClient)
            (.quit lissClient)
            )
          (recur (quot (- (.getTime (new Date))  (.getTime firstTime))  1000)
            (
                do
                (.readFully lissInputStream buf)
                (GenericMiniSeedRecord/buildMiniSeedRecord buf)
              (doall (map #(realstream/decodeminirtdata % buf (byte-array 512) 512)  (take 1 (iterate inc 0) ) ))

            )  )))

      )
    true
    (catch Exception e false)
    )


  )

(defn relation-tool [filename]
  (try


    {:success true :msg (read-string (slurp filename)) }
    (catch Exception e {:success false :msg (.getMessage e)})
    )

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
    ;;(println (/ (fs/size path) 512))
    (doall (map #(realstream/decodeminirtdata % bis buf 512)  (take (/ (fs/size path) 512) (iterate inc 0) ) ))
    )



  )


(defn getrealstreams []
  (let [paths ["/home/jack/test1w/ZJ_HAZ_BHE_1.mseed"
               "/home/jack/test1w/ZJ_HAZ_BHN_0.mseed"
               "/home/jack/test1w/ZJ_HAZ_BHZ_2.mseed"
               ]]
    (concat (readsiglefilestream (nth paths 0)) (readsiglefilestream (nth paths 1)) (readsiglefilestream (nth paths 2)))

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
  (let [paths [
               "/home/jack/test/ZJ_HAZ_BHE_1.mseed"
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