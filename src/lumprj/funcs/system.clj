(ns lumprj.funcs.system

  (:import (java.net Socket InetAddress)
           (com.sun.management OperatingSystemMXBean)
           (java.lang.management ManagementFactory))
  )






;;check port method
;;(try

;;    (new Socket "192.168.2.141" 8080)

;;    (catch Exception e (str "caught exception: " (.getMessage e))))

;;chekc ip connected
(defn checkport [ip port]
  (try
    (println ip)
    (println port)
    (new Socket ip (read-string port))
    true
    (catch Exception e false)
  )
)

(defn ping
  [host]
  (.isReachable (InetAddress/getByName host) 500))
;;(println (ping "bigjason.com"))

;;system info


;;(ManagementFactory/getOperatingSystemMXBean)
;;(def comb  (ManagementFactory/getOperatingSystemMXBean))

;;memory info
;;(.getFreePhysicalMemorySize comb)
;;(.getTotalPhysicalMemorySize comb)

;;disk space
;;(.getTotalSpace (aget (File/listRoots) 0 ))
