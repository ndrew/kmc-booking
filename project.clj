(defproject kmc-booking "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [postgresql "9.3-1102.jdbc41"]
                 [ring/ring-jetty-adapter "1.4.0-beta1"]
                 [compojure "1.3.3"]
                 [hiccup "1.0.5"]]

  :plugins [[lein-ring "0.9.3"]]
  :ring {:handler kmc-booking.web/application
         :init    kmc-booking.web/init
        :destroy  kmc-booking.web/destroy}

  :min-lein-version "2.0.0"
  :main ^:skip-aot kmc-booking.web
  :uberjar-name "kmc-booking-standalone.jar"
  :profiles {:uberjar {:aot :all}})