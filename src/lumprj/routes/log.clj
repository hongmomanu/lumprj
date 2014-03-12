(ns lumprj.routes.log
  (:use compojure.core)
  (:require [lumprj.controller.log :as logmanager]
            [noir.response :as resp]
            [clojure.data.json :as json]
            )

  )

(defroutes log-routes

  (GET "/log/getlogsystem" request
    (logmanager/log-system-list (:params  request))

    )
  (GET "/log/getlogduty" request
    (logmanager/log-duty-list (:params  request))

    )
  (POST "/log/deletelogs" request
    (logmanager/log-system-del (:params  request))

    )

  (GET "/log/logsystemstatics" request
    (logmanager/log-system-statics (:params  request))
    )
  (GET "/log/logsystemstaticsinfobyday" [day searchtype]
    (logmanager/log-system-statics-dayinfo day searchtype)
    )
  )