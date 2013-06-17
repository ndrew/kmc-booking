(ns booking-app.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro booking-app-templates
  []
   {:booking-app-page (dtfn (tnodes "booking-app.html" "booking-screen") #{})
    :admin-app-page   (dtfn (tnodes "admin-app.html" "admin-screen") #{})
    }
   )

;; Note: this file will not be reloaded automatically when it is changed.
