(ns lumprj.routes.home
  (:use compojure.core)
  (:require [lumprj.views.layout :as layout]
            [lumprj.util :as util]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  ;;(GET "/adduser" [] (about-page))
  (GET "/about" [] (about-page)))


