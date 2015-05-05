(ns kmc-booking.core
  (:require [sablono.core :as sab]))

(defonce app-state (atom { :likes 0 }))

(defn like-seymore [data]
  (sab/html [:div
             [:h1 "Pesto: " (:likes @data)]
             [:div [:a {:href "#"
                        :onClick #(swap! data update-in [:likes] inc)}
                    "Thumbs up"]]]))

(defn render! []
  (.render js/React
           (like-seymore app-state)
           (.getElementById js/document "app")))

(add-watch app-state :on-change (fn [_ _ _ _] (render!)))

(render!)

;;;;;;;;;;;;;;;;;;;;


(defn ^:export start[]
	(.log js/console "Hello"))

(.log js/console (pr-str @app-state))




