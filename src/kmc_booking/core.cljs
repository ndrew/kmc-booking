(ns ^:figwheel-always kmc-booking.core
	(:require
	   [figwheel.client :as fw]
	   [rum :include-macros true]
	   [datascript :as d]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defn el [id] (js/document.getElementById id))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(rum/defc item [text]
  [:li.task text])
 
(rum/defc item-list [items]
  (conj [:ul.tasks] (map item items)))
 
(rum/mount (item-list [1 2 3]) 
	(.-body js/document)
	;(el "main-area")
	)
 
(fw/start)
