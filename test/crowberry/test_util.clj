(ns lumberg.test-util
  (:require [lumberg.core :as core]))

(defn harness
  [f]
  (binding [core/*page-resources* (core/fresh-resource-collection)]
    (f)))
