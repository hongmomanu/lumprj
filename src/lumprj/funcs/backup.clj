
(import java.net.Socket)


;;check port method
(try

    (new Socket "192.168.2.141" 8080)

    (catch Exception e (str "caught exception: " (.getMessage e))))

;;chekc ip connected
(import java.net InetAddress)
(defn ping
  [host]
  (.isReachable (InetAddress/getByName host) 5000))
(println (ping "bigjason.com"))

;;system info

(import java.lang.management.ManagementFactory)
(import com.sun.management.OperatingSystemMXBean)
(ManagementFactory/getOperatingSystemMXBean)
(def comb  (ManagementFactory/getOperatingSystemMXBean))

;;memory info
(.getFreePhysicalMemorySize comb)
(.getTotalPhysicalMemorySize comb)

;;disk space
(.getTotalSpace (aget (File/listRoots) 0 ))
