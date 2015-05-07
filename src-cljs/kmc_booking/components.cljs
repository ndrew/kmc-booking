(ns ^:figwheel-always kmc-booking.components
  (:require [sablono.core :as sab]
  			[rum :as rum]))

;;;;;
;
; helpers

(defn seat-id [y x] 
	(str y "-" x))

(defn- attr[el n]
	(.getAttribute el n))

(defn- range-key [r] 
    [(first r) (last r)])


(defn seats-by-status [seats status ]
	(filter 
   		(fn [[k {s :status}]]
     				(= status s)) @seats))

(defn new-status [seats id]
	(let [status (get-in @seats [id :status])]
		(condp = status
			"free" "pending"
			"pending" "free")))

;;;;;
;
; debug
;


(rum/defc booking-info [app-state] ;;  < rum/cursored
	(let [seats (rum/cursor app-state [:seats])
		  booked (seats-by-status seats "pending")]
		[:div 
			[:pre (pr-str booked)]
		[:hr]
		[:button 
			{:onClick (fn[e] 
				(swap! app-state assoc-in [:seats (.prompt js/window "use row-seat format like '1-39'" "1-39") :status] 
					"pending")
				)} "piy!"]
		]
	)
)

;;;
;
; seating scheme components
;

(rum/defc seat < rum/cursored [y x current] 
	[:div {:x x :y y 
		 	:class (str "seat " (get @current :status))
			} ""])


(rum/defc seat-col < rum/static [col]
	[:div.col_num 
		[:div {:key col} col]])


(rum/defc seat-cols < rum/static [range]
	(into [:div] 
		(map #(rum/with-props seat-col % :rum/key %)
			range)))


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



(rum/defc seat-block < rum/cursored [root rows cols seats] ; < rum/cursored 
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


(defonce parter-rows (range 1 14))
(defonce side_rows (range 1 12))
(defonce back_house_rows (range 14 21))
(defonce left_side_cols (reverse (range 37 40)))
(defonce right_side_cols (reverse (range 1 4)))
(defonce left_house_cols (reverse (range 20 37)))
(defonce right_house_cols (reverse (range 4 20))) 		
(defonce beletage_cols (range 1 32))
(defonce beletage_last_cols (range 1 26))
(defonce beletage_last_rows (range 21 22))


(rum/defc seat-plan < rum/cursored rum/cursored-watch [app-state] 
	(let [seats (rum/cursor app-state [:seats])]

		[:div.seat-plan {:key "root"
						 :onClick (fn[e]
						 	(let [el (.-target e)
								  id (seat-id (attr el "y") (attr el "x"))]
						 		(when-not (= "-" id)
									(swap! seats assoc-in [id :status] (new-status seats id)))))
							}

			[:div#parter {:key "parter"} 
				(rum/with-props seat-rows parter-rows "left" :rum/key (str "left_" (range-key parter-rows)))
				(rum/with-props seat-rows parter-rows "right" :rum/key (str "right_" (range-key parter-rows)))

				(rum/with-props seat-block [:div#left_side_house.side_house] side_rows left_side_cols seats "left" :rum/key "left_side_house.side_house")
				(rum/with-props seat-block [:div#left_house.house] parter-rows left_house_cols seats :rum/key "left_house.house")
				(rum/with-props seat-block [:div#right_house.house] parter-rows right_house_cols seats :rum/key "right_house.house")
				(rum/with-props seat-block [:div#right_side_house.side_house] side_rows right_side_cols seats :rum/key "right_side_house.side_house")
				]
			[:div#back_house {:key "back_house"}
				(rum/with-props seat-rows back_house_rows "left" :rum/key (str "left_" (range-key back_house_rows)))
				(rum/with-props seat-rows back_house_rows "right" :rum/key (str "right_" (range-key back_house_rows)))

				(rum/with-props seat-block [:div#beletage] back_house_rows beletage_cols seats :rum/key "beletage")
			]

			[:div#last_row {:key "last_row"}
				(rum/with-props seat-rows beletage_last_rows "left" :rum/key (str "left_" (range-key beletage_last_rows)))
				(rum/with-props seat-rows beletage_last_rows "right" :rum/key (str "right_" (range-key beletage_last_rows)))

				(rum/with-props seat-block [:div#beletage_last_row] beletage_last_rows beletage_last_cols seats :rum/key "beletage_last_row")
				]


			]
		)


)





