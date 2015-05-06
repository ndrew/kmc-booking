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

                 [lein-figwheel "0.3.1"]
                 [sablono "0.3.4"]
                 [rum "0.2.6"]

                 [com.cemerick/friend "0.2.1"]
                 
                 [com.cognitect/transit-clj "0.8.271"]
                 [com.cognitect/transit-cljs "0.8.207"]
                 
                  [org.clojure/core.cache "0.6.3"]
                 ]

  :plugins [
    [lein-ring "0.9.3"]
    [lein-cljsbuild "1.0.5"]
    [lein-figwheel "0.3.1"]
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
          :warnings {:single-segment-namespace false}
        }}

      { :id "dev"
            :source-paths ["src-cljs"]
            :figwheel { 
              :on-jsload "kmc-booking.core/start"
               }
            :compiler {
              :output-to     "resources/public/booking.js"
              :output-dir    "resources/public/out"
              :optimizations :none
              :source-map    true
              :warnings {:single-segment-namespace false}

            }}
  ]}

 
  :profiles {
    :uberjar {:aot :all}

  }


  :figwheel {
             :http-server-root "public" ;; default and assumes "resources" 
             :server-port 3449 ;; default
             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             :ring-handler kmc-booking.web/application

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
             }
)