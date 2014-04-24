(ns lumprj.controller.realstream

  (:import (cn.org.gddsn.liss.client LissClient LissException LissTransferType)
           (edu.iris.miniseedutils.steim GenericMiniSeedRecord GenericMiniSeedRecordOutput)
           (cn.org.gddsn.liss.util LissClientReader)
           (java.io FileInputStream BufferedInputStream PushbackInputStream File DataInputStream)
           (java.util HashSet Date Calendar)
           (java.sql Timestamp)
           (java.text SimpleDateFormat)
           (java.lang.Math)
           (lumprj.java.eqim EqimConnectorTip)
           ;(cn.org.gddsn.jopens.pod.util PodUtil)
           (cn.org.gddsn.jopens.pod.amq AmqEarService)
           (org.springframework.core.io FileSystemResource)
           (org.springframework.beans.factory.xml XmlBeanFactory)
           ;;(edu.iris.timeutils TimeStamp)
           (cn.org.gddsn.seis.evtformat.seed SeedVolume SeedVolumeNativePlugin)
           (cn.org.gddsn.jopens.entity.seed Dataless)
           (cn.org.gddsn.jopens.client SeedVolumeImporter Migration)


           )
  (:use compojure.core  org.httpkit.server)
  (:require [lumprj.models.db :as db]
            [lumprj.funcs.realstream :as realstream]
            [lumprj.funcs.conmmon :as conmmon]
            [lumprj.funcs.websocket :as websocket]
            [me.raynes.fs :as fs]
            [clojure.data.json :as json]
            [noir.response :as resp]
            [taoensso.timbre :as timbre]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-days with-interval-in-minutes with-interval-in-seconds]]
            ;[clojurewerkz.quartzite.schedule.simple :refer [schedule with-repeat-count with-interval-in-seconds with-interval-in-milliseconds]]
            [clj-time.coerce :as clj]
            )
  )

(declare getrealstreams readrealstreamfromcache readsamplestreamcache readsamplestreamcache-less
  make-milltime-data make-milltime-data-cross readrealstreamfromcacheall-filter
  get-epicenter-sampledata-less readsamplestreamcache-less readsamplestreamcache-less-name send-rts-info rts-relation-begin
  get-epicenter-sampledata-less-name )

(def REAL_STREAM_CLIENT (atom {

                                :ip  "10.33.5.103"
                                :port 5000
                                :timelong 11
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
(defn get-epicenter-sampledata [ time station type]
  ;;(println (db/get))
  ;;(readrealstreamfromcache)
  (readsamplestreamcache time station type)
  )

(defn get-epicenter-sampledata-less [ time station type]
  (readsamplestreamcache-less time station type)
  )

(defn get-epicenter-sampledata-less-name [ time station name]
  (readsamplestreamcache-less-name time station name)
  )



(defn max-one-fn [data max]

  (map #(/ % 1) data)
  )
;;相关分析业务
(defn realstreamrelations [rtime rstaton stime sstation second move]

  (let [ sample  (get-epicenter-sampledata-less  stime sstation 0)
         rate (/ -1000 (:rate (first sample)))
         df   (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss.SSS")
         dft   (new SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss.SSS")
         fstime (let [time (:time (first sample))

                      ]
                  (.format df time)

                  )
         fstimet (let [time (:time (first sample))

                      ]
                  (.format dft time)

                  )
         stimet (clojure.string/replace stime #" " "T")
         sampledata1  (into []  (apply concat (map #(:data %) sample)))

         stimespan (- (clj/to-long fstimet) (clj/to-long stimet))


         sampledata (if (> stimespan 0)(sampledata1)(drop (/ stimespan rate) sampledata1))
         samplemax (apply max (map #(Math/abs %) sampledata ))
         realstream (get-epicenter-sampledata-less   rtime rstaton 1)
         frtime (let [time (:time (first realstream))
                      ]
                  (.format df time)

                  )
         frtimet (let [time (:time (first realstream))
                      ]
                  (.format dft time)

                  )

         rtimet (clojure.string/replace rtime #" " "T")
         ;realstreamdata (map #(:data %) realstream)
         realstreamdata1 (into [] (apply concat (map #(:data %) realstream)))
         rtimespan (- (clj/to-long frtimet) (clj/to-long rtimet))
         ;;test1 (println frtime rtime rtimet rtimespan "rtimspan" (clj/to-long frtimet) (clj/to-long rtimet))
         ;;test2 (println fstime stimespan "stimspan" (clj/to-long fstimet) (clj/to-long stimet))
         realstreamdata (if (> rtimespan 0)realstreamdata1 (drop (/ rtimespan rate) realstreamdata1))
         realmax (apply max (map #(Math/abs %) realstreamdata ))




        ]

    (resp/json {
                 :success true
                 :sstation sstation
                 :stime (if (> stimespan 0)fstime stime)
                 :rtime (if (> rtimespan 0) frtime rtime)
                 :rate rate
                 :rstation rstaton
                 :relations (map #(realstream/correlation-analysis
                                    (max-one-fn realstreamdata realmax)
                                    %
                                    (max-one-fn sampledata samplemax)
                                    0 (* second 100)) (range 0 move))
                })

    )


  )


;;相关分析业务实时
(defn realstreamrelationsrts [name rtime rstaton stime sstation second move]

  (let [ sample  (get-epicenter-sampledata-less-name  stime sstation name)
         rate (/ -1000 (:rate (first sample)))
         df   (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss.SSS")
         dft   (new SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss.SSS")
         fstime (let [time (:time (first sample))

                      ]
                  (.format df time)

                  )
         fstimet (let [time (:time (first sample))

                       ]
                   (.format dft time)

                   )
         stimet (clojure.string/replace stime #" " "T")
         sampledata1  (into []  (apply concat (map #(:data %) sample)))

         stimespan (- (clj/to-long fstimet) (clj/to-long stimet))


         sampledata (if (> stimespan 0)sampledata1(drop (/ stimespan rate) sampledata1))
         samplemax (apply max (map #(Math/abs %) sampledata ))
         realstream (readrealstreamfromcache  rtime rstaton )
         frtime (let [time (:time (first realstream))
                      ]
                  (.format df time)

                  )
         frtimet (let [time (:time (first realstream))
                       ]
                   (.format dft time)

                   )

         rtimet (clojure.string/replace rtime #" " "T")
         ;realstreamdata (map #(:data %) realstream)
         realstreamdata1 (map #(:data %) realstream)
         rtimespan (- (clj/to-long frtimet) (clj/to-long rtimet))
         ;;test1 (println frtime rtime rtimet rtimespan "rtimspan" (clj/to-long frtimet) (clj/to-long rtimet))
         ;;test2 (println fstime stimespan "stimspan" (clj/to-long fstimet) (clj/to-long stimet))
         realstreamdata (if (> rtimespan 0)realstreamdata1 (drop (/ rtimespan rate) realstreamdata1))
         realmax (apply max (map #(Math/abs %) realstreamdata ))




         ]
    (println (count sampledata1))
    (resp/json {
                 :success true
                 :sstation sstation
                 :stime (if (> stimespan 0)fstime stime)
                 :rtime (if (> rtimespan 0) frtime rtime)
                 :rate rate
                 :rstation rstaton
                 :relations (map #(realstream/correlation-analysis
                                    (max-one-fn realstreamdata realmax)
                                    %
                                    (max-one-fn sampledata samplemax)
                                    0 (* second 100)) (range 0 move))
                 })

    )


  )


(defn getstreamzerocross []
  ;;(println (db/stationcode-list))
  (resp/json {:success true
              :results  (map getstreamzerocross-fn  (db/stationcode-list))
              }   )
  )
;单元测试
(defn java-clojure-test [name]
  ;(println "ok111")
  (str "hello" name)
  )
;eqim 推送自动报警
(defn send-eqim-info [sp net]
  ;(println @websocket/channel-hub)
  (let [df (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss")
        ]
    (doseq [channel (keys @websocket/channel-hub)]
      ;;(println "ok")
      (send! channel (json/write-str
                       {:location (.Location_cname sp)
                        :lat (.Lat sp) :lon (.Lon sp) :depth (.Depth sp)
                        :eqtype (.Eq_type sp)  :time (.format df (.O_Time  sp))
                        :M (.M sp) :Ml (.Ml sp) :Ms (.Ms sp) :sname (.Sname net) :cname (.Cname net)
                        :type "eqim"
                        }
                       )
        false)
      )

    )


  )
;rts 地震监测
(defn send-rts-info [eventid]
  (let [infoitem (first (db/get-rts-eventinfo eventid))
        lon (:epi_lon infoitem)
        lat (:epi_lat infoitem)
        id (:id infoitem)
        eventranges (:eventranges (conmmon/get-config-prop))
        ]
    (doall(map #(when (and
                        (<= lon (nth (:range %) 1))
                        (>= lon (nth (:range %) 0))
                 (<= lat (nth (:range %) 3)) (>= lat (nth (:range %) 2))
                 ) (doseq [channel (keys @websocket/channel-hub)]
                         ;;(println "ok")
                         (send! channel (json/write-str
                                          {:results (rts-relation-begin id)
                                           :type "rts"
                                           :lonlat [lon lat]
                                           :name (:sname %)
                                           }
                                          )
                           false)))  eventranges) )


  )
  )

(defn rts-relation-begin [catalogid]

  (let [stationitems (db/get-rts-contentinfo catalogid )]
    stationitems
    )

  )

(defn eqim-server-init []
  (let [eqimservers (:eqimservers (conmmon/get-config-prop))]
    (doall(map #(.receiveAndPublish (new EqimConnectorTip (:ip %) (:port %) (:user %) (:pass %))) eqimservers))
    )
  )
(defn eqim-test []
  (.receiveAndPublish (new EqimConnectorTip "10.33.8.174" 5001 "show" "show"))
  (resp/json {:success true})
  )

(defn rts-test []
  ;(println (str "load" (AmqEarService/cfgFile)))
  ;(let [ res (new FileSystemResource (AmqEarService/cfgFile))
  ;       ac (new XmlBeanFactory res)
  ;       amq (.getBean ac "amqEarService")
  ;       ]
   ; (.runListening amq)

  ;  )
  (resp/json {:success true :results (send-rts-info "ZJ.201404141732.0001")})
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
          {:rate (:rate % )}
          {:stationname (:stationname %)}
          {:data (read-string (:data %))}
          {:zerocrossnum (:zerocrossnum %)}
          )
    (db/get-streamcacheall time station))
  )

(defn read-data-fn [row]
  ;(println row)
  {:data (read-string (:data row) ) :time (:time row) :rate (:rate row)}
  )
(defn readsamplestreamcache [time station type]
  ;;(println time station)
  ;;(println (db/get-samplecache time station))
  (map #(read-data-fn %) (db/get-samplecache time station type))
  )

(defn readsamplestreamcache-less [time station type]
  ;;(println time station)
  ;;(println (db/get-samplecache time station))
  (map #(read-data-fn %) (db/get-samplecache-less time station type))
  )

(defn readsamplestreamcache-less-name [time station name]
  ;;(println time station)
  ;;(println (db/get-samplecache time station))
  (map #(read-data-fn %) (db/get-sample-less time station name))
  )



(defn readsamplestreamcache-detail [time station second type]
  (drop 0 (take (* 100 second) (let
                                 [calendar (Calendar/getInstance)
         df   (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss.SSS")
         sample  (db/get-samplecache-bytype-less time station type)
         fstime (let [times (:time (first sample))
                      ]
                  (.format df times)
                  )
         rate (/ -1000 (:rate (first sample)))
         fstimet (clojure.string/replace fstime #" " "T")
         mytime (clojure.string/replace time #" " "T")
         sampledata1  (into []  (apply concat (map #(read-string (:data %)) sample)))

         stimespan (- (clj/to-long fstimet) (clj/to-long mytime))
         ;test1 (println stimespan mytime fstimet (count sampledata1))

         ]
          ;(doall (map #(println  (:time %) (:edtime %) (count (read-string (:data %))) (:stationname %)) sample))
    (if (>= stimespan 0) sampledata1 (drop (/ stimespan rate) sampledata1))
    )))
  ;(drop 0 (take (* 100 second) (map #(read-string (:data %)) (db/get-samplecache-bytype time station type))))
  )
(defn readsamplestreamdata-detail [time station second name]
  (let
                                 [calendar (Calendar/getInstance)
         df   (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss.SSS")
         sample  (db/get-sample-bytype-less time station name)
         fstime (let [times (:time (first sample))
                      ]
                  (.format df times)
                  )
         rate (/ -1000 (:rate (first sample)))
         fstimet (clojure.string/replace fstime #" " "T")
         mytime (clojure.string/replace time #" " "T")
         sampledata1  (into []  (apply concat (map #(read-string (:data %)) sample)))

         stimespan (- (clj/to-long fstimet) (clj/to-long mytime))
         ;test1 (println stimespan mytime fstimet (count sampledata1))

         ]
          ;(doall (map #(println  (:time %) (:edtime %) (count (read-string (:data %))) (:stationname %)) sample))
    (drop 0 (take (* (:rate (first sample)) second) (if (>= stimespan 0) sampledata1 (drop (/ stimespan rate) sampledata1)) ))
    )
  ;(drop 0 (take (* 100 second) (map #(read-string (:data %)) (db/get-samplecache-bytype time station type))))
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
    {:time (new Timestamp (->(.getTime cal)(.getTime))) :data (nth (:data data) n)
     :stationname (:stationname data)
     :type (:type data)
     }
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
     :rate (:rate data)
     :zerocrossnum (caculate-zerocross-num (:data data))
     }
    )


  )

(defn sampledata-child-process-local [data]
  (db/insert-sample  data)
  )
(defn sampledata-child-process [data]
  (db/del-samplecache data)
  ;(db/insert-samplecache  (map #(make-milltime-data data (:time data) %) (range 0 (count (:data data)))))
  (db/insert-samplecache  data)
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
              (j/of-type  realstream-nofile-Job);realstream-nofile-Job realstreamcacheJob
              (j/with-identity (j/key "jobs.noop.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     ;;(with-repeat-count 10)
                                     ;(with-interval-in-minutes 0.1)
                                     (with-interval-in-seconds 10)
                                     )))]
    (qs/schedule job trigger))

  )

(defn make-sampledata [paths  name]

  (let [path paths
        nums (count (db/get-sample-byname name))
        ]
    (if (> 0 nums) "已存在" (map #(let [seedplugin (new SeedVolumeNativePlugin)
                                     ]
                                 (.setFile  seedplugin (new File %))                  ;/home/jack/test/ZJ.201402130341.0002.seed
                                 (loop [gmsRec (.getNextMiniSeedData seedplugin) test 1]
                                   (if (nil? gmsRec)
                                     (println "解码完成la")
                                     (recur (.getNextMiniSeedData seedplugin)
                                       (
                                         do

                                         (sampledata-child-process-local {:stationname (str (.getStation gmsRec) "/"  (.getChannel gmsRec))
                                                                    :data (into [] (.getData gmsRec))
                                                                    :time (new Timestamp (* (.getStartTime gmsRec) 1000))
                                                                    :edtime (new Timestamp (* (.getEndTime gmsRec) 1000))
                                                                    :rate (int (.getSampleRate gmsRec))
                                                                    :name name
                                                                    }   )
                                         ))) )

                                 ) path))


    )


  )

(defn make-sampledata-cache [paths type name]
  (when (= type 1) (db/del-samplecache-type type))
  (when (= type 0) (do (db/del-samplecache-type 1) (db/del-samplecache-type 0)))
  ;(println (count (db/get-samplecache-type 1)) (count (db/get-samplecache-type 0)))

  (let [path paths]
    (map #(let [seedplugin (new SeedVolumeNativePlugin)
                ]
            (.setFile  seedplugin (new File %))                  ;/home/jack/test/ZJ.201402130341.0002.seed
            (loop [gmsRec (.getNextMiniSeedData seedplugin) test 1]
              (if (nil? gmsRec)
                (println "解码完成la")
                (recur (.getNextMiniSeedData seedplugin)
                  (
                    do

                     (sampledata-child-process {:stationname (str (.getStation gmsRec) "/"  (.getChannel gmsRec))
                                             :data (into [] (.getData gmsRec))
                                             :time (new Timestamp (* (.getStartTime gmsRec) 1000))
                                             :edtime (new Timestamp (* (.getEndTime gmsRec) 1000))
                                             :type type
                                             :rate (int (.getSampleRate gmsRec))
                                             }   )
                    ))) )

            ) path)

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
  (let [paths ["/home/jack/test/ZJ_HAZ_BHE_1.mseed"
               "/home/jack/test/ZJ_HAZ_BHN_0.mseed"
               "/home/jack/test/ZJ_HAZ_BHZ_2.mseed"
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