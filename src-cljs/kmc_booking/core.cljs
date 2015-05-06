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
	(let [seats (rum/cursor app-state [:seats])]
		[:div.seat-plan 
			[:div#parter 
				
				(into 
					[:div#rows.left]
					 	[[:div.col_num [:div {:__html "&nbsp;"}]]
					 	[:div.col_num [:div {:__html "&nbsp;"}]]
						(map #(vector :div.row_num %) (range 1 14))])

				(into [:div#rows.right]
					 	[[:div.col_num [:div {:__html "&nbsp;"}]]
					 	[:div.col_num [:div {:__html "&nbsp;"}]]
						(map #(vector :div.row_num %) (range 1 14))])

				[:div#left_side_house.side_house 
					(let [xs (reverse (range 37 40))
					      ys (range 1 14)]
						(concat 
							(into [:div] (map #(vector :div.col_num %) xs))
							(map (fn[[y x]] (vector :div.seat {:x x :y y} (pr-str [x y]))) 
								(for [y ys x xs] [y x])
								)
							)
						)
				]
				[:div#left_house.house 
					(into [:div] (map #(vector :div.col_num %) (reverse (range 20 37))))
					]
				[:div#right_house.house 
					(into [:div] (map #(vector :div.col_num %) (reverse (range 4 20))))

					]
				[:div#right_side_house.side_house
					(into [:div] (map #(vector :div.col_num %) (reverse (range 1 4))))

					]

				]

			[:div#back_house 
				[:div#rows.left "1 2 3 "]
				[:div#rows.right "1 2 3 "]
				[:div#beletage "!"]
				[:div#beletage_last_row ""]
			]
			(str (count @seats))
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


