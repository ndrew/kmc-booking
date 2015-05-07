(ns ^:figwheel-always kmc-booking.components
  (:require [sablono.core :as sab]
  			[rum :as rum]
  	))


(defn seat-id [y x] 
	(str y "-" x))

(defn- attr[el n]
	(.getAttribute el n))


(defn- range-key [r] 
    [(first r) (last r)])

;;;;;

(rum/defc booking-info  < rum/cursored-watch [app-state]
	(let [seats (rum/cursor app-state [:seats])
		  booked (filter 
   					(fn [[k {status :status}]]
     					(= "pending" status)) @seats)]
		[:pre 
			(pr-str booked)
		]
	)
)

;;;


(rum/defc seat < rum/cursored-watch [y x ref]
	(let [current ref
		  seat-click (fn[e] 
				(let [el (.-target e)
					  id (seat-id (attr el "y") (attr el "x"))]
				;(println "foo")
				(swap! current assoc :status (if (= "free" (@current :status)) "pending" "free"))
				))

		]
		[:div {:x x :y y 
				:onClick seat-click
			 	:class (if-not @current "seat" ["seat" (get @current :status)]
			 				)
			 	;:class (reduce conj ["seat"] 
				;			(if-not (@seats id)
				;	 			[]
				;	 			[(get-in @seats [id :status])]))
				} ""]))


(rum/defc seat-cols < rum/static [range]
	(into [:div] (map #(vector :div.col_num [:div {:key %} %]) range)))


(rum/defc seat-row < rum/static [row]
	[:div.row_num 
		[:div {:key row} row]
		]
	)

(rum/defc seat-rows < rum/static [range class]
	(into [:div.rows {:class class}]
		 	[[:div.col_num {:key "spacer"} [:div {:__html "&nbsp;" :key 0}]]
		 	(map 
		 		#(rum/with-props seat-row % :rum/key %)
		 		range)]))



(rum/defc seat-block < rum/cursored-watch [root rows cols seats]
	(reduce conj root [
		(if (= 1 (count rows))
			nil
			(rum/with-props seat-cols cols :rum/key :top)
			)
		
		(map (fn [[y x]] 
					(rum/with-props seat y x 
						(rum/cursor seats [(seat-id y x)]) :rum/key [y x])) 
				 (for [y rows x cols] [y x]))

		(rum/with-props seat-cols cols :rum/key :bottom)
		])
)		


(rum/defc seat-plan < rum/cursored-watch rum/reactive [app-state]
	(let [seats (rum/cursor app-state [:seats])

		  side_rows (range 1 12)
		  house_rows (range 1 14)

		  left_side_cols (reverse (range 37 40))
		  right_side_cols (reverse (range 1 4))

		  left_house_cols (reverse (range 20 37))
		  right_house_cols (reverse (range 4 20)) 		

		  back_house_rows (range 14 21)
	      beletage_cols (range 1 32)

	      beletage_last_cols (range 1 26)
	      beletage_last_rows (range 21 22)
		
	      parter-rows (range 1 14)
		]

		[:div.seat-plan {:key "root"}
			[:div#parter {:key "parter"} 
				(rum/with-props seat-rows parter-rows "left" :rum/key (str "left_" (range-key parter-rows)))
				(rum/with-props seat-rows parter-rows "right" :rum/key (str "right_" (range-key parter-rows)))

				(seat-block [:div#left_side_house.side_house] side_rows left_side_cols seats)
				(seat-block [:div#left_house.house] house_rows left_house_cols seats)
				(seat-block [:div#right_house.house] house_rows right_house_cols seats)
				(seat-block [:div#right_side_house.side_house] side_rows right_side_cols seats)
				]
			[:div#back_house {:key "back_house"}
				(rum/with-props seat-rows house_rows "left" :rum/key (str "left_" (range-key house_rows)))
				(rum/with-props seat-rows house_rows "right" :rum/key (str "right_" (range-key house_rows)))

				(seat-block [:div#beletage] back_house_rows beletage_cols seats)
				]

			[:div#last_row {:key "last_row"}
				(rum/with-props seat-rows beletage_last_rows "left" :rum/key (str "left_" (range-key beletage_last_rows)))
				(rum/with-props seat-rows beletage_last_rows "right" :rum/key (str "right_" (range-key beletage_last_rows)))

				(seat-block [:div#beletage_last_row] beletage_last_rows beletage_last_cols seats)
				]


			]
		)


)





