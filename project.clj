(defproject
  lumprj
  "0.1.0-SNAPSHOT"
  :dependencies
  [[com.h2database/h2 "1.3.174"]
   [org.xerial/sqlite-jdbc "3.7.2"]
   [ring-server "0.3.1"]
   [lein-ring "0.8.8"]
   [environ "0.4.0"]
   [com.taoensso/timbre "2.7.1"]
   [markdown-clj "0.9.35"]
   [korma "0.3.0-RC6"]
   [com.taoensso/tower "2.0.0"]
   [selmer "0.5.3"]
   [org.clojure/clojure "1.5.1"]
   [log4j
    "1.2.17"
    :exclusions
    [javax.mail/mail
     javax.jms/jms
     com.sun.jdmk/jmxtools
     com.sun.jmx/jmxri]]
   [compojure "1.1.6"]
   [lib-noir "0.7.6"]
   [com.postspectacular/rotor "0.1.0"]]
  :ring
  {:handler lumprj.handler/app,
   :init lumprj.handler/init,
   :destroy lumprj.handler/destroy}
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? true :auto-refresh? true}},
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.1"]],
    :env {:selmer-dev true}}}
  :url
  "http://example.com/FIXME"
  :aot
  :all
  :plugins
  [[lein-ring "0.8.8"] [lein-environ "0.4.0"] [ring-refresh "0.1.1"][ring/ring-jetty-adapter "1.2.1"]]
  :description
  "FIXME: write description"
  :min-lein-version "2.0.0")
