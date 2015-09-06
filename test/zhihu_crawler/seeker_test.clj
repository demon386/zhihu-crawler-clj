(ns zhihu-crawler.seeker-test
  (:require [net.cgrand.enlive-html :as html]
            [zhihu-crawler.seeker :as seeker :refer [seek]]
            [zhihu-crawler.test-utils :refer [nodes]]
            [zhihu-crawler.parser :as parser]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]])
  (:import [zhihu_crawler.seeker SidebarSeeker]))

(expose-testables zhihu-crawler.seeker)

(fact "private url processing functions work"
      (fact "clean and filter urls works"
            (let [urls ["#" "javascript:(0)"
                        "http://www.zhihu.com/test"
                        "https://www.zhihu.com/test"
                        "http://www.sohu.com/test"
                        "https://www.sohu.com/test"]
                  results (clean-and-filter-urls urls)]
              (count results) => 2
              results => (just "http://www.zhihu.com/test"
                               "https://www.zhihu.com/test")))
      (fact "clean redundant urls for zhihu"
            (let [urls ["/question/30762106/answer/49413938"
                        "/terms/#sec-licence-1"]
                  results (clean-redundant-urls-for-zhihu urls)]
              (count results) => 1))
      (fact "complement urls"
            (let [urls ["/question/30762106/answer/49413938"
                        "http://www.zhihu.com/hello"]
                  results (complement-zhihu-urls urls)]
              results => (just ["http://www.zhihu.com/question/30762106/answer/49413938"
                                "http://www.zhihu.com/hello"])))
      (fact "transform answer urls to question urls"
            (let [urls ["http://www.zhihu.com/question/22464072/answer/61761400"
                        "http://www.zhihu.com/question/22464072"
                        "/first"]
                  results (transform-answer-urls-to-question-urls urls)]
              results => (just ["http://www.zhihu.com/question/22464072"
                                "http://www.zhihu.com/question/22464072"
                                "/first"]))))

(fact "successfuly parse sidebar info"
      (let [urls (seek (SidebarSeeker.) nodes)]
        urls => (contains "http://www.zhihu.com/question/19687491")))
