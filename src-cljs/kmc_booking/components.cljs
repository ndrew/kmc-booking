(ns kmc-booking.components
  (:require [sablono.core :as sab]))

(defn like-seymore [data]
  (sab/html [:div
             [:h1 "Pesto: " (:likes @data)]
             [:div [:a {:href "#"
                        :onClick #(swap! data update-in [:likes] inc)}
                    "Thumbs up"]]
			 [:button {
			          	:onClick #(do 
			             			(.log js/console "Foo")
			             			)
			             	} "Foo"
		]]))


