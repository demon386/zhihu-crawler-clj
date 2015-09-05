(ns zhihu-crawler.url)

(defn categorize-page [url node]
  (cond
    (re-matches #"^http://www.zhihu.com/people/.*" url) :people
    (re-matches #"^http://www.zhihu.com/question/.*" url) :answer
    (re-matches #"^http://www.zhihu.com/topic/.*" url) :topic
    :else :unknown))
