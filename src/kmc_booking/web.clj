(ns kmc-booking.web
	(:require [compojure.core :refer [defroutes]]
	          [ring.adapter.jetty :as ring]
	          [compojure.route :as route]
	          [compojure.handler :as handler])
	(:gen-class))


(defroutes routes
  ;;shouts/routes
  (route/resources "/")
  ;(route/not-found (layout/four-oh-four))
  )

(def application (handler/site routes))

(defn init[] 
  ;; 
  )

(defn destroy[] 
  ;; 
  )


(defn start [port]
  (ring/run-jetty application {:port port
                               :join? false}))

(defn -main []
  ;(schema/migrate)
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))