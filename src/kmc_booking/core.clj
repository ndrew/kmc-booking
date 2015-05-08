(ns kmc-booking.core)

;; app 

(defonce seats (atom {}))

(defn init-seats! [db-seats]
	(reset! seats 
		(reduce (fn[a v] 
				   (assoc a (get v :id) (dissoc v :id))
				 ) {} db-seats)))
