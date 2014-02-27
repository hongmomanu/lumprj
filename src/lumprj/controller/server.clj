(ns lumprj.controller.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [lumprj.funcs.system :as  system]
            [noir.response :as resp]
            )
  )

(def SSH_SHOW_LIST (atom {}))

(defn update-ssh-list []
  (let  [results (db/serverlist 0 10000)]
    (dorun(map #(swap! SSH_SHOW_LIST conj (system/get-ssh-connect (:servervalue %) (:username %) (:password %))) results)
    ))
  )


(defn  addserver [servername servervalue parentid type]
  (let [servercount (if (> (read-string parentid) 0)(:counts (first (db/has-server servername servervalue)))
                      (:counts (first (db/has-system servervalue))))]
    (if (> servercount 0)
      (resp/json {:success false :msg "服务端口已存在"})
      (resp/json {:success true :msg (db/create-server
                                          {:servername servername :servervalue servervalue
                                           :parentid parentid :type type})})
      )
    )

 )


(defn systemmachines [parentid]

  (resp/json (db/servertree parentid))
  )

(defn serverport [serverid ip]
  (let  [results (db/serverport serverid)]

    (resp/json (map #(conj % {:isconnect (system/checkport ip (:servervalue %))}) results))
    )

  )
(defn serverport-app-check [serverid ip]
  (let  [results (db/serverport serverid)]
    (map #(conj % {:isconnect (if (> (:type %) 0)(system/checkappname ip (:servervalue %) @SSH_SHOW_LIST)(system/checkport ip (:servervalue %)))}) results)
    )
  )
(defn serverport-app [serverid ip]
  (let [results (serverport-app-check serverid ip)]
    results
    )
  )

(defn server-cpu [ip]
  (let [results (system/getCpuRatioByIp ip @SSH_SHOW_LIST)]
    ;(println (count results))
    (if (> (count results) 0) (re-find #"\d+.\d+" (first results)) "")

    )
  )

(defn server-mem [ip]
  (let [results (system/getMemRatioByIp ip @SSH_SHOW_LIST)]
    (if
      (> (count results) 0)
      (
        /
        (read-string (first (clojure.string/split (re-find #"\d+ used" (first results)) "used")))
        (read-string (first (clojure.string/split (re-find #"\d+ total" (first results)) "total")))

      )"")

    )
  )

(defn serverlist [key start limit]
  (let  [results (db/serverlist start limit)]
    ;;(println (server-cpu "192.168.2.112"))
    (resp/json {:results (map #(conj %
                                 {:isping (system/ping (:servervalue %))
                                  :cpu (server-cpu (:servervalue %))
                                  :apps (serverport-app (:key %) (:servervalue %))}) results)
                :totalCount (:counts (first (db/servercount)))})
    )

  )





(defn cputimenow []
  ( let [cpulistlist  (map-indexed (fn [idx itm ] {:name (inc idx) :value itm}) (system/getCpuRatioByIp "192.168.2.112" @SSH_SHOW_LIST))
         cpusmap  (reduce (fn [initstr item] (conj initstr
                                               (read-string (str "{:cpu" (:name item) " " (* (read-string (:value item)) 100) "}" )) ))
                    {} cpulistlist)
         ]
    (conj {:time (System/currentTimeMillis )} cpusmap)
    )

  )
(defn memorytimenow []
  ( let [memorymap (system/getMemoryRatio)
         ]
    memorymap
    )
  )
;;cpu info list
(defn getcpuratio []

    (resp/json [(cputimenow)])

)

(defn getmemoryratio []
    (resp/json (memorytimenow))
  )



