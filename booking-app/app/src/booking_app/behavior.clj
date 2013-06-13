(ns ^:shared booking-app.behavior
    (:require [clojure.string :as string]
            
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app.render.events :as events]
            
              [domina.events :as dom-event]
              [domina :as dom]))



(defn bind-seats-form [input-queue]
 (events/send-on-click
   (dom/by-class "seat")
   input-queue
   (fn [e]
     (let [seat-el (.-currentTarget (.-evt e))
           x (js/parseInt(dom/attr seat-el "x"))
           y (js/parseInt(dom/attr seat-el "y"))]
       
       [{msg/topic :booking msg/type :seat-selected :value [x y]}]))))


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
      ;(.log js/console (.-currentTarget (.-evt e)))
      [{msg/topic :booking 
        msg/type :book 
        :value {:name (dom/value (dom/by-id "contact_name"))
                :tel (dom/value (dom/by-id "phone"))}}]
      ))
  )

(defn booking-transform-fn [state message]
  (.log js/console "got transform message " 
                  (pr-str state)
                  (pr-str message))
  (let [t (msg/type message)]
    (cond 
      (= msg/init t) (do
                       (.log js/console "init msg") 
                       (:value message)
                       )
      (= :show-form t ) (do 
                          
                          (.log js/console "add msg") 
                          (merge state {:form-show (:value message)}))
      
      (= :seat-selected t) (let [coord (:value message)]
                             (js/alert (pr-str coord))
                             (update-in state [:selected] conj coord)     
                             )
      :else (do
              (.log js/console "else") 
              state
              ))))


; Initial state of the application model. It's always a single tree.
(def ^:private initial-app-model
  [{:form-show false
    :selected []
    :booked []}])


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

(def booking-app
  {:transform {:booking
               {:init initial-app-model 
                :fn booking-transform-fn}
               }
   ;:emit {:emit
   ;       {:fn booking-emit
   ;        :input #{[:in]}}
   ;       }
  })


;; While creating new behavior, write tests to confirm that it is
;; correct. For examples of various kinds of tests, see
;; test/booking_app/test/behavior.clj.

(defn set-value-transform [old-value message]
  (:value message))

(def example-app
  {;; There are currently 2 versions (formats) for dataflow
   ;; descritpion: the original version (version 1) and the current
   ;; version (version 2). If the version is not specified, the
   ;; descritpion will be assumed to be version 1 and an attempt
   ;; will be made to convert it to version 2.
   :version 2
   :transform [[:set-value [:greeting] set-value-transform]]})


;; Once this behavior works, run the Data UI and record
;; rendering data which can be used while working on a custom
;; renderer. Rendering involves making a template:
;;
;; app/templates/booking-app.html
;;
;; slicing the template into pieces you can use:
;;
;; app/src/booking_app/html_templates.cljs
;;
;; and then writing the rendering code:
;;
;; app/src/booking_app/rendering.cljs


(comment
  
  ;; The examples below show the signature of each type of function
  ;; that is used to build a behavior dataflow.
  
  ;; transform
  
  (defn example-transform [old-state message]
    ;; returns new state
    )

  ;; derive
  
  (defn example-derive [old-state inputs]
    ;; returns new state
    )

  ;; emit
  
  (defn example-emit [inputs]
    ;; returns rendering deltas
    )
    
  ;; effect
  
  (defn example-effect [inputs]
    ;; returns a vector of messages which effect the outside world
    )
  
  ;; continue
  
  (defn example-continue [iniputs]
    ;; returns a vector of messages which will be processed as part of
    ;; the same dataflow transaction
    )
  
  ;; dataflow description reference
  
  {:transform [[:op [:path] example-transform]]
   :derive    #{[#{[:in]} [:path] example-derive]}
   :effect    #{[#{[:in]} example-effect]}
   :continue  #{[#{[:in]} example-continue]}
   :emit      [[#{[:in]} example-emit]]}
  
  )
