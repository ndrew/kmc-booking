(ns kmc-booking.db
	(:require [clojure.java.jdbc :as sql]))

(def bookings-table "bookings")
(def seats-table "seats")
(def testing-table "testing")



(def CONN 
	(or (System/getenv "DATABASE_URL")
		"postgresql://postgres:postgres@localhost:5432/misskma"))


(defn migrated? [table]
  (-> (sql/query CONN
                 [(str "select count(*) from information_schema.tables "
                       "where table_name=  ?") table])
      first :count pos?))


(defn create-testing-table[]
	(sql/db-do-commands CONN
	                    (sql/create-table-ddl :testing [:data :text]))
		(sql/insert! CONN
	                    :testing {:data "Hello World"}))

(defn create-seats-table [] 
	(let [t (keyword seats-table)]
		(sql/with-db-connection [c CONN] 
		
			#_(sql/db-do-commands c
	                     (sql/create-table-ddl t
	                                            [:id "varchar(10)"]
	                                            [:status "varchar(20)"]
	                                            [:booking_id :int]))
			(sql/insert! c t 
				{
					:id "1-1"
					:status "free"
					:booking_id -1
				})

		)	

		)
	;(sql/db-do-commands CONN
	;                    (sql/create-table-ddl :testing [:data :text]))

	;(jdbc/db-do-commands db-spec "CREATE INDEX name_ix ON fruit ( name )")
	
)

(defn get-seats []
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  [(str "select * from " seats-table)])
		
		))


(defn init-db[] 
	(when-not (migrated? testing-table)
		(create-testing-table))

	(when-not (migrated? seats-table)
		(create-seats-table))

	)


(defn get-data[] 
	(sql/with-db-connection [c CONN] 
		(sql/query c
                  ["select * from testing"])
		
		))


