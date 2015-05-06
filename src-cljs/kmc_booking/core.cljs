(ns ^:figwheel-always kmc-booking.core 
  (:require [kmc-booking.components :refer [like-seymore]]
  			[rum :as rum]))


(defn el [id] (js/document.getElementById id))

;; (rum/defc name doc-string? [< mixins+]? [params*] render-body+)


;;;; state

(defonce app-state (atom { 
	:likes 0 

	:text "Hello world!"

	:first-input "123" :second-input "456"
}))

;;; rum 


(rum/defc item [text]
	[:li.task text])
	 
(rum/defc item-list [items]
	  [:div.foo 
	  		[:button {
	  				:onClick #(do
	  					(println "TESTOTESTOTESTO")


						(println @app-state)

	  				)}
	  			 "test"]
	  		(conj [:ul.tasks] (map item items))]
	  )
	 

(defn handle-number-input [event owner]
  (let [text (.. event -target -value)
        num-pattern #"-?\d*"]
    (if (re-matches num-pattern text)
      (reset! owner text)
      (reset! owner @owner))))
 
(rum/defc input < rum/reactive [ref]
  [:input {:type "text"
           :value (rum/react ref)
           :style {:width 100}
           :on-change #(handle-number-input % ref)}])


(rum/defc big-component < rum/cursored-watch [app-state]
  [:div
   (input (rum/cursor app-state [:first-input]))
   (input (rum/cursor app-state [:second-input]))])
 




;;;;;;;;;;;;;;;;;;;;
;; pure React


(defn render! []
  (.render js/React
           (like-seymore app-state)
           (.getElementById js/document "app"))

  )


;;;

(defn ^:export start[]
	(enable-console-print!)
	(println "Initial state")
	(.log js/console (pr-str @app-state))
	
	;; mount rum

	(rum/mount (item-list [1 2 3]) 
		(el "rum-app"))

	(rum/mount (big-component app-state) (el "rum-app-1"))


	(add-watch app-state :on-change 
		(fn [_ _ _ _] (render!)))
	(render!)
	
	)

(set! (.-onload js/window) start)


