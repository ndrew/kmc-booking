(ns ^:figwheel-always kmc-booking.core 
  (:require [kmc-booking.components :as c]
  			[rum :as rum]
  			[kmc-booking.util :refer [el ajax-transit]]
  			[cognitect.transit :as transit])
    (:import
    	goog.net.XhrIo))

;;;;;
;
; utils


;;;;;;
;
; state

(defonce app-state (atom { 
	:seats {}
	:name ""
	:phone ""
	;:seats-plan [[]]
	:sucess false
}))



;;;;;
;
; api

(defn load-data[]
	(ajax-transit "/api/seats" (fn [seats]
		(swap! app-state assoc :seats seats)) "GET"))

(defn book[]
	;;
	)

;;;;;;
;
; entry point
;

(defn booking-app[]
	(let [;nfo-comp    (rum/mount (c/booking-info app-state) (el "nfo"))
		  seat-comp   (rum/mount (c/seat-plan app-state)    (el "scheme"))
		  header-comp (rum/mount (c/header)                 (el "header"))
		  form-comp   (rum/mount (c/form app-state) 		(el "form"))]

		(add-watch app-state :rendering 
			(fn [_ _ _ _] 
				;(rum/request-render nfo-comp)
				(rum/request-render seat-comp)
				(rum/request-render header-comp)
				(rum/request-render form-comp))))

	(when-not (seq (@app-state :seats))
		(load-data))
	)

(defn admin-app[]
	(println "Hello!")
	)

(defn ^:export start[]
	(enable-console-print!)

	(let [u (.. js/window -location -pathname)]
		(if (= "/admin" u)
			(admin-app)
			(booking-app)
			)
		)
)

(set! (.-onload js/window) start)


