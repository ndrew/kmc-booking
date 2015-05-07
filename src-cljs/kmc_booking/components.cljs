(ns ^:figwheel-always kmc-booking.components
  (:require [sablono.core :as sab]
  			[rum :as rum]
  	))


(defn seat-id [y x] 
	(str y "-" x))

(defn- attr[el n]
	(.getAttribute el n))


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


(rum/defc seat-rows < rum/static [range class]
	(into [:div#rows {:class class}]
		 	[[:div.col_num [:div {:__html "&nbsp;"}]]
		 	(map #(vector :div.row_num [:div %]) range)]))



(rum/defc seat-block < rum/cursored-watch [root rows cols seats]
	(reduce conj root [
		(rum/with-props seat-cols cols :rum/key :top)
		(map (fn [[y x]] 
					(rum/with-props seat y x 
						(rum/cursor seats [(seat-id y x)]) :rum/key [y x])) 
				 (for [y rows x cols] [y x]))
		(rum/with-props seat-cols cols :rum/key :bottom)
		])
)		



(defn seat-fn [y x seats]
	(rum/with-props seat y x 
						seats
						(rum/cursor seats [(seat-id y x)]) 
						:rum/key [y x])
	)


(rum/defc seat-plan < rum/cursored-watch rum/reactive [app-state]
	(let [seats (rum/cursor app-state [:seats])


		  left_side_cols (reverse (range 37 40))
		  side_rows (range 1 12)
		  
		  right_side_cols (reverse (range 1 4))
		]

		[:div.seat-plan 
			[:div#parter 
				(seat-rows (range 1 14) "left")
				(seat-rows (range 1 14) "right")

				(seat-block [:div#left_side_house.side_house] side_rows right_side_cols seats)
				]
			;(str (count @seats))
		]
		)
)

#_(do 
		  right_side_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) right_side_cols))
							
							(map (fn [[y x]] (seat-fn y x (rum/cursor seats [(seat-id y x)])) 
								(for [y side_rows x right_side_cols] [y x])))
							
							(into [:div] (map #(vector :div.col_num [:div %]) right_side_cols))
							)

		  house_rows (range 1 14)

		  left_house_cols (reverse (range 20 37))
		  left_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) left_house_cols)) 
							
							(map (fn [[y x]] (seat-fn y x (rum/cursor seats [(seat-id y x)])) 
								(for [y house_rows x left_house_cols] [y x])))
							
							(into [:div] (map #(vector :div.col_num [:div %]) left_house_cols)) 

					 )

		  right_house_cols (reverse (range 4 20)) 							
	      right_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) right_house_cols)) 
							
							(map (fn [[y x]] (seat-fn y x (rum/cursor seats [(seat-id y x)])) 
								(for [y house_rows x right_house_cols] [y x])))

							(into [:div] (map #(vector :div.col_num [:div %]) right_house_cols)))

	      back_house_rows (range 14 21)

	      beletage_cols (range 1 32)
	      beletage (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) beletage_cols)) 
							
							(map (fn [[y x]] (seat-fn y x (rum/cursor seats [(seat-id y x)])) 
								(for [y back_house_rows x beletage_cols] [y x])))

							(into [:div] (map #(vector :div.col_num [:div %]) beletage_cols)))

	      beletage_last_cols (range 1 26)
	      beletage_last_rows (range 21 22)
	      
	      beletage_last_row  (conj [:div#beletage_last_row]
							
							 	(map (fn [[y x]] (seat-fn y x (rum/cursor seats [(seat-id y x)])) 
										(for [y beletage_last_rows x beletage_last_cols] [y x])))

	      						(into [:div] (map #(vector :div.col_num [:div %]) beletage_last_cols))
	      						)

	)

#_(do
	[:div#parter 
				(into 
					[:div#rows.left]
					 	[[:div.col_num [:div {:__html "&nbsp;"}]]
					 	(map #(vector :div.row_num [:div %]) (range 1 14))])

				(into [:div#rows.right]
					 	[[:div.col_num [:div {:__html "&nbsp;"}]]
					 	(map #(vector :div.row_num [:div %]) (range 1 14))])

				
				[:div#left_side_house.side_house left_side_house]

				[:div#left_house.house   left_house]
				[:div#right_house.house  right_house]
				[:div#right_side_house.side_house right_side_house]

				]

			[:div#back_house 
				(into 

					(into 
						[:div#rows.left]
						 	[[:div.col_num [:div {:__html "&nbsp;"}]]
						 	(map #(vector :div.row_num [:div %]) (range 14 21))
						 	])
					[[:div.col_num [:div {:__html "&nbsp;"}]]
						[:div.row_num [:div "21"]]])

				(into 
					(into 
						[:div#rows.right]
						 	[[:div.col_num [:div {:__html "&nbsp;"}]]
						 	(map #(vector :div.row_num [:div %]) (range 14 21))])
					[[:div.col_num [:div {:__html "&nbsp;"}]]
						[:div.row_num [:div "21"]]])


				[:div#beletage beletage]
				beletage_last_row 
			]
	)

