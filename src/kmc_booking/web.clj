(ns kmc-booking.web
	(:require [compojure.core :refer [defroutes]]
	          [ring.adapter.jetty :as ring]
	          [compojure.route :as route]
	          [compojure.handler :as handler]
            [kmc-booking.routing :as rounting]
            [kmc-booking.db :as db]
            [kmc-booking.core :as core])
	(:gen-class))


(defroutes routes
  rounting/routes
  (route/resources "/")
  ;(route/not-found (layout/four-oh-four))
  )

(def application (handler/site routes))



(defn init[] 
  (db/init-db)
  ;; load seats from db
  ;; (defonce seats)
  (swap! core/seats assoc [1 5] :reserved)
)



(defn destroy[] 
  ;; 
  )



(defn start [port]
  (ring/run-jetty application {:port port
                               :join? false}))

(defn -main []
  (init)
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))