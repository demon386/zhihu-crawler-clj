(ns zhihu-crawler.parser
  (:require [net.cgrand.enlive-html :as html]
            [net.cgrand.xml :as xml]
            [clojure.string :as str]
            [taoensso.timbre :refer [warn trace]]
            [zhihu-crawler.url :as url]))

;; Feed with a full node and return a map containing information.
(defprotocol Parse
  (parse [this resource]))

;; selector surrounding main-content
(defn select-main-content [full-node]
  (-> full-node
      (html/select [:div.zu-main-content-inner])))

(defn select-sidebar [full-node]
  (-> full-node
      (html/select [:div.zu-main-sidebar])))

(defn- extract-node-content-as-str [node]
  (-> node
      first
      :content
      first
      (#(when % (str/trim %)))))

;; question
(defn parse-question-title [main-content-node]
  {:question-title
   (-> main-content-node
      (html/select [:div#zh-question-title :h2.zm-item-title])
      extract-node-content-as-str)})

(defn parse-question-desc [main-content-node]
  {:question-desc
   (-> main-content-node
       (html/select [:div#zh-question-detail :> :div])
       extract-node-content-as-str)})

(defn node-content-contains-str? [node s]
  (cond
    (string? node)
    (.contains node s)
    (and (xml/tag? node) (:content node))
    (some #{true} (map #(node-content-contains-str? % s) (:content node)))
    (seq? node)
    (some #{true} (map #(node-content-contains-str? % s) node))))


(defn- parse-author-node [author-node]
  (let [author-name
        (-> author-node
            (html/select
              [[:a (html/attr? :href)
                (html/but :.zm-item-link-avatar)]])
            extract-node-content-as-str)
        author-name-refined
        (cond author-name author-name
              (node-content-contains-str? author-node "匿名用户") "匿名用户"
              (node-content-contains-str? author-node "知乎用户") "知乎用户")
        author-avatar
        (-> author-node
            (html/select [:img])
            first
            :attrs
            :src)
        author-url
        (-> author-node
            (html/select [[:a (html/attr? :href)
                           (html/but :.zm-item-link-avatar)]])
            first
            :attrs
            :href) ]
    {:author-name author-name-refined
     :author-avatar author-avatar
     :author-url author-url}))
;; answers
(defn parse-answers-author-info [main-content-node]
  (let [author-node
        (-> main-content-node
            (html/select [:div.zm-item-answer-author-info
                          :h3.zm-item-answer-author-wrap]))]
    (map parse-author-node author-node)))

(defn parse-answers-content [main-content-node]
  (-> main-content-node
      (html/select [:div.zm-item-answer :>
                    :div.zm-item-rich-text :>
                    :div.zm-editable-content])
      (->> (map (fn [x] {:answer-content x})))))

(deftype ZhihuQuestionParser []
  Parse
  (parse [_ main-content-node]
    (->> (for [parse-func [parse-question-title
                           parse-question-desc]]
           (parse-func main-content-node))
         (apply merge))))

(deftype ZhihuAnswersParser []
  Parse
  (parse [_ main-content-node]
    (let [parse-funcs [parse-answers-author-info
                       parse-answers-content]
          results (map #(% main-content-node) parse-funcs)
          results-counts (map count results)]
      (if (= (count (distinct results-counts)) 1)
        {:answers (apply map merge results)}
        (throw (IllegalArgumentException.
                 (str "error: results doesn't match: " (doall results))))))))

(deftype ZhihuMainContentParser []
  Parse
  (parse [_ main-content-node]
    (let [question-info
          (parse (ZhihuQuestionParser.) main-content-node)
          answers
          (parse (ZhihuAnswersParser.) main-content-node)]
      (merge question-info answers))))

(deftype ZhihuParser []
  Parse
  (parse [_ full-node]
    (parse (ZhihuMainContentParser.)
           (select-main-content full-node))))

(defmulti parse-zhihu url/categorize-page)

(defmethod parse-zhihu :answer [url node]
  (let [parser (ZhihuParser.)]
    (trace "parsing zhihu question page...")
    (parse parser node)))

(defmethod parse-zhihu :people [url node])

(defmethod parse-zhihu :topic [url node])

(defmethod parse-zhihu :unknown [url node]
  (warn (str "unknown url type: " url)))
