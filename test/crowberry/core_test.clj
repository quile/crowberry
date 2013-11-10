(ns crowberry.core-test
  (:require [clojure.test :refer :all]
            [crowberry.core :refer :all]
            [crowberry.test-util :as test-util]))

(comment (deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1)))))

(deftest has-resource-test
  (testing "Checking for existing resource in resource collection"
    (test-util/harness
     (fn []
       (add-resource :javascript "bing/bung.js")
       (is (= true (has-resource? :javascript "bing/bung.js")))
       (is (= false (has-resource? :javascript "jubjub.js")))))))

(deftest add-resource-test
  (testing "Adding a resource"
    (test-util/harness
     (fn []
       (add-resource :javascript "bing/bong.js")
       (is (= true (has-resource? :javascript "bing/bong.js")))))))

(deftest remove-resource-test
  (testing "Removing a resource"
    (test-util/harness
     (fn []
       (add-resource :javascript "zing/bong.js")
       (add-resource :javascript "zoing/bong.js")
       (remove-resource :javascript "zing/bong.js")
       (is (= true (has-resource? :javascript "zoing/bong.js")))
       (is (= false (has-resource? :javascript "zing/bong.js")))))))

(deftest matching-test
  (testing "Matching a predicate"
    (test-util/harness
     (fn []
       (add-resource :javascript "ding/dong.js")
       (add-resource :stylesheet "foo/goo.css" {:location :header :junk "hello"})
       (add-resource :stylesheet "foo/boo.css" {:location :footer :junk "yolo"})
       (add-resource :javascript "ding/dung.js" {:location :header :junk "hello"})
       (is (= 2 (count (matching #(= "hello" (:junk %))))))
       (is (= 1 (count (matching #(= "yolo" (:junk %))))))
       (is (= 0 (count (matching #(= "LOLZ" (:junk %))))))
       (is (= 2 (count (matching #(= :header (:location %))))))
       (is (= 2 (count (matching #(= :footer (:location %))))))))))

;;; TODO: i can haz moar tests?
