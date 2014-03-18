(ns lumprj.controller.realstream
  (:import (cn.org.gddsn.liss.client LissClient LissException LissTransferType)
           (edu.iris.miniseedutils.steim GenericMiniSeedRecord)
           (cn.org.gddsn.liss.util LissClientReader)
           )
  (:use compojure.core)
  (:require [lumprj.models.db :as db]

            [noir.response :as resp]
            )
  )

(defn readrealstream []

  (println (LissTransferType/BINARY))

  (.start (new Thread (new LissClientReader "127.0.0.1" "user" "dummy"
                                   "/home/jack/test/test.BJT" 15 "BJT"
                                   "headFile.BJT")))

  ;(let [lissClient (new LissClient "127.0.0.1" 5000)]

  ;  (.login lissClient "jack" "shayu626")
  ;  )

  ;;(resp/json (dboracle/oracltest))
  )