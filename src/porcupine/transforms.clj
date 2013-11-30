(ns porcupine.transforms)

;; functions that transform resources in different ways.
;; these functions must take a vector of resources
;; and return a vector of transformed resources

(defn identity
  "Returns its input."
  [& resources] resources)
