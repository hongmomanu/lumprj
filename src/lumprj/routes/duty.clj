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

  (GET "/getcurrentduty" [day]
    (duty/getcurrentduty day)
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
    (resp/json {:success false})
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
  (POST "/duty/eqimcheck" [id username password url]

    (duty/eqimcheck id username password url)

    )
  (POST "/duty/completeduty" [id]

    (duty/completeduty id)

    )

  (POST "/duty/copywavefile" [sourcedir targetdir]
    (duty/copywavefile sourcedir targetdir)
    )
  (POST "/duty/checkarchive" [sourcedir earthplatformlist archiveminsize]
    (duty/checkarchive sourcedir (json/read-str earthplatformlist) archiveminsize)
    )

)


