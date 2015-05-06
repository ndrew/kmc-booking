(ns kmc-booking.core
  (:require [kmc-booking.components :refer [like-seymore]]
  			[rum :as rum]))


(defn el [id] (js/document.getElementById id))

;; (rum/defc name doc-string? [< mixins+]? [params*] render-body+)


;;;; state

(defonce app-state (atom { 
	:likes 0 
	:text "Hello world!"
}))



;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(rum/defc item [text]
	[:li.task text])
	 
(rum/defc item-list [items]
	  (conj [:ul.tasks] (map item items)))
	 
(rum/mount (item-list [1 2 3]) 
		(el "rum-app"))



;;;;;;;;;;;;;;;;;;;;


(defn render! []
  (.render js/React
           (like-seymore app-state)
           (.getElementById js/document "app"))

  )

(add-watch app-state :on-change 
	(fn [_ _ _ _] (render!)))



(render!)
(enable-console-print!)


;;;

(defn ^:export start[]
	(.log js/console (pr-str @app-state))
	)





