(ns zhihu-crawler.graph-test
  (:require [zhihu-crawler.graph :refer :all]
            [zhihu-crawler.test-utils :refer [fact-stub-warn nodes
                                           snippet-to-str]]
            [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as html]))

(fact-stub-warn "return nil for unknown url type"
                [:times 2]
                (process-page {:url "hello" :node nodes}) =>
                {:parse-result nil :seek-urls nil})

(fact "zhihu answer page url"
      (let [{:keys [parse-result seek-urls]}
            (process-page {:url "http://www.zhihu.com/question/30762106"
                           :node nodes})]
        seek-urls => (contains "http://www.zhihu.com/question/19687491")
        (snippet-to-str parse-result) => (contains "上海屌丝")))
