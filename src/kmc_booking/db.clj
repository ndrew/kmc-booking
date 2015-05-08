(ns kmc-booking.db
	(:require [clojure.java.jdbc :as sql]))


(defn gen-id [] (str (java.util.UUID/randomUUID)))

(def CONN 
	(or (System/getenv "DATABASE_URL")
		"postgresql://postgres:postgres@localhost:5432/misskma"))


(def seat-schema  ;; [[row-start col-start] [row-end col-end]]
	[[[1 1] [12 40]]
     [[12 4] [14 37]]
     [[14 1] [21 32]]
     [[21 1] [22 26]]])


;; as described in seat-scema
(defn gen-seats-data [blocks]
  (reduce (fn[a [[row1 col1] [row2 col2]]] 
            (into a (for [rows (range row1 row2)
                          cols (range col1 col2)]
                      {:id (str rows "-" cols)
                       :status "free"
                  		}))
            ) [] blocks))

;;;;;;;;;
;; DDL


(defn migrated? [table]
  (-> (sql/query CONN
                 [(str "select count(*) from information_schema.tables "
                       "where table_name=  ?") table])
      first :count pos?))

(defn migrate-live! [id cb]
	(sql/with-db-connection [c CONN] 
		(when-not (seq (sql/query c
                  		["select * from testing where data = ?" id]))

			(cb)
			(sql/insert! c :testing {:data id}))))


(defn drop-table! [table]
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                     (sql/drop-table-ddl table))))


(defn create-testing-table! []
	(sql/db-do-commands CONN
	                    (sql/create-table-ddl :testing [:data :text]))
		(sql/insert! CONN
	                    :testing {:data "Hello World"}))


;; seats

(defn create-seats-table! [] 
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                    (sql/create-table-ddl "seats"
	                                           [:id "varchar(10)" :primary :key]
	                                           [:status "varchar(20)"]
	                                           [:booking_id "varchar(48)"]))
		(apply sql/insert! c "seats" 
			(gen-seats-data seat-schema))))

(defn create-bookings-table! [] 
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                    (sql/create-table-ddl "bookings"
	                                           [:id "varchar(48)" :primary :key]
	                                           [:name "varchar(128)"]
	                                           [:phone "varchar(16)"]
	                                           [:date :timestamp :default :current_timestamp]))))


(defn create-history-table! []
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                    (sql/create-table-ddl "history"
	                                           [:booking_id "varchar(48)"]
	                                           [:id "varchar(10)"]
	                                           [:status "varchar(20)"]
	                                           [:date :timestamp :default :current_timestamp]
	                                           ))))

;;;;;;;;;
;; DML


(defn get-test-data[] 
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  ["select * from testing"])))


(defn get-seats []
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  ["select * from seats"])))


(defn get-bookings []
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  ["select * from bookings"])))




;; TODO: trasaction
;; TODO: Check seats availability
;; TODO: Update seats with status 
(defn create-booking [name phone seats]
	(let [booking-id (gen-id)
		  records (for [rows (range 3 4)
                 	  cols (range 4 37)]
                      {:id (str rows "-" cols)
                       :status "paid"
                       :booking_id booking-id})]

		(sql/with-db-connection [c CONN] 
			(sql/insert! c "bookings"
	                  {
	                  	:id booking-id
	                  	:name name
	                  	:phone phone 
	                  })

			(apply sql/insert! c "history" records)

			(doseq [r records]
				(sql/update! c "seats" r ["id = ?" (get r :id)]))
		)

		
	)
)


;;;;;;;;;;;;;;;
;;
;; migrations

(defn- migrate__reinit_seats! []
	(sql/with-db-connection [c CONN] 
		(sql/delete! c "seats" ["1 = 1"])
		(apply sql/insert! c "seats" 
			(gen-seats-data seat-schema))))

(defn migrate__seats_for_judges! []
	(create-booking "ЖУРІ" "-" [])
	)


(defn init-db[] 
	(when-not (migrated? "testing")
		(create-testing-table!))

	(when-not (migrated? "seats")
		(create-seats-table!))

	(when-not (migrated? "bookings")
		(create-bookings-table!))

	(when-not (migrated? "history")
		(create-history-table!))
	
	;(try
	;	...
	;	(catch Exception e 
	;		(do 
	;			(print (.getNextException e)))))

	(migrate-live! "1_reinit_seats" migrate__reinit_seats!)

	(migrate-live! "2_seats_for_judges"  migrate__seats_for_judges!)

)











