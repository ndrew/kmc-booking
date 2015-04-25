(ns kmc-booking.db
     (:require [korma.db :as k]))


(defdb db (k/postgres {
		:db "misskma"
        :user "misskma"
        :password "misskma"
 ;; optional keys
 ;; :host "myhost"
 ;; :port "4567"
 ;; :delimiters ""
		}))


