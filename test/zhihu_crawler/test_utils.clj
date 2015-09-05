(ns zhihu-crawler.test-utils
  (:require  [midje.sweet :as midje]
             [net.cgrand.enlive-html :as html]
             [midje.sweet :refer :all]
             [taoensso.timbre :refer [log1-fn]]))

(defmacro fact-stub-log [log-type name condition & body]
  `(fact ~name
         ~@body
         (provided
          (log1-fn anything ~log-type
                   anything anything
                   anything anything
                   anything anything) => nil
                   ~@condition)))

(defmacro fact-stub-warn [name condition & body]
  `(fact-stub-log :warn ~name ~condition ~@body))

(def local-html-file "test-resources/zhihu_example.html")

(defn read-nodes-from-local-file [^String path]
  (html/html-resource (clojure.java.io/reader path)))

(def nodes
  (read-nodes-from-local-file local-html-file))

(defn snippet-to-str [node]
  (apply str (html/emit* node)))
