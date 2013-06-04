(ns booking-app.simulated.start
  (:require [io.pedestal.app.render.push.handlers.automatic :as auto]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push-render]

            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]
            
            [booking-app.start :as start]
            [booking-app.behavior :as behavior]
            [booking-app.rendering :as rendering]
            [booking-app.simulated.services :as services]
            
            [io.pedestal.app.util.console-log :as logger]
            ))

(defn ^:export main []
  ;; Create an application which uses the data renderer. The :data-ui
  ;; aspect is configured to run this main function. See
  ;;
  ;; config/config.clj
  ;;
  
  (logger/log "LOGGER TEST!")
  
  (.log js/console "booking-app.simulated.start::main")
  (let [render-cfg (rendering/render-config)]    

    
    (.log js/console (pr-str render-cfg))

    
    (start/create-app render-cfg)
  ))
  
