(ns kmc-booking.routing
	(:require [compojure.core :refer [defroutes GET POST]]
              [clojure.string :as str]
              [ring.util.response :as ring]
              [kmc-booking.db :as db]
              ))


(defn index []
	;(db/init-db)
	;"fooooooooo!"
	(pr-str (db/get-data))

	)

(defroutes routes
  (GET  "/" [] (index))
  (GET "/welcome-message" []
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    "Hello world from server!"})
  )