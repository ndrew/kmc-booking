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


(defn- book-fn [message i-q]
  (let [body (pr-str (:value message))
        log (fn [args]
                (.log js/console (pr-str args))
                (.log js/console (:xhr args)))
        err-fn (fn [args]
                 (p/put-message i-q 
                                {msg/topic :booking
                                 msg/type :failed
                                 :value args}))
        success-fn (fn[args] 
                     (let [{body :body} args
                           resp (cljs.reader/read-string body)
                           {status :status} resp]
                       (if (= :ok status)
                         (p/put-message i-q 
                                        {msg/topic :booking msg/type :success :value resp })
                         (do 
                           (js/alert "Помилка букінга")))))]
      (xhr/request (gensym)
                   "/booking"
                    ;"http://localhost:3344/booking"
                   :request-method "POST"
                   :headers {"Content-Type" "application/edn"}
                   :body body
                   :on-success success-fn
                   :on-error err-fn))
    
;    (.log js/console (str "Send to Server: " (pr-str message)))
)


(defn- refresh-fn [message i-q]
  (let [body (pr-str (:value message))
        log (fn [args]
                (.log js/console (pr-str args))
                (.log js/console (:xhr args)))
        err-fn (fn [args]
                 (p/put-message i-q 
                                {msg/topic :refresh
                                 msg/type :failed
                                 :value args}))
        success-fn (fn[args] 
                     (let [{body :body} args
                           resp (cljs.reader/read-string body)
                           {status :status} resp]
                       (if (= :ok status)
                         (p/put-message i-q 
                                        {msg/topic :refresh msg/type :success :value args })
                         (do 
                           (js/alert "Помилка оновлення"))))
                     
                     )]
      (xhr/request (gensym)
                   "/booking"
                  ;"http://localhost:3344/booking"
                   :request-method "GET"
                   :headers {"Content-Type" "application/edn"}
                   :body body
                   :on-success success-fn
                   :on-error err-fn))
)


(defn services-fn [message i-q]
  (.log js/console "service call for message " (pr-str message))
  
  (when-let [msg (msg/type message) ]
    (cond 
      (= msg/init msg)  (refresh-fn message i-q)
      (= :book msg)     (book-fn message i-q)
      (= :refresh msg)  (refresh-fn message i-q)
      )
    ))