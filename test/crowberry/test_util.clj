(ns crowberry.test-util
  (:require [crowberry.core :as core]))

(defn harness
  [f]
  (binding [core/*page-resources* (core/fresh-resource-collection)]
    (f)))
