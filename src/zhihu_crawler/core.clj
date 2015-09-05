(ns zhihu-crawler.core
  (:require [itsy.core :as itsy]
            [plumbing.core :refer [defnk fnk]]
            [net.cgrand.enlive-html :as html]
            [zhihu-crawler.graph :refer [process-graph]]
            [zhihu-crawler.parser :refer [parse-zhihu]]
            [zhihu-crawler.seeker :refer [seek-zhihu]])
  (:import [java.io StringReader])
  (:gen-class))

(defn parse-html-src [^String src]
  (html/html-resource (StringReader. src)))

(def seed-url "http://www.zhihu.com/question/30762106")

(defn snippet-to-str [node]
  (apply str (html/emit* node)))

(defn -main [& args]
  (let [_ (itsy/crawl {:url seed-url
                       :handler (fnk [url body]
                                  (let [result (parse-zhihu url (parse-html-src body))]
                                    (println url)
                                    (println (snippet-to-str result))))
                       :url-extractor (fn [url body]
                                        (let [result (seek-zhihu url (parse-html-src body))]
                                          (println "new urls: " result)
                                          result))
                       :url-limit 100
                       :worker 3})]))
