(ns ^:figwheel-always kmc-booking.components
  (:require [sablono.core :as sab]
  			[rum :as rum]
  			[clojure.string :as string]
  			[kmc-booking.util :refer [ajax]]

  			[goog.i18n.DateTimeFormat :as dtf]
  			))

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

(def MAX_SEATS 10)

(defn- can-select-more? [seats]
	(> MAX_SEATS (count seats)))

(defn new-status [seats id]
	(let [status (get-in @seats [id :status])]
		(condp = status
			"free" (if (can-select-more? (seats-by-status seats "your")) "your" "free")
			"your" "free"
  			"paid" "paid"
  			"pending" "pending"
			)))

(def format-map
  (let [f goog.i18n.DateTimeFormat.Format]
{:FULL_DATE (.-FULL_DATE f)
:FULL_DATETIME (.-FULL_DATETIME f)
:FULL_TIME (.-FULL_TIME f)
:LONG_DATE (.-LONG_DATE f)
:LONG_DATETIME (.-LONG_DATETIME f)
:LONG_TIME (.-LONG_TIME f)
:MEDIUM_DATE (.-MEDIUM_DATE f)
:MEDIUM_DATETIME (.-MEDIUM_DATETIME f)
:MEDIUM_TIME (.-MEDIUM_TIME f)
:SHORT_DATE (.-SHORT_DATE f)
:SHORT_DATETIME (.-SHORT_DATETIME f)
 :SHORT_TIME (.-SHORT_TIME f)}))


(defn format-date-generic
  "Format a date using either the built-in goog.i18n.DateTimeFormat.Format enum
or a formatting string like \"dd MMMM yyyy\""
  [date-format date]
  (.format (goog.i18n.DateTimeFormat.
            (or (date-format format-map) date-format))
           (js/Date. date)))

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
									(let [st (new-status seats id)]
										(swap! seats assoc-in [id :status] st)
										(when (= st "pending")
											(swap! app-state assoc :success false)
											(swap! app-state assoc :error nil)
											
											)

										)

									)))}

			[:div#parter {:key "parter"} 
				(rum/with-props seat-rows parter-rows "left" :rum/key (str "left_" (range-key parter-rows)))
				(rum/with-props seat-rows parter-rows "right" :rum/key (str "right_" (range-key parter-rows)))

				(rum/with-props seat-block [:div#left_side_house.side_house] side_rows left_side_cols seats "left" :rum/key "left_side_house.side_house")
				(rum/with-props seat-block [:div#left_house.house] parter-rows left_house_cols seats :rum/key "left_house.house")
				(rum/with-props seat-block [:div#right_house.house] parter-rows right_house_cols seats :rum/key "right_house.house")
				(rum/with-props seat-block [:div#right_side_house.side_house] side_rows right_side_cols seats :rum/key "right_side_house.side_house")]

			[:div#back_house {:key "back_house"}
				(rum/with-props seat-rows back_house_rows "left" :rum/key (str "left_" (range-key back_house_rows)))
				(rum/with-props seat-rows back_house_rows "right" :rum/key (str "right_" (range-key back_house_rows)))

				(rum/with-props seat-block [:div#beletage] back_house_rows beletage_cols seats :rum/key "beletage")]

			[:div#last_row {:key "last_row"}
				(rum/with-props seat-rows beletage_last_rows "left" :rum/key (str "left_" (range-key beletage_last_rows)))
				(rum/with-props seat-rows beletage_last_rows "right" :rum/key (str "right_" (range-key beletage_last_rows)))

				(rum/with-props seat-block [:div#beletage_last_row] beletage_last_rows beletage_last_cols seats :rum/key "beletage_last_row")]]))


;;;;;;
;
; header 

(rum/defc header < rum/static []
	[:div 
		[:div#kmc-logo "КМЦ"]
		[:div#legend
			[:div [:div.seat.free] "наявні" ]
			[:div [:div.seat.paid] "викуплені"]
			[:div [:div.seat.pending] "заброньовані" ]
			[:div [:div.seat.your] "вибрані" ]]

		[:div#legend-prices "Ціна — 80 грн, за бокові місця — 70 грн."]
	])

;;;;;;
; 
; form

(rum/defc input < rum/reactive [label ref fn]
  [:div.input 
	  [:label label]
	  [:input {:type "text"
           :value (rum/react ref)
			:class (when-not (fn (rum/react ref))
                                 	"error")

           :on-change #(reset! ref (.. % -target -value))}]

         ])

(defn ticket-n[n]
	(condp = n
		1 " квиток"
		2 " квитки"
		3 " квитки"
		4 " квитки"
		" квитків"
		)
	)

(defn price-by-id [id]
	(let [[r c] (string/split id #"-")
			row (js/parseInt r)
			col (js/parseInt c)
		]
			(if (or (< col 4)
					(> col 36))
				70
				80
			)
		)
	)

(defn calc-price [booked]
	(reduce (fn [p [id _]]
		(+ (price-by-id id) p)
			) 0 booked))

(rum/defc form < rum/cursored rum/cursored-watch [app-state] 
	(let [seats (rum/cursor app-state [:seats])
		  booked (seats-by-status seats "your")
		  name (rum/cursor app-state [:name])
		  phone (rum/cursor app-state [:phone])
		  
		  error (rum/cursor app-state [:error])
		  success (rum/cursor app-state [:success])
		  ]
		(if (seq booked)
			(let [validate-name #(not (string/blank? %))
				  validate-phone #(not (string/blank? %))

				  valid? (and (validate-name @name)
							 (validate-phone @phone))
				  n (count booked)
				  ids-to-book (reduce (fn [s [id _]]
          							(str s (if (seq s) ";") id )	
          						) "" booked)
				  url (str "/api/book" 
									"?name=" (js/encodeURIComponent @name)
									"&phone=" (js/encodeURIComponent @phone)
									"&seats=" (js/encodeURIComponent ids-to-book))
				  ]
					[:div 
						[:span.price (str n (ticket-n n) ", " (calc-price booked) " грн ")]

						(input "Прізвище й ім'я" name validate-name)
						(input "Телефон" phone validate-phone)

					[:button#book 
						{:class (if-not valid? 
									"disabled"
									"")
						 :onClick (fn[e]
						 	(when valid? 
						 		(ajax url 
						 			(fn[res]

						 				(if (map? res)
						 					(do
						 						(reset! error (get res :error "Сталась помилка!")))
						 					(do
						 						(reset! success true)
						 						
						 						)
						 					)
						 				((get @app-state :reload-fn)) 
									) "POST")
						 		)
						 	)
						}
						"Придбати"]

					(if-not (can-select-more? booked) 
						[:div.info-message (str "Ми продаємо не більше " MAX_SEATS " квитків в одні руки! Звиняйте")])]

				
			)
			; else
			(if @success  [:div.info-message "Квиточки заброньовані, з вами зв'яжуться"]
				(if @error [:div.error-message @error])

				)
		)
))


(rum/defc admin-booking [booking [actual-seats history-seats]]
  (let [{id :id
  		 name :name
  		 phone :phone
  		 date :date
  		 } booking
  		 pending? (partial every? 
  		 	 (fn [a]
  		 	 	(= "pending" (:status a))))
		 paid? (partial every? #(= "paid" (:status %)))

		 is-pending? (pending? actual-seats)
		 is-paid? (paid? actual-seats)
  		 ]

  [:div.booking-line
  	[:pre
  		(str 
  			"actual:  " "pending=" is-pending? "; paid=" is-paid?  "\n"
  			(pr-str actual-seats) "\n"

  			"history: " "pending=" (pending? history-seats) "; paid=" (paid? history-seats) "\n"
  			(pr-str history-seats)

  			) 
  		;(pr-str seats)

  		]
  	[:span
  		[:span.date (format-date-generic :SHORT_DATETIME date)]
  		[:span.name name]
  		[:span.phone phone] 
  	] 
	  	
	(if is-pending?
		[:span "очікують підтвердження"]
		)

  	#_(if is-pending?
  		[:button {:onClick (fn[e] 
  								(.log js/console "Foo"))}
  		 "викуплено!"])

  	#_(if is-pending? 
  		[:button {:onClick (fn[e] 
  								(.log js/console "Bar")
  		)} "скасувати"])

  	(if is-paid? 
  		[:span "ВИКУПЛЕНО"]
  		)

  	]
 ))


(rum/defc admin-panel < rum/cursored rum/cursored-watch [app-state] 
	(let [error (rum/cursor app-state [:error])
		  bookings (rum/cursor app-state [:bookings])] 

		[:div
			(if @error [:div.error-message @error])

			(if @bookings 
				(into [:div] 
					(map (fn[[booking seats]]
							(rum/with-props admin-booking booking seats :rum/key (:id booking)))
					@bookings))

				)
			]
		)
	)




