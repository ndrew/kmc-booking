(ns ^:figwheel-always kmc-booking.core 
  (:require [kmc-booking.components :as c]
  			[rum :as rum]
  			[cognitect.transit :as transit])
    (:import
    	goog.net.XhrIo))

;;;;;
;
; utils

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


;;;;;;
;
; state

(defonce app-state (atom { 
	:seats {}
	:selected #{}
	;:seats-plan [[]]
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

(defn ^:export start[]
	(enable-console-print!)
	;(println "Initial state")
	;(.log js/console (pr-str @app-state))

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

(set! (.-onload js/window) start)


