(ns lumprj.models.dboracle
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [lumprj.models.schema :as schema]))

(defdb dboracle schema/db-oracle)



(defentity t_doorplate)




(defn oracltest []
  (exec-raw ["SELECT count(*) FROM t_doorplate" []] :results)
  )

