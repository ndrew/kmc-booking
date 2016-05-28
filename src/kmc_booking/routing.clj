(ns kmc-booking.routing
	(:require [compojure.core :refer [defroutes GET POST ANY]]
               [clojure.string :as str]

               [ring.util.response :as ring]
               [ring.middleware.params :as params]

               [kmc-booking.core :as core]

               [cemerick.friend :as friend]
               (cemerick.friend [workflows :as workflows]
                                [credentials :as creds])

               [cognitect.transit :as transit]
               [clojure.java.io :as io]
              )
  (:import
      [java.io ByteArrayOutputStream ByteArrayInputStream])
)


(defn write-transit-bytes [x]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos :json {})]
    (transit/write w x)
    (.toByteArray baos)))


(defn wrap-transit-response [handler]
  (fn [request]
    (let [response (handler request)]
       (if (get response :transit)
         (update-in
          (update-in response [:headers] assoc "Content-Type" "application/transit+json; charset=utf-8")
          [:body] #(io/input-stream (write-transit-bytes %)))
         response
         )

       )))



(defn booking [req]
  (let [{{name :name
          phone :phone
          seats :seats} :params} req
          seats (clojure.string/split seats #";")]

        (try
          (do
            (doseq [id seats]
                ;(println (pr-str (get @core/seats id)))

                (when-not (get @core/seats id)
                    (throw (Exception. (str "Немає місця " id))))

                ;(println (pr-str (get-in @core/seats [id :status])))

                (when-not (= "free" (get-in @core/seats [id :status]))
                    (throw (Exception. (str "Хтось замовив місце " id " швидше!")))))


            (core/create-booking name phone seats)
            )
        (catch Exception e
          {:error (.getMessage e)}))))


(defn seats []
  (when (empty? @core/seats) ;; for figwheel
    (core/init!))

  ;; serve from memory!
  @core/seats

)


(defn admin[]
  (io/resource "admin.html")
)


(defroutes routes
  (GET "/test" req (core/playground))


  (GET "/" [] (ring/redirect "landing/index.html"))
  ;(GET  "/" [] (index))

  (friend/logout (ANY "/logout" request
                              (ring/redirect "/")))

  (GET "/admin" req
    (friend/authenticated
      (admin)))
)

(defn admin-bookings[]
  (if (friend/current-authentication)
    (do
      (core/get-bookings)

      )
    {:error "Forbidden"}
    )
)


(defn admin-discard[req]
  (if (friend/current-authentication)
    (do
      (let [{{id :booking_id} :params} req]
          (core/cancel-booking id)
        )
      )
    {:error "Forbidden"}
    )
)


(defn admin-confirm[req]
  (if (friend/current-authentication)
    (do
      (let [{{id :booking_id} :params} req]
          (core/confirm-booking id)
        )
      )
    {:error "Forbidden"}
    )
)


(defroutes api
  (wrap-transit-response
    (compojure.core/context "/api" []
          (GET "/seats" [] {:body (seats)
                            :transit true})
          (POST "/book" req {:body (booking req)
                             :transit true})

          (GET "/bookings" [] {:body (admin-bookings)
                               :transit true})

          (POST "/discard" req {:body (admin-discard req)
                                :transit true})

          (POST "/confirm" req {:body (admin-confirm req)
                                :transit true})

          )

    )
  )
