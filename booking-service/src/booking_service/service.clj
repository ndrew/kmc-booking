(ns booking-service.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              
              
             [io.pedestal.service.log :as log]
            ;; the impl dependencies will go away
            ;; these next two will collapse to one
            [io.pedestal.service.interceptor :as interceptor :refer [definterceptorfn defon-response]]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [io.pedestal.service.http.route.definition :refer [expand-routes]]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.route :as route]
            [ring.util.response :as ring-response]
              
              [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defroutes routes
  [[["/foo" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ;^:interceptors [(body-params/body-params) bootstrap/html-body]
     ;["/about" {:get about-page}]
     
     ]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))


(defon-response default-cache-control-to-no-cache
  [response]
  (update-in response [:headers "Cache-Control"] #(or % "no-cache")))


;; Consumed by booking-service.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              
              ::bootstrap/interceptors [
                             default-cache-control-to-no-cache
                             (middlewares/file-info)
                             (middlewares/file "public")
                             
                             bootstrap/not-found
                             bootstrap/log-request
                             servlet-interceptor/exception-debug
                             middlewares/cookies
                             (middlewares/params)
                             (route/router routes)]
              
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
