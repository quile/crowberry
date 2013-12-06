(ns porcupine.transforms
  (:import ro.isdc.wro.model.resource.Resource
           ro.isdc.wro.extensions.processor.js.UglifyJsProcessor
           java.io.StringReader
           java.io.StringWriter))

;; functions that transform resources in different ways.
;; these functions must take a vector of resources
;; and return a vector of transformed resources

(defn identity
  "Returns its input."
  [& resources] resources)

;; TODO - these need a protocol

(defn uglify-js-processor []
  (UglifyJsProcessor.))


(defn uglify-resource
  [resource]
  (let [processor (uglify-js-processor)
        writer (StringWriter.)]
    (.process processor
              (Resource.)
              (StringReader. (:contents resource))
              writer)
    (str (.getBuffer writer))))

(defn uglify
  "Uglifies its input."
  [& resources]
  (let [out (map uglify-resource resources)]
    (into [] (for [o out r resources]
               (assoc r (:contents o))))))
