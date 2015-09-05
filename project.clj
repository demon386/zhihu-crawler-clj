(defproject zhihu-crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "1.1.2"]
                 [enlive "1.1.5"]
                 [itsy "0.1.1"]
                 [com.taoensso/timbre "4.1.1"]
                 [prismatic/plumbing "0.4.4"]
                 [midje "1.7.0"]]
  :main zhihu-crawler.core
  :profiles {:dev {:plugins [[lein-midje "3.1.3"]]}})
