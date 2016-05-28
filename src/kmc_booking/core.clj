(ns kmc-booking.core
  (:require
    [clojure.java.io :as io]
    ;[kmc-booking.mockdb :as db]
    [kmc-booking.db :as db]
    ))

;; app config
(defonce config (read-string (slurp (io/resource "config.edn"))))


;; global state


(defonce seats (atom {}))

(defn init-seats! [db-seats]
	(reset! seats
		(reduce (fn[a v]
				   (assoc a (get v :id) (dissoc v :id))
				 ) {} db-seats)))


(defn seat-booked! [id booking-id status]
	;;(println id booking-id status)
	(swap! seats update-in [id] merge {:booking_id booking-id :status status}))



;; booking service


(defn init![] ;; start app - collect state
  (db/init-db config)
  (init-seats! (db/get-seats)))


(defn create-booking [name phone seats] ;; booking-id
  (let [booking-id (db/create-booking name phone seats)]
    (doseq [id seats]
      (seat-booked! id booking-id "pending"))
    booking-id))


(defn get-bookings []
	(db/get-bookings))


(defn cancel-booking [booking-id]
  (db/cancel-booking booking-id)
  (init-seats! (db/get-seats))
  booking-id
)

(defn confirm-booking [booking-id]
  (db/confirm-booking booking-id)
  (init-seats! (db/get-seats))
  booking-id
)


(defn playground[]



  (db/init-db config)
  (pr-str
    (db/get-seats)
    )

  )

