(ns lumprj.funcs.system

  (:import (java.net Socket InetAddress InetSocketAddress)
           (com.sun.management OperatingSystemMXBean)
           (java.io  BufferedReader InputStreamReader)
           (java.util StringTokenizer)
           (org.hyperic.sigar Sigar CpuPerc)
           (java.lang.management ManagementFactory))
  )





;;chekc port connected
(defn checkport [ip port]
  (try

    (.connect (new Socket) (new InetSocketAddress ip  (read-string port)) 500)
    true
    (catch Exception e false)
  )
)
;;check ip connected
(defn ping
  [host]
  (.isReachable (InetAddress/getByName host) 500))

;;get cpu information

(defn getCpuRatio []
  ;;(println (.getCpuPercList (new Sigar)))
  (let [cpuperclist  (.getCpuPercList (new Sigar))]
    (map #(str  (.getUser %)) cpuperclist)
    ;;(println (count cpuperclist))
    )
  )

(defn getMemoryRatio []
  ;;(println (.getCpuPercList (new Sigar)))
  (let [memory  (.getMem (new Sigar))]
    [{:name "已使用"  :memory (/ (/ (.getUsed memory) 1024) (/ (.getTotal memory) 1024))}
     {:name "未使用"  :memory (/ (/ (.getFree memory) 1024) (/ (.getTotal memory) 1024))}]
    )
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
