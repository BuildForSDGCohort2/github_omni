(ns omniward.postgres.db
  (:require [integrant.core :as ig]
            [clojure.java.jdbc :as j]
            [java-time.api :as jt]))

(defmethod ig/init-key ::pg-db
  [_ config]
  config)

(def patients-sql
  (j/create-table-ddl
   :patient
   [[:patient_id :serial "PRIMARY KEY"]
    [:name "VARCHAR(255)"]
    [:gender "VARCHAR(10)"]
    [:dob "DATE"]
    [:address "VARCHAR(255)"]
    [:phone "VARCHAR(20)"]
    [:CONSTRAINT :unique_patient "UNIQUE(name, dob)"]]))

(defn create-patients-table
  [db]
  (let [db-spec db]
    (j/execute! db-spec patients-sql)))

(defn get-patient-info
  [db-spec patient-id]
  (j/query
   db-spec
   ["select * from patient where patient_id = ?" patient-id]))

(defn get-patients
  ([db-spec]
   (get-patients db-spec {:offset nil :limit 100}))

  ([db-spec {:keys [offset limit]}]
   (let [limit     (or limit 100)
         query-str (cond-> (str "select * from patient limit " limit)
                     offset (str " offset " offset))]
     (j/query
      db-spec
      [query-str]))))

(defn insert-patient
  [db-spec patient]
  (let [{:keys [p-name gender dob address phone]} patient]
    (j/insert!
     db-spec
     :patient {:name    p-name
               :gender  gender
               :dob     (jt/local-date (jt/sql-date dob))
               :address address
               :phone   phone})))

(defn update-patient
  [db-spec patient]
  (let [{:keys [update-val patient-id]} patient]
    (j/update!
     db-spec
     :patient
     update-val
     ["patient_id=?" patient-id])))

(defn delete-patient
  [db-spec patient-id]
  (j/delete! db-spec :patient ["patient_id=?" patient-id]))