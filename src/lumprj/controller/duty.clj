(ns lumprj.controller.duty
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )
  )

(defn dutylist []
  (resp/json (db/duty-list))
  )

(defn getcurrentduty [day]
  (resp/json (db/duty-query-day day))
  )

(defn getmissions []
  (resp/json (db/mission-query))
  )

(defn insertnewmission [userid]
  (db/mission-history-insert (let [missions (db/mission-query)]
    (map #(conj (select-keys % [:id]) {:userid userid}) missions)
    ))
  )

(defn maketodaymission [day userid]
  (if (= (:counts (first (db/mission-history-query day userid))) 0)(println (insertnewmission userid))())
  (resp/json {:success true :msg "测试成功"})
  )

(defn insertduty [day userid]
  (if (> (count (db/duty-query-day day)) 0) (resp/json {:success false :msg "已存在"})
    (if (> (first (vals (db/duty-insert day userid))) 0) (resp/json {:success true})
      (resp/json {:success false :msg "插入数据失败"})
      )
    )

  )
(defn insertmission [missionname missiontime]
  (if (> (first (vals (db/mission-insert missionname missiontime))) 0) (resp/json {:success true})
    (resp/json {:success false :msg "插入数据失败"})
    )
  )

(defn delenumbyid [request]
  (let [form-params (:form-params request)]
    (db/duty-del-byids (get form-params "enumids"))
    )
  )

