(ns porcupine.test-util
  (:require [porcupine.core :as core]))

(defn harness
  [f]
  (let [resources (core/fresh-resource-collection "resources")]
    (f resources)))
