(ns ^:figwheel-always kmc-booking.core 
  (:require [kmc-booking.components :refer [seat-plan booking-info]]
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
	:seats {}
	:selected #{}
	;:seats-plan [[]]
}))



;;;

(defn load-data[]
	(ajax "/api/seats" (fn [seats]
		(swap! app-state assoc :seats seats)) "GET"))


(defn ^:export start[]
	(enable-console-print!)
	;(println "Initial state")
	;(.log js/console (pr-str @app-state))

	(let [seat-comp (rum/mount (seat-plan app-state) (el "scheme"))
		  nfo-comp  (rum/mount (booking-info app-state) (el "nfo"))]
		;; mount rum
		;; (rum/mount (item-list [1 2 3])  (el "rum-app"))
		;; (rum/mount (big-component app-state) (el "rum-app-1"))

		(add-watch app-state :rendering 
			(fn [_ _ _ _] 
				(rum/request-render nfo-comp)
				(rum/request-render seat-comp))))

	(when-not (seq (@app-state :seats))
		(load-data)
		)
)

(set! (.-onload js/window) start)


