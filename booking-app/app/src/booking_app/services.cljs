(ns booking-app.services
  (:require [cljs.reader :as r]
            [io.pedestal.app.net.xhr :as xhr]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log]))



(defn receive-messages [app]
;  (p/put-message (:input app) {msg/topic :inbound
;                 msg/type :received
;                 :text (str "incoming message " (gensym))
;                 :nickname (str (gensym))
;                 :id (util/random-id)})
;  (.setTimeout js/window (fn [] (receive-messages app)) 10000)
)



(defrecord Services [app]
  p/Activity
  (start [this]
    (let [source (js/EventSource. "/booking")]
      ; todo
      (.addEventListener source
                         "msg"
                         (fn [e]
                           ;(let [data (r/read-string (.-data e))]
                           ;  (.log js/console e)
                           ;  (p/put-message (:input app)
                           ;                 {msg/topic :inbound
                           ;                  msg/type :received
                           ;                  :value 
                            ;                 {}))) ; TODO: track msg id throughout the system
                         false)
      (.addEventListener source
                         "open"
                         (fn [e]
                           (.log js/console e))
                         false)
      (.addEventListener source
                         "error"
                         (fn [e]
                           (.log js/console e))
                         false)
      (.log js/console source)))
  (stop [this])))


(defn services-fn [message i-q]
  (when-let [msg (msg/topic message) ]
    (let [body (pr-str 
                 (:value message)
                 ;{:text (:text msg) :nickname (:nickname msg)}
                 )
          log (fn [args]
                (.log js/console (pr-str args))
                (.log js/console (:xhr args)))
          err-fn (fn [args]
                   (p/put-message i-q 
                                  {msg/topic :booking
                                   msg/type :failed
                                   :value args }
                   
                   ))
          
          success-fn (fn[args] 
                       (log args)
                       (p/put-message i-q 
                                  {msg/topic :booking
                                   msg/type :success
                                   :value args }
                       )
          
          ]
      (xhr/request (gensym)
                   "/booking"
                   :request-method "POST"
                   :headers {"Content-Type" "application/edn"}
                   :body body
                   :on-success success-fn
                   :on-error err-fn))
    (.log js/console (str "Send to Server: " (pr-str message)))))



;; The services namespace responsible for communicating with back-end
;; services. It receives messages from the application's behavior,
;; makes requests to services and sends responses back to the
;; behavior.
;;
;; This namespace will usually contain a function which can be
;; configured to receive effect events from the behavior in the file
;;
;; app/src/booking_app/start.cljs
;;
;; After creating a new application, set the effect handler function
;; to receive effect
;;
;; (app/consume-effect app services-fn)
;;
;; A very simple example of a services function which echos all events
;; back to the behavior is shown below

(comment

  ;; The services implementation will need some way to send messages
  ;; back to the application. The queue passed to the services function
  ;; will convey messages to the application.
  (defn echo-services-fn [message queue]
    (put-message queue message))
  
  )

;; During development, it is helpful to implement services which
;; simulate communication with the real services. This implementaiton
;; can be placed in the file
;;
;; app/src/booking_app/simulated/services.cljs
;;
