(ns kmc-booking.web
	(:require [compojure.core :refer [defroutes]]
	          [ring.adapter.jetty :as ring]
	          [compojure.route :as route]
	          [compojure.handler :as handler]

            [ring.middleware.params :as params]

            [kmc-booking.core :as core]
            [kmc-booking.routing :as rounting]


            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])


            )
	(:gen-class))




(defroutes routes
  rounting/routes
  rounting/api
  (route/resources "/")
)


(def app* (->
            (handler/site routes)
            (params/wrap-params)
            ))


(def users (atom {"admin" {:username "admin"
                    :password (:admin-path core/config)
                    :roles #{::admin}}})
)


(def application (friend/authenticate
                   app*
                   {:allow-anon? true
                    :unauthenticated-handler #(workflows/http-basic-deny "kmc booking" %)
                    :workflows [(workflows/http-basic
                                 :credential-fn #(creds/bcrypt-credential-fn @users %)
                                 :realm "You shall not pass!")]}))




(defn start [port]
  (core/init!)
  (ring/run-jetty application {:port port
                               :join? false}))


(defn destroy[]
  ;;
)


;; uncomment for figwheel
;;(init!)


(defn -main
  "for heroku"
  []
  (let [port (Integer. (or (System/getenv "PORT") "8081"))]
    (start port)))
