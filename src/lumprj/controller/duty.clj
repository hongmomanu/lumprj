(ns lumprj.controller.duty
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            [me.raynes.fs :as fs]
            [clj-http.client :as client]
            )
  )

(defn dutylist []
  (resp/json (db/duty-list))
  )

(defn getcurrentduty [day date]
  (resp/json (db/duty-query-day day date))
  )

(defn getmissions []
  (resp/json (db/mission-query))
  )
(defn savemission [request]
  (let [form-params (:form-params request)]
    (db/update-mission form-params (get form-params "id"))
    (resp/json {:success true})
    )
  )

(defn getdutymissions [day]
  (resp/json (db/mission-today-list day))
  )

(defn insertnewmission [userid]
  (db/mission-history-insert (let [missions (db/mission-query)]
    (map #(conj {:missionid (:id (select-keys % [:id]))} {:userid userid :missionstatus 0}) missions)
    ))
  (resp/json {:success true})
  )

(defn getworkmanagerevents [startDate endDate]
  (let [data (db/getworkmanagerevents startDate (str endDate "T"))]
    (resp/json {:success true :data (map #(conj {:ad true} %) data)})
    )

  )

(defn addworkmanagerevents [cid start end]
  (resp/json {:success true :data [{:cid cid :start start :end end
                                    :id (first (vals (db/addworkmanagerevents cid start end)))}]
              :msg "添加成功"} )
  )

(defn saveworkmanagerevents [id cid start end]
  (db/saveworkmanagerevents id {:userid cid :start start :end end})
  (resp/json {:success true :data [{:cid cid :start start :end end
                                    :id id}]
              :msg "保存成功"} )
  )

(defn deleteworkmanagerevents [id]
  (db/deleteworkmanagerevents id)
  (resp/json {:success true :data []
              :msg "删除成功"} )
  )

(defn getcalendars []
  (let [results (db/user-list)]
    (resp/json {:results (map #(conj {:color (if(even? (:id %)) (- 32 (:id %))(+ 0 (:id %) )) } %) results)})
    )

    )

(defn getstationinfo [stationcode]
  (resp/json (db/get-station-code stationcode))
  )
(defn getstations[keyword start limit]
  (resp/json
    {
      :results (db/stations-list keyword start limit )
      :totalCount (:counts (first (db/stations-count keyword start limit)))
      }
    )

  )
(defn addnewstation [values]

  (if (> (first (vals (db/savenewstation values))) 0) (resp/json {:success true})
    (resp/json {:success false :msg "插入数据失败"})
    )
  (resp/json {:success true})
  )
(defn savestation [values]
  (db/savestation values)
  (resp/json {:success true})

  )
(defn delstation [sid]
  (db/delstation sid)
  (resp/json {:success true})
  )
(defn mysqlalert []
  (resp/json (db/mysqlalert))
  )
(defn recordcheck [today yestoday]
  (let [results (db/is-suspend today yestoday)]
    (resp/json {:success (> (count results) 0) :result results})
    )
  )
(defn maketodaymission [day userid]
  (if (= (:counts (first (db/mission-history-query day userid))) 0)
    (insertnewmission userid)(resp/json {:success false}))
  )
(defn insertduty [day userid]
  (if (> (count (db/duty-query-day day)) 0) (resp/json {:success false :msg "已存在"})
    (if (> (first (vals (db/duty-insert day userid))) 0) (resp/json {:success true})
      (resp/json {:success false :msg "插入数据失败"})
      )
    )

  )
(defn insertmission [missionname missiontime missioninterval]
  (if (> (first (vals (db/mission-insert missionname missiontime missioninterval))) 0) (resp/json {:success true})
    (resp/json {:success false :msg "插入数据失败"})
    )
  )

(defn senddutylogs [dutylogs]
  (db/add-dutylog
    dutylogs)
  )

(defn copywavefile [sourcedir targetdir]
  (fs/copy-dir sourcedir targetdir)
  (resp/json {:success (fs/exists? sourcedir)})

  )
(defn checkarchive [sourcedir earthplatformlist archiveminsize]
  (println earthplatformlist)
  (let [earthplatformlist (db/stationcode-list)]
    (let [results (filter
                    #(false? (and (fs/child-of? sourcedir (str sourcedir (:stationcode %)))
                               (>= (fs/size (str sourcedir (:stationcode %))) (* (read-string archiveminsize) 1048576)))) earthplatformlist)]

      (resp/json {:success (empty? results) :results (map #(:stationcode %) results)})
      )

    )


  )

(defn completeduty [id dutylog]
  (db/completedutymission id dutylog)
  (resp/json {:success true})
  )
(defn eqimcheck [id username password url securl]
  (let [my-cs (clj-http.cookies/cookie-store)]

    (try
      (client/post url {:form-params {:username username :password password}
                        :socket-timeout 1000
                        :conn-timeout 1000
                        :cookie-store my-cs})
      (resp/json {:success true :msg (:body (client/get securl {:cookie-store my-cs}))})

      (catch Exception e (resp/json {:success false}))
      )

    )


  )

(defn newrecord [params]
  (try
    (client/post (:url params) {:form-params params  :socket-timeout 1000
                                :conn-timeout 1000})
    (resp/json {:success true :msg "ok"})
    (catch Exception e (resp/json {:success false}))
    )

  )

(defn eqimcheck-nologin [url]
  (let [my-cs (client/get url {:as :auto  :socket-timeout 1000
                               :conn-timeout 1000})]

    (try
      (println (:body my-cs))
      (resp/json {:success true :msg (:body my-cs)})

      (catch Exception e (resp/json {:success false}))
      )

    )


  )


(defn delenumbyid [request]
  (let [form-params (:form-params request)]
    (db/duty-del-byids (get form-params "enumids"))
    )
  )

