(ns lumprj.controller.duty
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )
  )



(defn dutylist []
  (resp/json (db/duty-list))
  )

(defn insertduty [day userid]
  (resp/json (db/duty-insert day userid))
  )

