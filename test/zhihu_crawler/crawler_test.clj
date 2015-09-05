(ns zhihu-crawler.crawler-test
  (:require  [midje.sweet :refer :all]
             [itsy.core :as itsy])
  (:import [java.util.concurrent LinkedBlockingQueue TimeUnit]))

(def seed-url "http://www.zhihu.com/question/30762106")

(def queue (LinkedBlockingQueue.))

(defn my-handler [{:keys [url body]}]
  (.put queue [url body]))

(fact :integration "crawl-zhihu"
      (let [_ (itsy/crawl {:url seed-url
                           :handler my-handler
                           :url-limit 1
                           :workers 3})
            [url body] (.poll queue 5 TimeUnit/SECONDS)]
        url => seed-url))

