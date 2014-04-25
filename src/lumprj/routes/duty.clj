(ns lumprj.routes.duty
  (:use compojure.core)
  (:require [lumprj.controller.duty :as duty]
            [noir.response :as resp]
            [clojure.data.json :as json]
            )
  )


(defroutes duty-routes

  (GET "/getworkmanagers" []
    (duty/dutylist)
    )

  (GET "/getcurrentduty" [day date]
    (duty/getcurrentduty day date)
    )
  (GET "/getmissions" []
    (duty/getmissions)

    )
  (POST "/duty/savemission" request
    (duty/savemission request)
    )
  (GET "/getdutymissions" [day]
    (duty/getdutymissions day)
    )

  (GET "/maketodaymission" [day userid]
    (duty/maketodaymission day userid)
    )

  (GET "/duty/mysqlalert" []
    (duty/mysqlalert)
    )

  (POST "/duty/addnewstation"  request
    (duty/addnewstation (:params request))
   )
  (POST "/duty/savestation"  request
    (duty/savestation (:params request))
   )

  (GET "/duty/getworkmanagerevents" [startDate endDate]
    (duty/getworkmanagerevents startDate endDate)
    )
  (POST "/duty/getworkmanagerevents/0" [cid start end]
    (duty/addworkmanagerevents cid start end)
    )
  (PUT "/duty/getworkmanagerevents/:id" [id cid start end]
    (duty/saveworkmanagerevents id cid start end)
    )
  (DELETE "/duty/getworkmanagerevents/:id" [id]
    (duty/deleteworkmanagerevents id)
    )
  (GET "/duty/getcalendars" []
    (duty/getcalendars)
    )
  (GET "/duty/getstations" [keyword start limit]
    (duty/getstations keyword start limit)
    )

  (POST "/addnewduty" [day userid]
    (duty/insertduty day userid)
    )
  (POST "/addnewmission" [missionname missiontime missioninterval]
    (duty/insertmission missionname missiontime missioninterval)
    )
  (POST "/delenumbyid" request

    (resp/json  (duty/delenumbyid request))
    ;;(resp/json {:success false})
    )
  (POST "/duty/senddutylogs" [systemlogs]

    (duty/senddutylogs (json/read-str systemlogs
                         :key-fn keyword))

    )
  (POST "/duty/eqimcheck" [id username password url securl]

    (duty/eqimcheck id username password url securl)

    )
  (POST "/duty/eqimpublic" [url]

    (duty/eqimcheck-nologin url )

    )
  (POST "/duty/completeduty" [id dutylog]

    (duty/completeduty id dutylog)

    )

  (POST "/duty/copywavefile" [sourcedir targetdir]
    (duty/copywavefile sourcedir targetdir)
    )
  (POST "/duty/checkarchive" [sourcedir earthplatformlist archiveminsize]
    (duty/checkarchive sourcedir (json/read-str earthplatformlist) archiveminsize)
    )

)


