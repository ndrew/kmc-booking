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



(defn booking [req]
  (let [{params :params} req]
      params
    )
)


(defn seats []
  (when (empty? @core/seats) ;; for figwheel
    (core/init-seats! (db/get-seats)))

  @core/seats
)


(defn admin[]
  ;(friend/current-authentication)
  (io/resource "admin.html")
)


(defroutes routes
  (GET "/" [] (ring/redirect "landing/index.html"))
  ;(GET  "/" [] (index))
  
  (POST "/booking" req (booking req))

  (friend/logout (ANY "/logout" request 
                              (ring/redirect "/")))

  (GET "/admin" req 
    (friend/authenticated 
      (admin)))

  (wrap-transit-response 
    (compojure.core/context "/api" []
          (GET "/seats" [] {:body (seats)})
          (POST "/book" req {:body (booking req)})))

)

