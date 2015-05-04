(defproject kmc-booking "0.1.0-SNAPSHOT"
  :description "KMC booking website"
  :url "https://github.com/ndrew/kmc-booking"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [postgresql "9.3-1102.jdbc41"]
                 [ring/ring-jetty-adapter "1.4.0-beta1"]
                 [compojure "1.3.3"]
                 [hiccup "1.0.5"]

                 [org.clojure/clojurescript "0.0-3211"]

                 ]

  :plugins [
    [lein-ring "0.9.3"]
    [lein-cljsbuild "1.0.5"]
    ]

  :ring {:handler kmc-booking.web/application
         :init    kmc-booking.web/init
        :destroy  kmc-booking.web/destroy}


  :global-vars  {*warn-on-reflection* true}

  :min-lein-version "2.0.0"
  :main ^:skip-aot kmc-booking.web
  :uberjar-name "kmc-booking-standalone.jar"

  :hooks [leiningen.cljsbuild]
  :cljsbuild { 
    :builds [
      { :id "prod"
        :source-paths ["src-cljs"]
        :jar true
        :compiler {
          ;:preamble      ["public/md5.js"]
          :output-to     "resources/public/booking.min.js"
          :optimizations :advanced
          :pretty-print  false
        }}
  ]}

 
  :profiles {
    :uberjar {:aot :all}
    
    :dev {
      :cljsbuild {
        :builds [
          { :id "dev"
            :source-paths ["src-cljs"]
            :compiler {
              :output-to     "resources/public/booking.js"
              :output-dir    "resources/public/out"
              :optimizations :none
              :source-map    true
            }}
      ]}
    }
  }

)