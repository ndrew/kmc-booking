(ns booking-service.db-test
  (:require [clojure.test :refer :all])
  (:use [datomic.api :only (q db) :as d]))


;(def uri "datomic:free://localhost:4334/test")
(def uri "datomic:mem://hello")




(comment (deftest test-db-creation
    
  (d/create-database uri)
  (def conn (d/connect uri))

  ;; parse schema edn file
  (def schema-tx (read-string (slurp "resources/booking-schema.edn")))
  
  (is (vector? schema-tx))
  (is (map? (first schema-tx)))

  ;; submit schema transaction
  @(d/transact conn schema-tx)

  
  
  ;; add seats info
  @(d/transact conn 
               [{:db/id (d/tempid :db.part/user)
                 :seats/id :1_1}])

  @(d/transact conn 
               [{:db/id (d/tempid :db.part/user)
                 :seats/id :1_2}
                
                {:db/id (d/tempid :db.part/user)
                 :seats/id :1_3}
                
                {:db/id (d/tempid :db.part/user)
                 :seats/id :1_4}
                ; ...
                ])


  
  (def test-seat-id (ffirst 
                      (q '[:find ?s
                           :where [?s :seats/id :1_1]]
                         (db conn))))
  (println (str "test seat is " test-seat-id ))

   (def test-seat-id-1 (ffirst 
                      (q '[:find ?s :where [?s :seats/id :1_2]] (db conn))))
 
  
  ;; add customers
  @(d/transact conn 
               [{:customer/id 1
                 :customer/tel "0931234567"
                 :customer/name "test"
                 :customer/comment "test comment"
                 :db/id (d/tempid :db.part/user)}])


  (def test-user-id (ffirst 
             (q '[:find ?s
                  :where [?s :customer/id 1]]
                (db conn))))
  (println (str "test user is " test-user-id ))
   

   ;; make booking
  @(d/transact conn 
               [{:booking/id 1
                 :booking/user test-user-id
                 :booking/seats [test-seat-id test-seat-id-1]
                 :booking/status :booking.status/pre-booked
                 :booking/comment "FFFUU!!"
                 :db/id (d/tempid :db.part/user)}])

  (def test-booking-id (ffirst 
             (q '[:find ?s
                  :where [?s :booking/id 1]]
                (db conn))))

  (println (str "test booking is " test-booking-id ))



  (def booked-seats   
               (q '[:find ?id ?b-id ?c ?st :where
                    [?s :seats/id _]
                    [?s :seats/id ?id]
                    [?b :booking/seats ?s]
                    [?b :booking/id ?b-id]
                    [?b :booking/status ?_st]
                    [?b :booking/user ?c]
                    [?_st :db/ident ?st]]
                (db conn)))
  
  
  (println (pr-str booked-seats))
  
)
  )