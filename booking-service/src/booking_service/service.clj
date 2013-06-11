(ns booking-service.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [io.pedestal.service.log :as log]
            ;; the impl dependencies will go away
            ;; these next two will collapse to one
            [io.pedestal.service.interceptor :as interceptor :refer [definterceptorfn defon-response defon-request defafter defbefore]]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [io.pedestal.service.interceptor :as interceptor]
            [io.pedestal.service.http.route.definition :refer [expand-routes]]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.route :as route]
            [ring.util.response :as ring-resp]
            [clojure.data.codec.base64 :as base64]))


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


(defn about-page
  [request]
  (ring-resp/response (format "<html><body>%s</body></html>" (pr-str request))))


(defn home-page
  [request]
  (ring-resp/response "Hello World!"))


(defn- authentication-text[s] 
  (format "Basic realm=\"%s\"" s))


;;;;;;;;;;;
; PAGES

(defn auth-page [] 
  {:status 401 
   :headers {"Content-Type" "text/html; charset=utf-8"
             "WWW-Authenticate" (authentication-text "ПАРОЛЬ!")}
   :body "Restricted!"})



(defn admin-page [req] 
  {:status 200 
   :headers {
             "Content-Type" "text/html; charset=utf-8"
             }
   :body "Hello Admin!"})




(defn not-found-page [req] 
  {:status 200 
   :headers {"Content-Type" "Content-Type: text/html; charset=utf-8"}
   ; bad, but will work for sometime
   :body (slurp "public/404.html")})



(defon-request dummy-interceptor
  [request]
  (assoc request
         :data :SUPER_DATA))


(def auth-map {
  "odmin" "odmin"})


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


(defroutes routes
  [[["/"
     ^:interceptors [dummy-interceptor] ; add data for request 
     {:get about-page}]
    ["/odmin" 
     ^:interceptors [basic-auth]
     {:get admin-page}]]])


;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))


(defon-response default-cache-control-to-no-cache
  [response]
  (update-in response [:headers "Cache-Control"] #(or % "no-cache")))


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


;; Consumed by booking-service.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ::bootstrap/interceptors [
                  log-request
                  
                  log-response
                  
                  not-found
                  
                  (route/router routes)
                  
                  (middlewares/file-info)
                  (middlewares/file "public" {:index-files? false})
                  
                  ]
                  ;default-cache-control-to-no-cache
                  ;bootstrap/log-request
                  ;servlet-interceptor/exception-debug
                  ;middlewares/cookies
                  ;(middlewares/params)
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



