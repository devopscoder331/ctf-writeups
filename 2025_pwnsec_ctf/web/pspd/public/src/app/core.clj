(ns core
  (:require [clojure.string]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.handler.dump :refer [handle-dump]]
            [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [selmer.parser :refer [render-file]]
            [middleware :refer [wrap-authenticated?]]
            [routes :refer :all]))

(defonce server (atom nil))

(selmer.parser/cache-off!)

[(defroutes app-routes
   (-> (GET "/" [user-id :as {{user-id :user-id} :session}]
         (render-file "static/index.html" {:title "Control Panel" :user-id user-id}))
       (wrap-routes wrap-authenticated?))
   (GET "/login" {{user :user-id} :session} (if (nil? user) (render-file "static/login.html" {:title "Login"}) (response/redirect "/")))
   (GET "/register" [] (render-file "static/register.html" {:title "Register"}))
   (GET "/logout" [] (->
                      (response/redirect "/login")
                      (assoc :session nil :user nil)))
  ;; debug (GET "/dump" [:as req] (handle-dump req))
   (context "/users" [] (user-routes))
   (route/resources "/static" {:root "static"})
   (route/not-found "404 Not Found"))

 ;; threading macro applies middleware chain bottom to top
 (def app (-> app-routes
              (wrap-session {:store (cookie-store) :cookie-attrs {:same-site :strict}})
              wrap-params
              wrap-stacktrace))]

(defn start-server []
  (reset! server
          (jetty/run-jetty (fn [req] (app req))
                           {:host (or (env :clojure-host) "0.0.0.0")
                            :port (Integer/parseInt (or (env :clojure-port) "8080"))
                            :join? false})))

(defn stop-server []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))

(comment ;; dev shortcuts
  (start-server)
  (stop-server))

(def -main start-server)