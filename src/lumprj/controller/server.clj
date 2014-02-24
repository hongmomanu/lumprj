(ns lumprj.controller.server
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [lumprj.funcs.system :as  system]
            [noir.response :as resp]
            )
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

(defn serverlist [key start limit]
  (let  [results (db/serverlist start limit)]

    (resp/json {:results (map #(conj % {:isping (system/ping (:servervalue %))}) results)
                :totalCount (:counts (first (db/servercount)))})
    )

  )
(defn serverport [serverid ip]
  (let  [results (db/serverport serverid)]
    (resp/json (map #(conj % {:isconnect (system/checkport ip (:servervalue %))}) results))
    )

  )

(defn cputimenow []
  ( let [cpulistlist  (map-indexed (fn [idx itm ] {:name (inc idx) :value itm}) (system/getCpuRatio))
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



