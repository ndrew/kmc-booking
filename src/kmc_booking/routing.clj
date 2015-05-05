(ns kmc-booking.routing
	(:require [compojure.core :refer [defroutes GET POST]]
              [clojure.string :as str]
              [ring.util.response :as ring]
              [kmc-booking.db :as db]
              [kmc-booking.core :as core]
              ))


(defn index []
	;(db/init-db)
	;"fooooooooo!"
	(pr-str (db/get-data))
)

(defn booking []
  ;; also keep-alive
	(pr-str @core/seats)
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
