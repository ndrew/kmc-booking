(ns ^:figwheel-always kmc-booking.core 
  (:require [kmc-booking.components :as c]
  			[rum :as rum]
  			[kmc-booking.util :refer [el ajax]]
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
	:success false
	:error nil
}))



;;;;;
;
; api

(defn load-data[]
	(ajax "/api/seats" (fn [seats]
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

		(swap! app-state assoc :reload-fn load-data)

		(add-watch app-state :rendering 
			(fn [_ _ _ _] 
				;(rum/request-render nfo-comp)
				(rum/request-render seat-comp)
				(rum/request-render header-comp)
				(rum/request-render form-comp))))

	(when-not (seq (@app-state :seats))
		(load-data))
	)

;;;;;;;
;
; entry point for admin
;

(defn admin-app[]
	(ajax "/api/bookings" 
		(fn [data]
				(if (and (map? data) (:error data))
					(swap! app-state assoc :error (:error data))
					(swap! app-state assoc :bookings data))) "GET")

	(let [admin-comp    (rum/mount (c/admin-panel app-state) (el "admin"))]
		(add-watch app-state :rendering 
				(fn [_ _ _ _] 
					(rum/request-render admin-comp)))))

(defn ^:export start[]
	(enable-console-print!)

	(let [u (.. js/window -location -pathname)]
		(if (= "/admin" u)
			(admin-app)
			(booking-app) ;(admin-app)
		)))

(set! (.-onload js/window) start)


