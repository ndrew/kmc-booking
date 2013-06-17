(ns booking-app.admin.start
  (:require [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            
            [booking-app.start :as start]
            [booking-app.behavior :as behavior]
            [booking-app.rendering :as rendering]
            
            
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]))



(defn ^:export main []
  (start/create-app 
    (app/build behavior/admin-app)
    (push-render/renderer "content" (rendering/render-config) render/log-fn))
  
  )
