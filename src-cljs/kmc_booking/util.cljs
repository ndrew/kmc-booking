(ns kmc-booking.util 
  (:require [cognitect.transit :as transit])
    (:import
    	goog.net.XhrIo))


(defn el [id] (js/document.getElementById id))


(defn read-transit [s]
  (transit/read (transit/reader :json {:handlers {"datascript/Datom" (fn[a] 
  	;(println "read transit")
  	)}}) s))

(defn ajax-transit [url callback & [method]]
  (.send goog.net.XhrIo url
    (fn [reply]
      (let [res (.getResponseText (.-target reply))
            res (read-transit res);(profile (str "read-transit " url " (" (count res) " bytes)") (read-transit res))
            ]
        (when callback
          (js/setTimeout #(callback res) 0))))
    (or method "GET")))

(defn ajax-post [url callback]
  (.send goog.net.XhrIo url
    (fn [reply]
      (let [res (.getResponseText (.-target reply))
           ; res (read-transit res);(profile (str "read-transit " url " (" (count res) " bytes)") (read-transit res))
            ]

         (.warn js/console res)

        (when callback
          (js/setTimeout #(callback res) 0))))
    "POST")
  )