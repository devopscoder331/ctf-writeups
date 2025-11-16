(ns utils
  (:require [clojure.string]
            [cheshire.core]))

;; my super secure sanitize function, proud of it 💪
(defn sanitize [unsafe]
  (clojure.string/escape unsafe {\& "&amp;", \< "&lt;", \> "&gt;" \" "&quot;" \' "&#039"}))

(defn json-error [msg] {:body (cheshire.core/generate-string {:result "error" :msg msg})
                        :headers {"Content-Type" "application/json"}})

(defn json-success
  ([] (cheshire.core/generate-string {:result "success"}))
  ([msg] {:body (cheshire.core/generate-string {:result "success" :msg msg})
          :headers {"Content-Type" "application/json"}}))
  