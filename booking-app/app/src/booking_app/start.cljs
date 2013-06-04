(ns booking-app.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [booking-app.behavior :as behavior]
            [booking-app.rendering :as rendering]
            [booking-app.services :as services]))

(defn create-app [render-config]
  (let [app (app/build behavior/booking-app)
        render-fn (push-render/renderer "content" render-config)
        app-model (render/consume-app-model app render-fn)
        ;services-fn (services/booking-services-fn)
        ]
    ;(app/consume-effect app services-fn)

    (app/begin app)
    (.log js/console (pr-str {:app app 
                              :app-model app-model}))
    
    
    {:app app 
     :app-model app-model}))

(defn ^:export main []
  (.log js/console "booking-app.simulated.start::main")

  ;(create-app (rendering/render-config))
  
  )
