(ns ^:shared booking-app.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app.messages :as msg]
              
              [clojure.set :as set]))


; Initial state of the application model. It's always a single tree.
(def ^:private initial-app-model
  {:form-show false
   :selected #{}
   :pending #{}
   :booked #{}})


(defn booking-transform-fn [state message]
  (.log js/console "TRANSFORM " (pr-str message))
  
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
                      :else (update-in state [:selected] disj [x y])))
      
      (= :refresh-seats t) (let [{pending :pending
                                  booked :booked} (:value message)
                                 selected (get state :selected #{})]
                             
                             ;(if (seq (set/difference selected (set/union pending booked)))
                               ;(js/alert "Місце вибране Вами хтось замовив паралельно")
                             ;  )

                              (merge state
                                    {:pending (set/union pending (get state :pending #{})) 
                                     :booked (set/union booked (get state :booked #{}))
                                     :selected (set/difference selected (set/union pending booked))
                                     }))
      
      (= :failed t) (do 
                      ;(js/alert "Помилка при запитові!")
                      (assoc state :form-show true))
      
      (= :success t) (do 
        ;(js/alert (str "Заброньовано! Код бронювання: " (:booking-id (:value message))))
        ;(.log js/console "success" (pr-str message)) 
        
        ; push update-message
        (assoc state :selected #{}))
      
      :else state)))


(defn booking-effect-fn [message old-model new-model]  
  (.log js/console "EFFECT " (pr-str message))

  (let [t (msg/type message)]
    (cond 
      ; booking
      (= :book (msg/type message)) [{msg/topic :booking msg/type :book :value (:value message)}
                                    {msg/topic :booking msg/type :refresh :value (:value message)}]
    
      ; initialization
      (= msg/init (msg/type message)) [{msg/topic :booking msg/type :refresh :value (:value message)}]
    
      ; refresh 
      (= :refresh (msg/type message)) [{msg/topic :booking msg/type :refresh :value (:value message)}]

      :else [])))
   
      

(def booking-app
  {:transform {:booking
               {:init initial-app-model 
                :fn booking-transform-fn}}
    :effect {:booking booking-effect-fn}
  })

;;;;;;;;;;;;;;;;;;;;
;
; admin
;


(def ^:private initial-admin-model
  {})


(defn admin-transform-fn [state message]
  (let [t (msg/type message)]
    (cond 
      (= msg/init t) (:value message)
      :else state)))


(defn admin-effect-fn [message old-model new-model]  
  [])


(def admin-app
  {:transform {:admin
               {:init initial-admin-model 
                :fn admin-transform-fn}}
    :effect {:admin admin-effect-fn}})

(comment
  ;; dataflow description reference

  {:transform [[:op [:path] example-transform]]
   :derive    #{[#{[:in]} [:path] example-derive]}
   :effect    #{[#{[:in]} example-effect]}
   :continue  #{[#{[:in]} example-continue]}
   :emit      [[#{[:in]} example-emit]]}
  )
