(ns zhihu-crawler.seeker
  "Seeker find interesting links on the page to follow."
  (:require [zhihu-crawler.parser :as parser]
            [zhihu-crawler.url :as url]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [taoensso.timbre :refer [warn]]))

(defprotocol Seek
  (seek [this resource]))

(defn- ^{:testable true} clean-and-filter-urls [urls]
  (->> urls
       (map str/trim)
       (remove #{"#"})
       (remove #(re-matches #"^javascript.*" %))
       (remove #(re-matches #"^http[s]?://(?!www.zhihu.com).*" %))))

(defn- ^{:testable true} clean-redundant-urls-for-zhihu [urls]
  (->> urls
       (remove #(re-matches #"^/term.*" %))))

(defn- ^{:testable true} complement-zhihu-urls [urls]
  (letfn [(complement-func [url]
            (if (re-matches #"^/.*" url)
              (str "http://www.zhihu.com" url)
              url))]
    (map complement-func urls)))

(defn- ^{:testable true} transform-answer-urls-to-question-urls [urls]
  (letfn [(replace-func [url]
            (str/replace-first url
                               #"^(http[s]?://.*zhihu.com/question/(\d+))/answer/\d+"
                               "$1"))]
    (map replace-func urls)))

(defn- select-urls-in-sidebar [node]
  (let [sidebar-url-nodes (-> node
                              parser/select-sidebar
                              (html/select [:div#zh-question-related-questions
                                            :li :>
                                            [:a.question_link (html/attr? :href)]]))
        main-content-nodes (-> node
                              parser/select-main-content
                              (html/select [[:a (html/attr? :href)]]))
        url-func (comp :href :attrs)
        url-filter ()]
    (-> (concat (map url-func sidebar-url-nodes)
                (map url-func main-content-nodes)))))

(defn- select-urls-in-content-inner [node]
  (let [content-inner (-> node
                          (html/select [:div.zu-main-content-inner
                                        [:a (html/attr? :href)]]))
        url-func (comp :href :attrs)]
    (map url-func content-inner)))

(defn process-zhihu-urls [urls]
  (-> urls
      clean-and-filter-urls
      clean-redundant-urls-for-zhihu
      complement-zhihu-urls
      distinct))

(deftype SidebarSeeker []
  Seek
  (seek [_ node]
    (-> node
        select-urls-in-sidebar
        process-zhihu-urls)))

(deftype ContentInnerSeeker []
  Seek
  (seek [_ node]
    (-> node
        select-urls-in-content-inner
        process-zhihu-urls)))

(defmulti seek-zhihu url/categorize-page)

(defmethod seek-zhihu :answer [url node]
  (let [seeker (SidebarSeeker.)]
    (seek seeker node)))

(defmethod seek-zhihu :people [url node]
  (let [seeker (ContentInnerSeeker.)]
    (seek seeker node)))

(defmethod seek-zhihu :topic [url node]
  (let [seeker (ContentInnerSeeker.)]
    (seek seeker node)))

(defmethod seek-zhihu :unknown [url node]
  (warn "unknown url type: " url))
