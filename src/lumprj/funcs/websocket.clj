(ns lumprj.funcs.websocket
  (:use org.httpkit.server)
  (:require
            [clojure.data.json :as json]
            )
)

(def clients (atom {}))

(defn ws
  [req]
  (with-channel req con
    (swap! clients assoc con true)
    (println con " connected")
    (on-close con (fn [status]
                    (swap! clients dissoc con)
                    (println con " disconnected. status: " status)))))

(future (loop []
          (doseq [client @clients]
            (send! (key client) (json/write-str
                                  {:happiness (rand 10)})
              false))
          (Thread/sleep 5000)
          (recur)))


(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})


(defn start-server [port]

    (run-server app {:port port :join? false})

  )
