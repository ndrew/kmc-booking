(defproject booking-service "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [io.pedestal/pedestal.service "0.1.9-SNAPSHOT"]

                 ;; Remove this line and uncomment the next line to
                 ;; use Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.1.9-SNAPSHOT"]
                 ;; [io.pedestal/pedestal.tomcat "0.1.9-SNAPSHOT"]
                 [com.datomic/datomic-free "0.8.3993" 
                  :exclusions [org.slf4j/slf4j-nop org.slf4j/slf4j-log4j12]]
                 
                 ;; Logging
                 [ch.qos.logback/logback-classic "1.0.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.2"]
                 [org.slf4j/jcl-over-slf4j "1.7.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.2"]
                 
                 [org.clojure/data.codec "0.1.0"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources", "public"]
  :main ^{:skip-aot true} booking-service.server)


;datomic:free://localhost:4334/