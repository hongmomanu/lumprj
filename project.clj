(defproject
  lumprj
  "0.1.0-SNAPSHOT"
  :dependencies
  [[com.h2database/h2 "1.3.174"]
   [org.xerial/sqlite-jdbc "3.7.15-M1"]
   [com.oracle/ojdbc6 "11.2.0.3"]
   ;;[self/sigar "1.6.4"]
   [ch.ethz.ganymed/ganymed-ssh2 "261"]
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
  :repositories [
                  ["java.net" "http://download.java.net/maven/2"]
                  ["nexus" "https://code.lds.org/nexus/content/groups/main-repo"]
                 ["sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                              ;; If a repository contains releases only setting
                              ;; :snapshots to false will speed up dependencies.
                              :snapshots false
                              ;; Disable signing releases deployed to this repo.
                              ;; (Not recommended.)
                              :sign-releases false
                              ;; You can also set the policies for how to handle
                              ;; :checksum failures to :fail, :warn, or :ignore.
                              :checksum :fail
                              ;; How often should this repository be checked for
                              ;; snapshot updates? (:daily, :always, or :never)
                              :update :always
                              ;; You can also apply them to releases only:
                              :releases {:checksum :fail :update :always}}]
                 ;; Repositories named "snapshots" and "releases" automatically
                 ;; have their :snapshots and :releases disabled as appropriate.
                 ;; Credentials for repositories should *not* be stored
                 ;; in project.clj but in ~/.lein/credentials.clj.gpg instead,
                 ;; see `lein help deploying` under "Authentication".
                 ]
  :description
  "FIXME: write description"
  ;;:jvm-opts ["-Djava.library.path=/home/jack/soft/lumprj/target/native"]
  :java-source-paths ["src/lumprj/java" ] ; Java source is stored separately.
  ;;:native-path "native"
  ;;:resource-paths ["lib/*"]
  :min-lein-version "2.0.0")
