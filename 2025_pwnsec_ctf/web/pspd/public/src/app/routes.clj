(ns routes
  (:require [ring.util.response :as response]
            [compojure.core :refer :all]
            [cheshire.core :as cheshire]
            [utils :refer [sanitize json-error json-success]]
            [services]
            [middleware :refer [wrap-authenticated? wrap-admin?]]))

(def DEFAULT_JOURNAL
  "- Delivered three web challenges<br />
- Investigated a case of cheating<br />
- Fix faulty deployment<br />")

;; routes
(defn user-routes []
  (routes
   (POST "/signup" [username password journal]
     (if (some nil? [username password]) (json-error "Please enter username and password.")
         (if (< (count password) 8)
           (json-error "Password must be at least 8 characters.")
           (if (or (empty? username) (services/get-user-by-username username))
             (json-error "Invalid username.")
             (let [new-user (services/->User (str (java.util.UUID/randomUUID)) (sanitize username) password false (or journal DEFAULT_JOURNAL))]
               (services/add-user new-user)
               (json-success (format "User `%s` created." (:id new-user))))))))

   (POST "/signin" [username password]
     (if-let [user (first (filter #(and (= username (:username %)) (= password (:password %))) (vals @services/users)))]
       {:status 200 :body (cheshire/generate-string "Signed in successfully") :session {:user-id (:id user)}}
       (json-error "Incorrect username or password.")))

   (-> (GET "/:id" [id :as {{user-id :user-id} :session}]
         ;; TODO: refactor this check into a middleware
         (let [user (services/get-user-by-id id)]
          ;; unnecessary nobody can guess a uuid anyways
          ;;  (if (not= id user-id)
            ;;  (json-error "You don't have the required permissions on this object.")
           (if (nil? user)
             (json-error (format "User `%s` not found." id))
             {:headers {"Content-Type" "application/json"}
              :body (cheshire/generate-string
                     (select-keys user [:id :username :password :is-admin]))})))
       (wrap-routes wrap-authenticated?))

   (-> (GET "/:id/journal" [id :as {{user-id :user-id} :session}]
         ;; TODO: refactor this check into a middleware 
         (let [user (services/get-user-by-id id)]
          ;; unnecessary nobody can guess a uuid anyways
          ;;  (if (not= id user-id)
            ;;  (json-error "You don't have the required permissions on this object.")
           (if (nil? user)
             (json-error (format "User `%s` not found." id))
             {:headers {"Content-Type" "application/json"}
              :body (cheshire/generate-string
                      ;; i will sanitize the journal client-side, no need
                      ;; :journal (-> user :journal sanitize)
                     (:journal user))})))
       (wrap-routes wrap-authenticated?))

   (-> (POST "/:id" [id journal :as {{user-id :user-id} :session}]
         (if (services/update-user-journal-by-id user-id journal)
           (response/redirect "/") (json-error "Failed to update journal.")))
       (wrap-routes wrap-authenticated?))

   (DELETE "/:id" [id]
     (wrap-admin? (do (services/delete-user-by-id id) (json-success "User deleted."))))))

