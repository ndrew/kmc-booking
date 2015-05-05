(ns kmc-booking.routing
	(:require [compojure.core :refer [defroutes GET POST]]
              [clojure.string :as str]
              [ring.util.response :as ring]
              [kmc-booking.db :as db]
              [kmc-booking.core :as core]
              ))


(defn booking []
  ;; also keep-alive
	
  #_(str 
    (pr-str @core/seats)
    "<hr>"
    (pr-str (db/get-data))

    )

  #_(pr-str (db/migrated? db/testing-table))


  (db/drop-table! db/seats-table)
  (db/drop-table! db/bookings-table)
  
  (pr-str (db/create-seats-table))
  (pr-str (db/create-bookings-table))  
  
  (pr-str (db/get-seats))


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
