(ns booking-app.services
  (:require [cljs.reader :as r]
            [io.pedestal.app.net.xhr :as xhr]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]))


(def *booking-url* "/booking") ;"http://localhost:3344/booking"
                   

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
                   *booking-url*
                   :request-method "POST"
                   :headers {"Content-Type" "application/edn"}
                   :body body
                   :on-success success-fn
                   :on-error err-fn))
    
)


(defn- refresh-fn [message i-q]
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
                                        {msg/topic :booking msg/type :refresh-seats :value (:seats resp) })
                         (do 
                           (js/alert "Помилка оновлення"))))
                     
                     )]
      (xhr/request (gensym)
                   *booking-url*
                   :request-method "GET"
                   :headers {"Content-Type" "application/edn"}
                   :body body
                   :on-success success-fn
                   :on-error err-fn))
)


(defn services-fn [message i-q]
  (when-let [msg (msg/type message) ]
    (cond 
      (= msg/init msg)  (refresh-fn message i-q)
      (= :book msg)     (book-fn message i-q)
      (= :refresh msg)  (refresh-fn message i-q)
      :else nil )))



