(ns kmc-booking.db
	(:require [clojure.java.jdbc :as sql]))


(def bookings-table "bookings")
(def seats-table "seats")
(def testing-table "testing")

(defn gen-id [] (str (java.util.UUID/randomUUID)))

(def CONN 
	(or (System/getenv "DATABASE_URL")
		"postgresql://postgres:postgres@localhost:5432/misskma111"))


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
	(let [t (keyword seats-table)]
		(sql/with-db-connection [c CONN] 
			(sql/db-do-commands c
	                     (sql/create-table-ddl t
	                                            [:id "varchar(10)" :primary :key]
	                                            [:status "varchar(20)"]
	                                            [:booking_id "varchar(32)"]))
			(apply sql/insert! c t 
				(gen-seats-data seat-schema)))))

(defn create-bookings-table [] 
	(let [t (keyword bookings-table)]
		(sql/with-db-connection [c CONN] 
		
			(sql/db-do-commands c
	                     (sql/create-table-ddl t
	                                            [:id "varchar(48)" :primary :key]
	                                            [:name "varchar(128)"]
	                                            [:phone "varchar(16)"]
	                                            [:date :timestamp :default :current_timestamp]))
		)	
	)
)


(defn init-db[] 
	(when-not (migrated? testing-table)
		(create-testing-table))

	(when-not (migrated? seats-table)
		(create-seats-table))

	(when-not (migrated? bookings-table)
		(create-bookings-table))

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
                  [(str "select * from " seats-table)])))


(defn get-bookings []
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  [(str "select * from " bookings-table)])))




;; TODO: Ideally in one trasaction
;; TODO: Check seats availability
;; TODO: Apdate seats with status 
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
		booking-id
	)
)





