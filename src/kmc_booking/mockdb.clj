(ns kmc-booking.mockdb)


(defonce seats-table (atom []))
(defonce bookings-table (atom []))

;; as described in seat-scema
(defn gen-seats-data [blocks]
  (reduce (fn[a [[row1 col1] [row2 col2]]]
            (into a (for [rows (range row1 row2)
                          cols (range col1 col2)]
                      {:id (str rows "-" cols)
                       :status "free"
                  		}))
            ) [] blocks))


(defn init-db[config]
  ;; ;; [[row-start col-start] [row-end col-end]]
  (reset! seats-table (gen-seats-data (:seats-plan config)))
)

(defn get-seats []
	@seats-table
  )


(defn get-bookings []
	[])


(defn create-booking [name phone seats]
  :booking-id
  )

(defn cancel-booking [booking-id]
  )

(defn confirm-booking [booking-id]
  )


(comment





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




(defn cancel-booking [booking-id]
	(println (str "deleting " booking-id))
	(sql/with-db-connection [c CONN]
		(sql/delete! c "history" ["booking_id = ?" booking-id ])
		(sql/update! c "seats" {:status "free" :booking_id nil} ["booking_id = ?" booking-id])
		(sql/delete! c "bookings" ["id = ?" booking-id ])

		))


(defn confirm-booking [booking-id]
	(println (str "confirming " booking-id))
	(sql/with-db-connection [c CONN]
		(sql/update! c "history" {:status "paid" } ["booking_id = ?" booking-id ])
		(sql/update! c "seats" {:status "paid" :booking_id booking-id} ["booking_id = ?" booking-id])

		))




;; TODO: trasaction
;; TODO: Check seats availability
;; TODO: Update seats with status
(defn create-booking [name phone seats]
	(let [booking-id (gen-id)
		  records (map (fn[id] {:id id
		              :status "pending"
		              :booking_id booking-id}) seats)]

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

			booking-id
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
	(drop-table! "seats")
	(create-seats-table!)
	(create-booking "ЖУРІ & передзамовлення" "-"
		(concat
          (for [rows (range 3 4) cols (range 4 37)] (str rows "-" cols))
          (for [rows (range 1 3) cols (range 1 4)] (str rows "-" cols))
          (for [rows (range 1 3) cols (range 37 40)] (str rows "-" cols))
          (for [rows (range 11 12) cols (range 20 26)] (str rows "-" cols)))))


  )
