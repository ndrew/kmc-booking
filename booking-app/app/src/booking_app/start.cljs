(ns booking-app.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            
            [io.pedestal.app.render.events :as events]
            
            [booking-app.behavior :as behavior]
            [booking-app.rendering :as rendering]
            
            [domina.events :as dom-event]
            [domina :as dom]
            ))



; When the button is clicked, send a message to :todo topic to kick off the
; process.
(defn bind-seats-form [input-queue]
 (events/send-on-click
   (dom/by-class "seat")
   input-queue
   (fn []
      (js/alert "click on the seat")
                      
                      ;(let [text-node (dom/by-id "todo-entry")
                      ;      text (.-value text-node)]
                      ;  (set! (.-value text-node) "")
                      ;  [{msg/topic :todo msg/type :add :value text}])))
                      ))
 
  ;(let [form (dom/by-id "todo-form")
  ;      btn (dom/by-id "todo-add-button")]
  ;  (.focus (dom/by-id "todo-entry"))
    ;)
  )

(defn bind-booking-form [input-queue]
  (events/send-on-click
    (dom/by-id "pre_book") input-queue 
    (fn []
      (js/alert "pre_book")
      
      
      [{msg/topic :booking-app 
        msg/type :set-name :name "Alice" }]
      ; return some message
      )))  
  

(defn create-app [render-config]
  (let [app (app/build behavior/booking-app)
        render-fn (push-render/renderer "content" render-config render/log-fn)
        ;; This application does not yet have services, but if it did,
        ;; this would be a good place to create it.
        ;; services-fn (fn [message input-queue] ...)
        app-model (render/consume-app-model app render-fn)]
    ;; If services existed, configure the application to send all
    ;; effects there.
    ;; (app/consume-effect app services-fn)

    (.log js/console (pr-str (keys app)))
    
    ;; Start the application
    (app/begin app)
    
    (let [input-queue (:input app)]
      (bind-seats-form input-queue)
      (bind-booking-form input-queue)      
      )

    ;; Send a message to the application so that it does something.
    ;(p/put-message (:input app) {msg/type :init-seats 
    ;                             msg/topic [:seats] 
    ;                             :value [:1_1 :1_2]})
    ;(p/put-message (:input app) {msg/type :set-value msg/topic [:greeting] :value "Hello World!"})
    {:app app :app-model app-model}))


(defn ^:export main []
  (create-app (rendering/render-config)))
