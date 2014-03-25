(ns lumprj.handler
  (:require [compojure.core :refer [defroutes]]
            [lumprj.routes.home :refer [home-routes]]
            [lumprj.routes.user :refer [user-routes]]
            [lumprj.routes.server :refer [server-routes]]
            [lumprj.routes.realstream :refer [realstream-routes]]
            [lumprj.routes.duty :refer [duty-routes]]
            [lumprj.routes.log :refer [log-routes]]
            [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [lumprj.models.schema :as schema]
            [lumprj.controller.server :as server]
            [lumprj.controller.realstream :as realstream]
            [com.postspectacular.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/append})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "lumprj.log" :max-size (* 512 1024) :backlog 10})



  (if (env :selmer-dev) (parser/cache-off!))

  (if-not (schema/initialized?) (schema/create-tables))
  (realstream/makerealstreamcache) ;;生成数据缓存
  (server/update-ssh-list) ;;更新ssh列表
  (timbre/info "lumprj started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "lumprj is shutting down..."))

(defn template-error-page [handler]
  (if (env :selmer-dev)
    (fn [request]
      (try
        (handler request)
        (catch clojure.lang.ExceptionInfo ex
          (let [{:keys [type error-template] :as data} (ex-data ex)]
            (if (= :selmer-validation-error type)
              {:status 500
               :body (parser/render error-template data)}
              (throw ex))))))
    handler))

(def app (middleware/app-handler
           ;; add your application routes here
           [user-routes server-routes realstream-routes home-routes duty-routes log-routes app-routes]
           ;; add custom middleware here
           :middleware [template-error-page]
           ;; add access rules here
           :access-rules []
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:json-kw :edn :json :jsonp]))
