(ns porcupine.core-test
  (:require [clojure.test :refer :all]
            [porcupine.core :refer :all]
            [porcupine.test-util :as test-util]))

(comment (deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1)))))

(deftest has-resource-test
  (testing "Checking for existing resource in resource collection"
    (test-util/harness
     (fn [resources]
       (add-resource resources :javascript "bing/bung.js")
       (is (= true (has-resource? resources :javascript "bing/bung.js")))
       (is (= false (has-resource? resources :javascript "jubjub.js")))))))

(deftest add-resource-test
  (testing "Adding a resource"
    (test-util/harness
     (fn [resources]
       (add-resource resources :javascript "bing/bong.js")
       (is (= true (has-resource? resources :javascript "bing/bong.js")))))))

(deftest remove-resource-test
  (testing "Removing a resource"
    (test-util/harness
     (fn [resources]
       (add-resource resources :javascript "zing/bong.js")
       (add-resource resources :javascript "zoing/bong.js")
       (remove-resource resources :javascript "zing/bong.js")
       (is (= true (has-resource? resources :javascript "zoing/bong.js")))
       (is (= false (has-resource? resources :javascript "zing/bong.js")))))))

(deftest matching-test
  (testing "Matching a predicate"
    (test-util/harness
     (fn [resources]
       (add-resource resources :javascript "ding/dong.js")
       (add-resource resources :stylesheet "foo/goo.css" {:location :header :junk "hello"})
       (add-resource resources :stylesheet "foo/boo.css" {:location :footer :junk "yolo"})
       (add-resource resources :javascript "ding/dung.js" {:location :header :junk "hello"})
       (is (= 2 (count (matching resources #(= "hello" (:junk %))))))
       (is (= 1 (count (matching resources #(= "yolo" (:junk %))))))
       (is (= 0 (count (matching resources #(= "LOLZ" (:junk %))))))
       (is (= 2 (count (matching resources #(= :header (:location %))))))
       (is (= 2 (count (matching resources #(= :footer (:location %))))))))))

;;; TODO: i can haz moar tests?
