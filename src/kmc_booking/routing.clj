(ns kmc-booking.routing
	(:require [compojure.core :refer [defroutes GET POST]]
              [clojure.string :as str]
              [ring.util.response :as ring]
              [kmc-booking.db :as db]
              [kmc-booking.core :as core]
              ))


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




(defroutes routes
  (GET "/" [] (ring/redirect "landing/index.html"))
  ;(GET  "/" [] (index))
  (GET  "/booking" [] (booking))
  (GET "/welcome-message" []
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    "Hello world from server!"})
  )

