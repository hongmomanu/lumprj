(ns lumprj.routes.home
  (:use compojure.core)
  (:require [lumprj.views.layout :as layout]
            [noir.response :as resp]
            [lumprj.util :as util]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (resp/redirect "index.html"))
  ;;(GET "/adduser" [] (about-page))
  (GET "/about" [] (about-page)))


