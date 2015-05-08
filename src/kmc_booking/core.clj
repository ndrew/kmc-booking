(ns kmc-booking.core)

;; app 

(defonce seats (atom {}))

(defn init-seats! [db-seats]
	(reset! seats 
		(reduce (fn[a v] 
				   (assoc a (get v :id) (dissoc v :id))
				 ) {} db-seats)))


(defn seat-booked! [id booking-id status]
	(println id booking-id status)

	(swap! seats update-in [id] merge {:booking_id booking-id :status status}))

