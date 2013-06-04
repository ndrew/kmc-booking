(ns ^:shared booking-app.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app.messages :as msg]))



(defn booking-transform [transform-state message]
  (condp = (msg/type message)
    msg/init (:value message)
    transform-state))

(def booking-app
  {:transform {:booking {:init "Hello World!" :fn booking-transform}}}
  )

;;;;;;;;;;;;;;
;; samples ↴

;; While creating new behavior, write tests to confirm that it is
;; correct. For examples of various kinds of tests, see
;; test/booking_app/test/behavior.clj.

;; You'll always receive a message with the type msg/init when your
;; app starts up. This message will include a :value key with the
;; value of the :init key from your dataflow.

(defn example-transform [transform-state message]
  (condp = (msg/type message)
    msg/init (:value message)
    transform-state))

(def example-app
  {:transform {:example-transform {:init "Hello World!" :fn example-transform}}})


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
  
  (defn example-transform [transform-state message]
    ;; returns new state
    )
  
  ;; effect
  
  (defn example-effect [message old-transform-state new-transform-state]
    ;; returns vector of messages to be added to input queue for future processing
    )
  
  ;; combine
  
  (defn example-combine-1 [combine-state input-name old-transform-state new-transform-state]
    ;; returns new combine state
    )
  
  (defn example-combine-2 [combine-state inputs]
    ;; inputs are a map of input names to their old and new state
    ;; returns new combine state
    )
  
  ;; continue
  
  (defn example-continue [combine-name old-combine-state new-combine-state]
    ;; returns vector of messages to be processed as part of current data flow execution
    )
  
  ;; emit
  
  (defn example-emit
    ([input]
       ;; input is a map of input names to their old and new state
       ;; called when emit is first displayed - returns rendering data
       )
    ([input changed-input]
       ;; input is a map of input names to their old and new state
       ;; changed-input is a set of the input names which have changed
       ;; called when inputs are updated - returns rendering data
       ))
  
  ;; example dataflow map
  
  {:transform {:example-transform {:init "" :fn example-transform}}
   :effect {:example-transform example-effect}
   :combine {:example-combine {:fn example-combine-1 :input #{:example-transform}}}
   :continue {:example-combine example-continue}
   :emit {:example-emit {:fn example-emit :input #{:example-combine}}}
   :focus {:home [[:a-path]]
                :default :home}}
  
  )
