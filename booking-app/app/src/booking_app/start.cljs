(ns booking-app.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            
            [booking-app.behavior :as behavior]
            [booking-app.rendering :as rendering]
            
            [domina.events :as dom-event]
            [domina :as dom]
            ))




(defn render-booking [[op nodes v]]
  (render/log-fn [op nodes v])
  (.log js/console "render-booking" (pr-str op) (pr-str nodes) (pr-str v))

  
  ; what this should return
)

(defn render-form[show]
  (dom/set-classes! (dom/by-id "pre_book") (if show 
                                              "invisible"
                                              "visible"))
  
  (dom/set-classes! (dom/by-id "booking_form") (if show 
                                              "visible"
                                              "invisible"))
  
  
)


(defn booking-renderer [] ; do we really need this?
  (fn [deltas input-queue]
    (.log js/console "on render: " (pr-str deltas) (pr-str input-queue))
    (doseq [d deltas]
      (.log js/console "delta " (pr-str d))
      ; we don't care about node creation here
      (if (= :value (first d))
        (let [[_ [model] old-state new-state] d
              show-form (get (last new-state) :form-show false)]
          
          (render-form show-form)
          
        )
        
        )
      
      )
        
    ; change init queue
    
    ))




(defn create-app [render-config]
  (let [app (app/build behavior/booking-app)
        render-fn (booking-renderer) ;  render-config ; do not use it yet
        
        ;(push-render/renderer "content" render-config render-booking)
        
        ;; This application does not yet have services, but if it did,
        ;; this would be a good place to create it.
        ;; services-fn (fn [message input-queue] ...)
        app-model (render/consume-app-model app render-fn)]
    ;; If services existed, configure the application to send all
    ;; effects there.
    ;; (app/consume-effect app services-fn)

    ;; Start the application
    (app/begin app)
    
    (let [input-queue (:input app)]
      ;(behavior/bind-seats-form input-queue)
      (behavior/bind-booking-form input-queue))

      {:app app :app-model app-model}))


(defn ^:export main []
  (create-app (rendering/render-config)))
