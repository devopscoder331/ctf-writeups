(ns middleware
  (:require [ring.util.response :as response]
            [services]
            [utils :refer [json-error]]))

;; our custom middlewares
(defn wrap-authenticated? [handler]
  (fn [request]
    (if-let [user (-> request :session :user-id services/get-user-by-id)]
      (handler (assoc request :user user))
      (response/redirect "/login"))))

(defn wrap-admin? [handler]
  (fn [request]
    (if (-> request :user :is-admin)
      (handler request)
      (merge {:status 401} (json-error "Unauthorized")))))