(ns booking-app.rendering
  (:require [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.render.events :as events]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log]
            [domina.events :as dom-event]
            [domina :as dom]
            
            [clojure.set :as set]  
            [clojure.string :as string])
  (:require-macros [booking-app.html-templates :as html-templates]))

;; Load templates.

(def templates (html-templates/booking-app-templates))



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
           status (get-seat-status (dom/classes seat-el))]             
       [{msg/topic :booking msg/type :seat-selected :value [status [x y]]}]))))


(defn bind-booking-form [input-queue]
  ; pre-book
  (events/send-on-click
    (dom/by-id "pre_book")
    input-queue
    (fn [e]
      ;(.log js/console (.-currentTarget (.-evt e)))
      [{msg/topic :booking msg/type :show-form :value true}])))



(defn render-booking-page [renderer [_ path] transmitter]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:booking-app-page templates))]
    
    (dom/append! (dom/by-id parent) (html))
    
    (bind-seats-form transmitter)
    (bind-booking-form transmitter)
    
    ))


(defn render-admin-page [renderer [_ path] transmitter]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:admin-app-page templates))
        ]
    
    (dom/append! (dom/by-id parent) (html))))


  
(defn render-message [renderer [_ path _ new-value] transmitter]
  ;; This function responds to a :value event. It uses the
  ;; `update-t` function to update the template at `path` with the new
  ;; values in the passed map.
  (templates/update-t renderer path {:message new-value}))


(defn render-config []
  
  [[:node-create  [:booking] render-booking-page]
   [:node-destroy   [:booking] d/default-exit]
   
   [:node-create  [:admin] render-admin-page]
   [:node-destroy   [:admin] d/default-exit]
   
;[:value [:booking] render-message]
   ])

;; In render-config, paths can use wildcard keywords :* and :**. :*
;; means exactly one segment with any value. :** means 0 or more
;; elements.


