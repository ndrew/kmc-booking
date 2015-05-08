(ns kmc-booking.web
	(:require [compojure.core :refer [defroutes]]
	          [ring.adapter.jetty :as ring]
	          [compojure.route :as route]
	          [compojure.handler :as handler]
            
            [ring.middleware.params :as params]

            [kmc-booking.routing :as rounting]
            [kmc-booking.db :as db]
            [kmc-booking.core :as core]


            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])


            )
	(:gen-class))




(defroutes routes
  rounting/routes
  (route/resources "/")
); (route/not-found (layout/four-oh-four))


(def app* (-> 
            (handler/site routes)
            (params/wrap-params)
            ))


(def users (atom {"admin" {:username "admin"
                    :password "$2a$10$1TjUe7363gEbtNJwG5FnMOb3.o3AsZL4cqcLUc5iSYPNgasR9Es1u"
                    :roles #{::admin}}})

)


(def application (friend/authenticate
                   app*
                   {:allow-anon? true
                    :unauthenticated-handler #(workflows/http-basic-deny "kmc booking" %)
                    :workflows [(workflows/http-basic
                                 :credential-fn #(creds/bcrypt-credential-fn @users %)
                                 :realm "You shall not pass!")]}))



(defn init[] 
  (db/init-db)
  (core/init-seats! (db/get-seats)))



(defn start [port]
  (init)
  (ring/run-jetty application {:port port
                               :join? false}))


(defn destroy[] 
  ;; 
)


;; uncomment for figwheel
;;(init)


(defn -main 
  "for heroku"
  []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))