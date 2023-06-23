(ns omniward.postgres.db-test
  (:require
   [clojure.test :refer [is testing deftest] :as t]
   [omniward.postgres.db :refer [insert-patient]]
   [java-time.api :as jt]
   [clojure.java.jdbc :as j]
   [test-common :refer [test-db-spec]]))

(def test-patient-ddl
  (j/create-table-ddl
   :patient
   [[:patient_id :serial "PRIMARY KEY"]
    [:name "VARCHAR(255)"]
    [:gender "VARCHAR(10)"]
    [:dob "DATE"]
    [:address "VARCHAR(255)"]
    [:phone "VARCHAR(20)"]
    [:CONSTRAINT :unique_test_patient "UNIQUE(name, dob)"]]))

(defn create-test-db []
  (j/execute! test-db-spec test-patient-ddl))

(defn drop-test-db []
  (j/execute! test-db-spec "DROP TABLE IF EXISTS patient"))

(deftest insert-patient-test
  (create-test-db)
  (let [patient-data {:p-name "Jane Smith"
                      :gender "Female"
                      :dob (jt/local-date (jt/sql-date 1995 6 21))
                      :address "456 Elm St"
                      :phone "555-5678"}]
    (testing "Inserting a new patients record to db"
      (is (->
           (insert-patient test-db-spec patient-data)
           first
           :patient_id
           int?))))
  (drop-test-db))

(comment
  (t/run-tests *ns*))