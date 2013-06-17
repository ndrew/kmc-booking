(ns booking-app.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push-render]
            [booking-app.behavior :as behavior]
            [booking-app.rendering :as rendering]
            [booking-app.services :as services]))

;; In this namespace, the application is built and started.

(defn create-app [app renderer]
  (let [app-model (render/consume-app-model app renderer)]
    (app/consume-output app services/services-fn)
    ;; Start the application
    (app/begin app)
    {:app app :app-model app-model}))


(defn ^:export main []
  (let [booking-app (create-app 
                      (app/build behavior/booking-app)
                      (push-render/renderer "content" (rendering/render-config) render/log-fn))]))
