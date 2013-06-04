(ns booking.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn seats-edn
  [request]
  (ring-resp/response ["Testo" "Pesto"]))



(defn home-page
  [request]
  (ring-resp/response "Bla-bla-bla!"))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params)]
     ["/about" {:get about-page}]
     ["/seats" {:get seats-edn}]
     ;["/book"
     ;   ^:interceptors [verify-booking]
     ;   {:get list-bookings :post create-booking}
     ;   ["/:id"
     ;     ^:interceptors [verify-booking-ownership load-booking-from-db]
     ;     {:get view-booking :put update-booking}]]
     ]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by booking.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"
              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
