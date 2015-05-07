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


(defn create-testing-table[]
	(sql/db-do-commands CONN
	                    (sql/create-table-ddl :testing [:data :text]))
		(sql/insert! CONN
	                    :testing {:data "Hello World"}))


;; seats

(defn create-seats-table [] 
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                    (sql/create-table-ddl "seats"
	                                           [:id "varchar(10)" :primary :key]
	                                           [:status "varchar(20)"]
	                                           [:booking_id "varchar(32)"]))
		(apply sql/insert! c "seats" 
			(gen-seats-data seat-schema))))

(defn create-bookings-table [] 
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                    (sql/create-table-ddl "bookings"
	                                           [:id "varchar(48)" :primary :key]
	                                           [:name "varchar(128)"]
	                                           [:phone "varchar(16)"]
	                                           [:date :timestamp :default :current_timestamp]))))


;;;;;;;;;;;;;;;
;;
;; migrations

(defn- migrate__reinit_seats![]
	(sql/with-db-connection [c CONN] 
		(sql/delete! c "seats" ["1 = 1"])
		(apply sql/insert! c "seats" 
			(gen-seats-data seat-schema))))


(defn init-db[] 
	(when-not (migrated? "testing")
		(create-testing-table))

	(when-not (migrated? "seats")
		(create-seats-table))

	(when-not (migrated? "bookings")
		(create-bookings-table))

	(migrate-live! "1_reinit_seats" migrate__reinit_seats!)

)








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




;; TODO: Ideally in one trasaction
;; TODO: Check seats availability
;; TODO: Apdate seats with status 
(defn create-booking [name phone seats]
	(let [booking-id (gen-id)]
		(sql/with-db-connection [c CONN] 
			(sql/insert! c "bookings"
	                  {
	                  	:id booking-id
	                  	:name name
	                  	:phone phone 
	                  }
			)
		)
		booking-id
	)
)





