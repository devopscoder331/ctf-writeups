(ns services
  (:require [environ.core :refer [env]]))

;; records
(defrecord User [id username password is-admin journal])

;; synchronized store
(def users (atom {}))

;; services
(defn add-user [user] (swap! users assoc (str (:id user)) user))
(defn get-user-by-id [id] (get @users id))
(defn get-user-by-username [username] (first (filter #(= username (:username %)) (vals @users))))
(defn delete-user-by-id [username] (swap! users dissoc username))
(defn update-user-journal-by-id [id journal] (if-let [user (get-user-by-id id)]
                                               (swap! users assoc id
                                                      (assoc user :journal journal)) nil))

;; initialization
(def admin (->User (str (java.util.UUID/randomUUID)) "aelmo" (env :admin-password) true (format "- %s" (env :flag))))
(add-user admin)

;; repl shortcuts
(comment
  (delete-user-by-id "")
  (update-user-journal-by-id "" "Hey!")
  (get-user-by-username "aelmo")
  (get-user-by-id "")
  (map :username (vals @users))
  (map :id (vals @users))
  (last (map :id (vals @users)))
  (last @users)
  (->> @users (vals) (map (comp :id)))
  (count @users)
  (vals @users)
  (def admin (->User (str (java.util.UUID/randomUUID)) "aelmo" "s3cret" true "<b>hey</b>"))
  (add-user admin)
  (reset! users {})
  (get-user-by-id "aelmo")
  (get-user-by-id "notexistent"))