(ns lumprj.funcs.conmmon


  )


(defn sum [arr]
  (apply + arr)
    )
(defn average [coll]

  (let [sum (apply + coll)]
    (quot sum (count coll))))