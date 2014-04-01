(ns lumprj.funcs.system

  (:import (java.net Socket InetAddress InetSocketAddress)
           (ch.ethz.ssh2 Connection Session StreamGobbler)
           (com.sun.management OperatingSystemMXBean)
           (java.io  BufferedReader InputStreamReader)
           (java.util StringTokenizer)

           ;;(org.hyperic.sigar Sigar CpuPerc)
           (java.lang.management ManagementFactory))
  )

(declare execCommand get-ssh-connect)
;;chekc port connected
(defn checkport [ip port]
  (try

    (.connect (new Socket) (new InetSocketAddress ip  (read-string port)) 500)
    true
    (catch Exception e false)
  )
)

(defn checkappname [ip appname SSH_SHOW_LIST]
  (let [result (execCommand ip  appname SSH_SHOW_LIST) ]
    (if(> (count result) 0) true false)
    )
  )

;;check appname-old
(defn checkappname-old [ip appname]
  (let [connect (new Connection ip)
        ]
    (.connect connect nil 100 0)
    (println 1)
    (.authenticateWithPassword connect "jack" "shayu626")
    ( let [
            sess  (.openSession connect)
            ]
      (println 2)
      (.execCommand sess (str "pidof " appname))

      (if (> (count (line-seq (new BufferedReader
                                (new InputStreamReader (new StreamGobbler (.getStdout sess ))))))
            0) true false
      )

    )
  ))
;;check ip connected
(defn ping
  [host]
  (.isReachable (InetAddress/getByName host) 500))

;;get cpu information


(defn get-ssh-connect [ip username password]
  (let [connect (new Connection ip)
        ]
    (try
      (println 1)
      (.connect connect nil 100 0)
      (println 2)
      (if (true? (.authenticateWithPassword connect username password)) (assoc {} (read-string (str ":" ip)) connect){})
      (catch Exception e {})
      )
    )
  )

(defn execCommand [ip cmdstr SSH_SHOW_LIST]
  (let [connect (get SSH_SHOW_LIST (read-string (str ":" ip)))
        ]

    (if (nil? connect)[] ( let [
                                    sess  (.openSession connect)
                                    ]
                              (.requestPTY sess "vt100" 80 24 640 480 nil)
                              (.execCommand sess cmdstr)

                              (let [result (line-seq (new BufferedReader
                                                                     (new InputStreamReader (new StreamGobbler (.getStdout sess )))))
                                                 ]
                                (.close sess)
                                result
                               )


                              ))

    )
  )
(defn getDiskRatioByIp [ip SSH_SHOW_LIST]
  (let [result (execCommand ip "df -hl | grep -w '/home\\|/'" SSH_SHOW_LIST) ]
    result
    )
  )
(defn getCpuRatioByIp [ip SSH_SHOW_LIST]
  (let [result (execCommand ip "top -n 1 b|grep Cpu | cut -d ',' -f 1 | cut -d ':' -f 2" SSH_SHOW_LIST) ]
    result
    )
  )

(defn getMemRatioByIp [ip SSH_SHOW_LIST]
  (let [result (execCommand ip "top -n 1 b|grep Mem:| cut -d ':' -f 2" SSH_SHOW_LIST) ]
    result
    )
  )


(defn getCpuRatio []

  ;;(let [cpuperclist  (.getCpuPercList (new Sigar))]
  ;;  (map #(str  (.getUser %)) cpuperclist)

  ;;  )
  (let [connect (new Connection "192.168.2.112")
        ]
    (println "begin ")
    (.connect connect)
    (println "connecting ")
    (println (.authenticateWithPassword connect "jack" "shayu626"))
    (println "connected ")
    ( let [
            sess  (.openSession connect)
            ]
      (.execCommand sess "top -p 10000 -b -n 1")
      (doall (map #(println %) (line-seq (new BufferedReader (new InputStreamReader (new StreamGobbler (.getStdout sess )))))))
      )

    )

  (let [a 1]
    ["0.2" "0.3"]
    )

  )

(defn getMemoryRatio []
  ;;(let [memory  (.getMem (new Sigar))]
  ;;  [{:name "已使用"  :memory (/ (/ (.getUsed memory) 1024) (/ (.getTotal memory) 1024))}
  ;;   {:name "未使用"  :memory (/ (/ (.getFree memory) 1024) (/ (.getTotal memory) 1024))}]
   ;; )
  ([])
  )

;;(let [osname (System/getProperty "os.name")
;;      osversion  (System/getProperty "os.version")
;;      brStat  (new BufferedReader (new InputStreamReader (.getInputStream (.exec (Runtime/getRuntime) "top -b -n 1" ))))
;;      ]
;;  (.readLine brStat)
;;  (.readLine brStat)
;;  (let [tokenStat (new StringTokenizer (.readLine brStat)) ]
;;    (.nextToken tokenStat)
;;    (.nextToken tokenStat)
;;    (.nextToken tokenStat)
;;    (.nextToken tokenStat)
;;    (.nextToken tokenStat)
;;    (.nextToken tokenStat)
;;    (.nextToken tokenStat)
;;    (println (.nextToken tokenStat))
;;    )

;;  )
;;(println (ping "bigjason.com"))

;;system info


;;(ManagementFactory/getOperatingSystemMXBean)
;;(def comb  (ManagementFactory/getOperatingSystemMXBean))

;;memory info
;;(.getFreePhysicalMemorySize comb)
;;(.getTotalPhysicalMemorySize comb)

;;disk space
;;(.getTotalSpace (aget (File/listRoots) 0 ))
