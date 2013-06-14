(ns ^:shared booking-app.behavior
    (:require [clojure.string :as string]
            
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app.render.events :as events]
            
              [domina.events :as dom-event]
              [domina :as dom]
              
              
              ))




(defn- get-seat-status [seat-classes]
  (let [s (set seat-classes)]
    (cond 
      (= #{"seat"} s) :free
      (= #{"seat" "seat_pending"} s) :pending
      (= #{"seat" "seat_booked"} s) :booked
      :else :pending)))


(defn bind-seats-form [input-queue]
 (events/send-on-click
   (dom/by-class "seat")
   input-queue
   (fn [e]
     (let [seat-el (.-currentTarget (.-evt e))
           x (js/parseInt(dom/attr seat-el "x"))
           y (js/parseInt(dom/attr seat-el "y"))
           status (get-seat-status (dom/classes seat-el))
           ]
              
       [{msg/topic :booking msg/type :seat-selected :value [status [x y]]}]))))


(defn bind-booking-form [input-queue]
  ; pre-book
  (events/send-on-click
    (dom/by-id "pre_book")
    input-queue
    (fn [e]
      ;(.log js/console (.-currentTarget (.-evt e)))
      [{msg/topic :booking 
        msg/type :show-form 
        :value true}]
      ))
  
 ; book 
  (events/send-on-click
    (dom/by-id "book")
    input-queue
    (fn [e]
      (let [seats (reduce #(conj %1 [(js/parseInt (dom/attr %2 "x"))
                               (js/parseInt (dom/attr %2 "y"))]) #{} 
                                                  (goog.dom/getElementsByClass "seat_your"))]
        [{msg/topic :booking 
          msg/type :book 
          :value {:name (dom/value (dom/by-id "contact_name"))
                  :tel (dom/value (dom/by-id "phone"))
                  :selected seats
                  }}]))))


(defn booking-transform-fn [state message]
  ;(.log js/console "got transform message " 
  ;                (pr-str state)
  ;                (pr-str message))
  (let [t (msg/type message)]
    (cond 
      (= msg/init t) (:value message)
      
      (= :show-form t )  
        (merge state
               {:form-show (:value message)})
                                
      (= :book t) (do
                    ;(.log js/console "previous state " (pr-str state))
                    (if-not (:name message)
                        (assoc state :form-show false)
                        state))

      
      (= :seat-selected t) (let [[status [x y]] (:value message)]
                    
                    (cond 
                      (= :free status) (update-in state [:selected] conj [x y])  
                      :else (update-in state [:selected] disj [x y])  
                      
                      )
                    )
      
      (= :failed t) (do 
                      (js/alert "Помилка при запитові!")
                      (assoc state :form-show true)
                      )
      
      (= :success t) (do 
        (js/alert (str "Заброньовано! Код бронювання: " (:booking-id (:value message))))
        ;(.log js/console "success" (pr-str message)) 
        
        state 
      )
      :else (do
              (.log js/console "message unknown") 
              state
              ))))


; Initial state of the application model. It's always a single tree.
(def ^:private initial-app-model
  {:form-show false
    :selected #{}
    :pending #{}
    :booked #{}})


; TODO: not working, using default
(defn booking-emit
  ([inputs] 
   (.log js/console "booking-emit: " (pr-str inputs))
   [[:value [:booking] initial-app-model]])
  
  ([inputs changed-inputs]
    (.log js/console "booking emit[1]: " (pr-str inputs) (pr-str changed-inputs) )
  
    [[:value [:booking] initial-app-model]]
    ;initial-app-model
    )
  
  )


(defn booking-effect [message old-model new-model]
  
  (if (= :book (msg/type message) )
    [{msg/topic :booking 
      msg/type :book
      :value (:value message)}]
    []
    ))


(def booking-app
  {:transform {:booking
               {:init initial-app-model 
                :fn booking-transform-fn}}
   
    :effect {:booking booking-effect}
   ;:emit {:emit
   ;       {:fn booking-emit
   ;        :input #{[:in]}}
   ;       }
  })
