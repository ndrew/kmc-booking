(ns kmc-booking.core
  (:require [kmc-booking.components :refer [like-seymore]]))


;;;;;;;;;;;;;;;;;;;;

(defonce app-state (atom { :likes 0 }))

(defn render! []
  (.render js/React
           (like-seymore app-state)
           (.getElementById js/document "app"))

  )

(add-watch app-state :on-change 
	(fn [_ _ _ _] (render!)))

(render!)

;;;

(defn ^:export start[]
	(.log js/console (pr-str @app-state))
	)





