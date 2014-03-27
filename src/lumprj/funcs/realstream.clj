(ns lumprj.funcs.realstream

  (:import
           (java.io  BufferedReader InputStreamReader)
           (edu.iris.miniseedutils.steim GenericMiniSeedRecord)
           (java.util StringTokenizer))
  )


(defn decodeminirtdata  [x bis buf recLen]
  (.read bis buf 0 recLen)

  ;long check_ms = gmsRec.getStartTime().getTime() / 1000L * 1000L +
  ;;(gmsRec.getStartTime().getNanos()) / 1000000;
  ;;(-> (.getStartTimem gmsRec) (.getNanos))
  (let [
         gmsRec (GenericMiniSeedRecord/buildMiniSeedRecord buf)
         updata (make-array Integer/TYPE (.getNumSamples gmsRec))]
    (if(.decompress gmsRec updata)()())

    {:stationname (str (.getStation gmsRec) "/" (.getChannel gmsRec))
     :data (into [] updata)
     :time (.getStartTime gmsRec)
     }
    )

  )