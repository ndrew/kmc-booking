(ns booking-app.simulated.start
  (:require [io.pedestal.app.render.push.handlers.automatic :as d]
            [booking-app.start :as start]
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]))

(defn ^:export main []  
  (.log js/console "booking-app.simulated.start")
  
  ;; Create an application which uses the data renderer. The :data-ui
  ;; aspect is configured to run this main function. See
  ;;
  ;; config/config.clj
  ;;
  (let [app (start/create-app d/data-renderer-config)]
    
    
    (.log js/console "renderer cfg")
    (.log js/console (pr-str d/data-renderer-config))
    
    (.log js/console "app")
    (.log js/console (pr-str (keys app)))
    
    ))
