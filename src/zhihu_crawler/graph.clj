(ns zhihu-crawler.graph
  (:require [plumbing.graph :as graph]
            [plumbing.core :refer [defnk fnk]]
            [zhihu-crawler.parser :refer [parse-zhihu]]
            [zhihu-crawler.seeker :refer [seek-zhihu]]))

(def process-graph
  {:parse-result (fnk [url node] (parse-zhihu url node))
   :seek-urls (fnk [url node] (seek-zhihu url node))})

(def process-page (graph/compile process-graph))
