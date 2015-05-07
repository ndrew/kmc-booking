(ns kmc-booking.routing
	(:require [compojure.core :refer [defroutes GET POST ANY]]
               [clojure.string :as str]

               [ring.util.response :as ring]
               [ring.middleware.params :as params]

               [kmc-booking.db :as db]
               [kmc-booking.core :as core]
 
               [cemerick.friend :as friend]
               (cemerick.friend [workflows :as workflows]
                                [credentials :as creds])

               [cognitect.transit :as transit]
               [clojure.java.io :as io]
              )
  (:import
      [java.io ByteArrayOutputStream ByteArrayInputStream])
  )


(defn write-transit-bytes [x]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos :json {})]
    (transit/write w x)
    (.toByteArray baos)))

(defn wrap-transit-response [handler]
  (fn [request]
    (-> (handler request)
       (update-in [:headers] assoc "Content-Type" "application/transit+json; charset=utf-8")
       (update-in [:body] #(io/input-stream (write-transit-bytes %))))))



(defn booking []
  ;; also keep-alive
	
  #_(pr-str (db/migrated? db/testing-table))


  #_(do 
    (when (db/migrated? db/seats-table) (db/drop-table! db/seats-table))
    (when (db/migrated? db/bookings-table) (db/drop-table! db/bookings-table))
    
    (pr-str (db/create-seats-table))
    (pr-str (db/create-bookings-table))  
  )

  #_(pr-str (db/get-seats))
  #_(pr-str (str "Booking created: " (db/create-booking "Test User" "093777764" [])))

  (str 
    (pr-str @core/seats)
    "<hr>"
    (pr-str (db/get-test-data))
    )

)


(defn seats []

  ;(when (empty? @core/seats) ;; for figwheel
  ;  (core/init-seats! (db/get-seats)))

  @core/seats
)


(defn admin[]
  "I rule everything!"
  )


(defroutes routes
  ;(GET "/" [] (ring/redirect "landing/index.html"))
  (GET  "/" [] (ring/redirect "bookings.html"))
  
  (GET "/booking" [] (booking))

  (friend/logout (ANY "/logout" request 
                              (ring/redirect "/")))

  (GET "/admin" req 
    (friend/authenticated 
      (str "You have successfully authenticated as "
                                  (friend/current-authentication))))

  (compojure.core/context "/api" []
    (wrap-transit-response 
        (GET "/seats" request {:body (seats)})))
   
)

