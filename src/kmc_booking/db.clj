(ns kmc-booking.db
	(:require [clojure.java.jdbc :as sql]))

(def bookings-table "bookings")
(def seats-table "seats")
(def testing-table "testing")

(defn gen-id [] (str (java.util.UUID/randomUUID)))

(def CONN 
	(or (System/getenv "DATABASE_URL")
		"postgresql://postgres:postgres@localhost:5432/misskma"))


(defn migrated? [table]
  (-> (sql/query CONN
                 [(str "select count(*) from information_schema.tables "
                       "where table_name=  ?") table])
      first :count pos?))


(defn drop-table! [table]
	(sql/with-db-connection [c CONN] 
		(sql/db-do-commands c
	                     (sql/drop-table-ddl table))
		)
	)

(defn create-testing-table[]
	(sql/db-do-commands CONN
	                    (sql/create-table-ddl :testing [:data :text]))
		(sql/insert! CONN
	                    :testing {:data "Hello World"}))

;; seats

(defn create-seats-table [] 
	(let [t (keyword seats-table)]
		(sql/with-db-connection [c CONN] 
		
			(sql/db-do-commands c
	                     (sql/create-table-ddl t
	                                            [:id "varchar(10)" :primary :key]
	                                            [:status "varchar(20)"]
	                                            [:booking_id "varchar(32)"]))
			;; TODO: create more free seats

			(apply sql/insert! c t 
				[:id :status]
				[
					["1-1" "free"]
					["1-2" "free"]]
				)

		)	
	)
)

(defn get-seats []
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  [(str "select * from " seats-table)])
		
		))

;; bookings

(defn create-bookings-table [] 
	(let [t (keyword bookings-table)]
		(sql/with-db-connection [c CONN] 
		
			(sql/db-do-commands c
	                     (sql/create-table-ddl t
	                                            [:id "varchar(32)" :primary :key]
	                                            [:name "varchar(32)"]
	                                            [:phone "varchar(16)"]
	                                            [:date :timestamp :default :current_timestamp]))
		)	
	)
)

(defn get-bookings []
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  [(str "select * from " bookings-table)])
		
		))

(defn create-booking [name phone seats]
	(let [booking-id (gen-id)]
		(sql/with-db-connection [c CONN] 
			(sql/insert! c bookings-table
	                  {
	                  	:id booking-id
	                  	:name name
	                  	:phone phone 
	                  }
			
			)
		)

	)
)

;;

(defn init-db[] 
	(when-not (migrated? testing-table)
		(create-testing-table))

	(when-not (migrated? seats-table)
		(create-seats-table))

	(when-not (migrated? bookings-table)
		(create-bookings-table))


	)


(defn get-data[] 
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  ["select * from testing"])
		
		))


