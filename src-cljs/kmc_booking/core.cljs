(ns ^:figwheel-always kmc-booking.core 
  (:require [kmc-booking.components :refer [like-seymore]]
  			[rum :as rum]

  			[cognitect.transit :as transit]

	)
    (:import
    	goog.net.XhrIo)
)


;;; utils

(defn el [id] (js/document.getElementById id))

(defn read-transit [s]
  (transit/read (transit/reader :json {:handlers {"datascript/Datom" (fn[a] 
  	(println "read transit")
  	)}}) s))

(defn- ajax [url callback & [method]]
  (.send goog.net.XhrIo url
    (fn [reply]
      (let [res (.getResponseText (.-target reply))
            res (read-transit res);(profile (str "read-transit " url " (" (count res) " bytes)") (read-transit res))
            ]
        (when callback
          (js/setTimeout #(callback res) 0))))
    (or method "GET")))



;;;; state

(defonce app-state (atom { 
	:likes 0 

	:text "Hello world!"

	:first-input "123" :second-input "456"
	:seats {}
	;:seats-plan [[]]
}))

;;; rum 

;; (rum/defc name doc-string? [< mixins+]? [params*] render-body+)

(rum/defc seat-plan < rum/cursored-watch [app-state]
	(let [seats (rum/cursor app-state [:seats])

		  left_side_cols (reverse (range 37 40))
		  side_rows (range 1 12)
		  
		  left_side_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) left_side_cols))
							(map (fn[[y x]] (vector :div.seat {:x x :y y} "")) 
								(for [y side_rows x left_side_cols] [y x]))
							(into [:div] (map #(vector :div.col_num [:div %]) left_side_cols))
							)

		  right_side_cols (reverse (range 1 4))
		  right_side_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) right_side_cols))
							(map (fn[[y x]] (vector :div.seat {:x x :y y} "")) 
								(for [y side_rows x right_side_cols] [y x]))
							(into [:div] (map #(vector :div.col_num [:div %]) right_side_cols))
							)

		  house_rows (range 1 14)

		  left_house_cols (reverse (range 20 37))
		  left_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) left_house_cols)) 
							(map (fn[[y x]] (vector :div.seat {:x x :y y} "")) 
								(for [y house_rows x left_house_cols] [y x]))
							(into [:div] (map #(vector :div.col_num [:div %]) left_house_cols)) 

					 )

		  right_house_cols (reverse (range 4 20)) 							
	      right_house (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) right_house_cols)) 
							(map (fn[[y x]] (vector :div.seat {:x x :y y} "")) 
								(for [y house_rows x right_house_cols] [y x]))
							(into [:div] (map #(vector :div.col_num [:div %]) right_house_cols)))

	      back_house_rows (range 14 21)

	      beletage_cols (range 1 32)
	      beletage (conj 
							(into [:div] (map #(vector :div.col_num [:div %]) beletage_cols)) 
							(map (fn[[y x]] (vector :div.seat {:x x :y y} "")) 
								(for [y back_house_rows x beletage_cols] [y x]))
							(into [:div] (map #(vector :div.col_num [:div %]) beletage_cols)))

	      beletage_last_cols (range 1 26)
	      beletage_last_rows (range 21 22)
	      
	      beletage_last_row  (conj [:div#beletage_last_row]
									(map (fn[[y x]] (vector :div.seat {:x x :y y} "")) 
										(for [y beletage_last_rows x beletage_last_cols] [y x]))
	      						(into [:div] (map #(vector :div.col_num [:div %]) beletage_last_cols))
	      						)
	      						 
									
									

		]



		[:div.seat-plan 
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
			;(str (count @seats))
			]
		)
)


(rum/defc item [text]
	[:li.task text])
	 
(rum/defc item-list [items]
	  [:div.foo 
	  		[:button {
	  				:onClick #(do
	  					(println "TESTOTESTOTESTO")


						(println @app-state)

	  				)}
	  			 "test"]
	  		(conj [:ul.tasks] (map item items))]
	  )
	 

(defn handle-number-input [event owner]
  (let [text (.. event -target -value)
        num-pattern #"-?\d*"]
    (if (re-matches num-pattern text)
      (reset! owner text)
      (reset! owner @owner))))
 
(rum/defc input < rum/reactive [ref]
  [:input {:type "text"
           :value (rum/react ref)
           :style {:width 100}
           :on-change #(handle-number-input % ref)}])


(rum/defc big-component < rum/cursored-watch [app-state]
  [:div
   (input (rum/cursor app-state [:first-input]))
   (input (rum/cursor app-state [:second-input]))])
 




;;;;;;;;;;;;;;;;;;;;
;; pure React


(defn render! []
  (.render js/React
           (like-seymore app-state)
           (.getElementById js/document "app"))

  )


;;;

(defn ^:export start[]
	(enable-console-print!)
	(println "Initial state")
	(.log js/console (pr-str @app-state))
	
	;; mount rum
	
	(rum/mount (item-list [1 2 3]) 
		(el "rum-app"))

	(rum/mount (big-component app-state) (el "rum-app-1"))

	(rum/mount (seat-plan app-state) (el "scheme"))

	(add-watch app-state :on-change 
		(fn [_ _ _ _] (render!)))
	(render!)
	

	(ajax "/api/seats" (fn [seats]
		(swap! app-state assoc :seats seats)
		) "GET")
)

(set! (.-onload js/window) start)


