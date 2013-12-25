(ns lumprj.models.dboracle
  (:use korma.core
        [korma.db :only [defdb with-db]])
  (:require [lumprj.models.schema :as schema]))

(defdb dboracle schema/db-oracle)






(defentity t_doorplate
  (database dboracle)
  )

(defn oracltest []
  (with-db dboracle
    (exec-raw ["SELECT count(*) FROM t_doorplate" []] :results))
  )

