(defproject org.blancas/eisen "0.2.2"
  :description "A Language for Programmable Applications"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/blancas/eisen"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.2.2"]
                 [org.blancas/kern "1.1.0"]
                 [org.blancas/morph "0.3.0"]]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :jvm-opts ["-Dfile.encoding=UTF-8"]
  :profiles
  {:1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :dev {:resource-paths ["src/main/resources" "src/test/resources"]
         :dependencies   [[midje "1.8.3" :exclusions [org.clojure/clojure]]]
         :plugins        [[codox "0.6.4"]]
         :codox          {:sources ["src/main/clojure"] :output-dir "codox"}}}
  :deploy-repositories
  [["releases" {:url "http://artifactory.service.consul:8081/artifactory/libs-release-local"}]
   ["snapshots" {:url "http://artifactory.service.consul:8081/artifactory/libs-snapshot-local"}]])
