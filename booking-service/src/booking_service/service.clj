(ns booking-service.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [io.pedestal.service.log :as log]
            ;; the impl dependencies will go away
            ;; these next two will collapse to one
            [io.pedestal.service.interceptor :as interceptor :refer [definterceptor definterceptorfn defon-response defon-request defafter defbefore]]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [io.pedestal.service.interceptor :as interceptor]
            [io.pedestal.service.http.route.definition :refer [expand-routes]]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.route :as route]
            ;
            [io.pedestal.service.http.sse :refer :all]
            ;
            [ring.util.response :as ring-resp]
            [ring.middleware.session.cookie :as cookie]
            [clojure.data.codec.base64 :as base64]
            
            [booking-service.db :as db]))


(defn- byte-transform
  "Used to encode and decode strings.  Returns nil when an exception
  was raised."
  [direction-fn string]
  (try
    (reduce str (map char (direction-fn (.getBytes string))))
    (catch Exception _)))


(defn- encode-base64
  "Will do a base64 encoding of a string and return a string."
  [^String string]
  (byte-transform base64/encode string))


(defn- decode-base64
  "Will do a base64 decoding of a string and return a string."
  [^String string]
  (byte-transform base64/decode string))


;(defn about-page
;  [request]
;  (ring-resp/response (format "<html><body>%s</body></html>" (pr-str request))))


(defn home-page
  [request]
 
   (println (pr-str request)) 

  {:status 200 
   :headers {
             "Content-Type" "text/html; charset=utf-8"
             }
   
   :body (if (= (:query-string request) "ive-mode-on")
           (do 
             (println "=============")
             (println " IVE MODE ON")
             (println "=============")
             
           (clojure.string/replace (slurp "public/booking-app.html" :encoding "UTF-8") 
                                   #"booking-app.css" "ive.css"))
           
           (slurp "public/booking-app.html" :encoding "UTF-8")
           )
   
          })


(defn- authentication-text[s] 
  (format "Basic realm=\"%s\"" s))


;;;;;;;;;;;
; PAGES

(defn auth-page [] 
  {:status 401 
   :headers {"Content-Type" "text/html; charset=utf-8"
             "WWW-Authenticate" (authentication-text "Please authorize yourself")}
   :body "Restricted!"})


(defn admin-page [req] 
  {:status 200 
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body "Hello Admin!"})


(defn not-found-page [req] 
  {:status 200 
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (slurp "public/404.html" :encoding "UTF-8")})



(defon-request dummy-interceptor
  [request]
  (assoc request
         :data :SUPER_DATA))


(def auth-map {
  "odmin" "0dmin"})


(defn- session-id [] (.toString (java.util.UUID/randomUUID)))


(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))


(defbefore basic-auth [context]
  (println (pr-str context))
  (merge context 
         (if-let [auth-s (get (:headers (:request context)) 
                                        "authorization")]
           (let [u-p (decode-base64 (last (re-find #"^Basic (.*)$" auth-s)))
                 [login pass] (clojure.string/split u-p #":")] 
             
             (println "========PASS======")
             (println login)
             (println pass)
             
             
             
             (if (= pass
                    (get auth-map login))
                  {}
                  {:response (auth-page)}))
           {:response (auth-page)})))


;;; sse stuff

(def ^{:doc "Map of subscriber IDs to SSE contexts"} 
  subscribers (atom {}))

(defn context-key
  "Return key for given `context`."
  [cookie-name sse-context]
  (get-in sse-context [:request :cookies cookie-name :value]))


(defn add-subscriber
  "Add `context` to subscribers map."
  [cookie-name sse-context]
  (swap! subscribers assoc (context-key cookie-name sse-context) sse-context))

(defn remove-subscriber
  [cookie-name context]
  ;(log/info :msg "removing subscriber")
  (swap! subscribers dissoc (context-key cookie-name context))
  (end-event-stream context))



(declare url-for)

(defn subscribe-for
  [queue-id redirect-to request]
     (let [session-id (or (get-in request [:cookies queue-id :value])
                          (session-id))
           cookie {(keyword queue-id) {:value session-id :path "/"}}]
       (-> (ring-resp/redirect (url-for redirect-to))
           (update-in [:cookies] merge cookie))))


(def admin-subscribe (partial subscribe-for "admin-session" ::wait-for-events))

(def wait-for-events (sse-setup (partial add-subscriber "admin-session")))


(defn remove-subscriber
  "Remove `context` from subscribers map and end the event stream."
  [cookie-name context]
  ; (log/info :msg "removing subscriber")
  (swap! subscribers dissoc (context-key cookie-name context))
  (end-event-stream context))



(defn send-to-subscriber
  "Send `msg` as event to event stream represented by `context`. If
  send fails, removes `context` from subscribers map."
  [cookie-name context msg]
  (try
    ;(log/info :msg "calling event sending fn")
    (send-event context "msg" msg)
    (catch java.io.IOException ioe
      (log/error :msg "Exception from event send"
                 :exception ioe)
      (remove-subscriber cookie-name context))))
           
    
(defn send-to-subscribers
  "Send `msg` to all event streams in subscribers map."
  [cookie-name msg]
  ;(log/info :msg "sending to all subscribers")
  (doseq [sse-context (vals @subscribers)]
    (send-to-subscriber cookie-name sse-context msg)))


           
(defn admin-publish [req]
  (let [msg-data [:updated [:1_1 :1_2] ]]
    ;; pr-str won't be needed in the future
    (send-to-subscribers "admin-session" 
                         (pr-str msg-data)))
    (ring-resp/response ""))
           
           
(defn do-booking [req]
  ; (db/put-booking )
  ; (println (pr-str req))
  
  (let [{{nm :name
          tel  :tel
          selected :selected} :edn-params} req

          seat-ids (map #(let[[x y] %] (keyword (str x "_" y))) selected)
          customer-id (db/store-customer nm tel)
    
          booking-id (db/store-booking customer-id seat-ids)
        ]
            
    (bootstrap/edn-response {:status :ok
                             :customer-id customer-id
                             :booking-id booking-id
                             })
    ))
           

(defn booking-subcribe [req]
 ; tbd subscribe
  (let [seats (db/get-seats-status)
        seats-map (reduce (fn[a b] 
                            (let [[id _ _ _ status] b
                                  ui-status (cond 
                                              (= status :booking.status/pre-booked) :pending
                                              (= status :booking.status/pre-booked) :booked
                                              :else :pending)]
                              (assoc a ui-status (conj (get a ui-status)                                                        
                                                       (vec 
                                                         (map read-string 
                                                              (clojure.string/split (name id) #"_"))))))) 
                          {:selected #{}
                           :pending #{}
                           :booked #{}} seats)]
    
  (bootstrap/edn-response 
    {:status :ok :seats seats-map})))           
                      
           
           
(defroutes routes
  [[["/"  ^:interceptors [session-interceptor]
     {:get home-page}]
    ["/booking"
     ;^:interceptors [dummy-interceptor] ; add data for request 
      
      {:get booking-subcribe
       :post do-booking    
      }
      ["/odmin" ^:interceptors [basic-auth ] {:get admin-subscribe}
          ["/all" {:get wait-for-events}]
     ]]]])


;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))



(defon-response default-cache-control-to-no-cache
  [response]
  (update-in response [:headers "Cache-Control"] #(or % "no-cache")))


(defon-response cross-origin-ajax
  [response]
  (assoc response :headers
    (merge (:headers response)
           {"Access-Control-Allow-Origin" "*"
            "Access-Control-Allow-Headers" "Origin, X-Requested-With, Content-Type, Accept"
            "Access-Control-Request-Method" "POST, GET"})))


;;;;;;;;;;;;;;;;;;;;;;;;;
; custom interceptors


(defon-request log-request [req] 
  (println "==R=E=Q=U=E=S=T==")
  (println (pr-str req))
  (println "=================")
  req)



(defon-response log-response [resp]
  (println "==R=E=S=P=O=N=S=E==")
  (println (pr-str resp))
  (println "===================")
  resp)


(defafter not-found
  "An interceptor that returns a 404 when routing failed to resolve a route."
  [context]
  (if-not (servlet-interceptor/response-sent? context)
    (if-not (map? (:response context))
      (assoc context :response 
        (not-found-page (:request context)))
      context)
    context))



;;; TBD:
; 0. init web app
; 1. session for admins (storing current user)
; 2. sse for seat updating


;; Consumed by booking-service.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ::bootstrap/interceptors [
                  (body-params/body-params)
                  
                  bootstrap/log-request
                  ; tbd: remove this
                  ;log-request
                  ;log-response
                  cross-origin-ajax
                  not-found
                  
                  (middlewares/resource "public")
                  (middlewares/file-info)
                  (middlewares/file "public" {:index-files? false})

                  
                  (route/router routes)
                  
                  ]
                  ;default-cache-control-to-no-cache
                  ;
                  ;servlet-interceptor/exception-debug
                  ;middlewares/cookies
                  ;(route/router routes)
                  ;bootstrap/not-found]
              
              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 3344})



