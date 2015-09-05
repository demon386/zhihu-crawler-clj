(ns zhihu-crawler.parser-test
  (:require [midje.sweet :refer :all]
            [zhihu-crawler.parser :as parser :refer [parse]]
            [zhihu-crawler.test-utils :refer [nodes]]
            [net.cgrand.enlive-html :as html])
  (:import [zhihu_crawler.parser ZhihuParser
            ZhihuMainContentParser ZhihuQuestionParser ZhihuAnswersParser]))

(fact "parse-zhihu-main-content-question-title"
      (let [result (parse (ZhihuMainContentParser.) nodes)]
        (:question-title result)) => "高考真的会改变人的命运吗？")

(fact "parse-zhihu-main-content-question-desc"
      (let [result (parse (ZhihuMainContentParser.) nodes)]
        (:question-desc result))
      => "如题。见多了家世背景以及人脉关系铸就各个领域成功的例子，对“知识改变命运”一句话想要进行深入的探讨。")

(fact "parse-zhihu-answers-author-info"
      (let [results (-> nodes
                        parser/select-main-content
                        parser/parse-answers-author-info)]
        (count results) => 20
        (map :author-name results) => (contains "知乎用户")
        (map :author-name results) => (contains "匿名用户")
        results =>
        (contains {:author-name "陈君明",
                   :author-avatar "//pic4.zhimg.com/10b8057f3_s.jpg",
                   :author-url "/people/chen-jun-ming-7"}
                  )))

(fact "parse-zhihu-answers-content"
      (let [results (-> nodes
                        parser/select-main-content
                        parser/parse-answers-content)]
        (count results) => 20
        (-> results first :answer-content)
        => #(parser/node-content-contains-str? % "我是不资瓷的")))

(fact "zhihu-answers-content-parser"
      (let [main-content-node (parser/select-main-content nodes)
            results (-> (parse (ZhihuAnswersParser.) main-content-node)
                        :answers)
            first-result (first results)]
        (count results) => 20
        (:author-name first-result) => "知乎用户"
        (:author-avatar first-result) =>
        "//pic1.zhimg.com/da8e974dc_s.jpg"
        ((comp :tag :answer-content) first-result) =>
        :div))

(fact "zhihu-answers-error-content-parser"
      (let [main-content-node (parser/select-main-content nodes)]
        (-> (parse (ZhihuAnswersParser.) main-content-node)) => (throws IllegalArgumentException)
        (provided (parser/parse-answers-author-info main-content-node)
                  => [{:hello "hello"} {:hello "hello"}])))

