(ns porcupine.test-util
  (:require [porcupine.core :as core]))

(defn harness
  [f]
  (binding [core/*page-resources* (core/fresh-resource-collection)]
    (f)))
