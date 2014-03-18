(ns lumprj.controller.realstream
  (:import (cn.org.gddsn.liss.client LissClient LissException LissTransferType)
           )
  (:use compojure.core)
  (:require [lumprj.models.db :as db]

            [noir.response :as resp]
            )
  )

(defn readrealstream []

  (println (LissTransferType/BINARY))
  ;(let [lissClient (new LissClient "127.0.0.1" 23)]

  ;  (.login lissClient "jack" "shayu626")
  ;  )

  ;;(resp/json (dboracle/oracltest))
  )