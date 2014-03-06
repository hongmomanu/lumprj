(ns lumprj.controller.log
  (:use compojure.core)
  (:require [lumprj.models.db :as db]
            [noir.response :as resp]
            )
  )

(defn log-system-statics [params]
  (let [searchtype (:type params)]
    (if (= searchtype "duty")
      (resp/json (db/log-duty-statics params))
      (resp/json (db/log-system-statics params))
    )
  )
  )
(defn log-system-statics-dayinfo [day searchtype]
  (if (= searchtype "duty")
    (resp/json (db/log-duty-statics-dayinfo day))
    (resp/json (db/log-system-statics-dayinfo day))
    )
  )

(defn log-system-list [params]

     (resp/json
        {
         :results (db/log-list params)
         :totalCount (:counts (first (db/log-count params)))
        }
       )

  )
(defn log-duty-list  [params]

     (resp/json
        {
         :results (db/log-duty-list params)
         :totalCount (:counts (first (db/log-duty-count params)))
        }
       )

  )

