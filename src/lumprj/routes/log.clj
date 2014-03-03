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
  )