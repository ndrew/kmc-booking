(ns kmc-booking.db
	(:require [clojure.java.jdbc :as sql]))


(def CONN 
	(or (System/getenv "DATABASE_URL")
		"postgresql://postgres:postgres@localhost:5432/misskma"))


(defn migrated? []
  (-> (sql/query CONN
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='testing'")])
      first :count pos?))


(defn init-db[] 
	(when-not (migrated?)
		(sql/db-do-commands CONN
	                         	(sql/create-table-ddl :testing [:data :text]))
		(sql/insert! CONN
	                    :testing {:data "Hello World"})
		)
	)


(defn get-data[] 
	(sql/query CONN
                  ["select * from testing"]))


