(ns porcupine.transforms
  (:import ro.isdc.wro.model.resource.Resource
           ro.isdc.wro.extensions.processor.js.UglifyJsProcessor
           ro.isdc.wro.extensions.processor.js.JsLintProcessor
           ro.isdc.wro.extensions.processor.js.JsHintProcessor
           java.io.StringReader
           java.io.StringWriter))

(defn transform-all
  [f resources]
  (let [out (map f resources)]
    (into [] (map (fn [r o] (assoc r :contents o)) resources out))))

(defn process
  [resource processor]
  (let [writer (StringWriter.)]
    (.process processor
              (Resource.)
              (StringReader. (:contents resource))
              writer)
    (str (.getBuffer writer))))

;; functions that transform resources in different ways.
;; these functions must take a vector of resources
;; and return a vector of transformed resources

(defn identity
  "Returns its input."
  [resources] resources)


;; TODO - these need a protocol

(def uglify-js-processor (UglifyJsProcessor.))
(def jslint-processor (proxy [JsLintProcessor] []
                       (onLinterException [e r] (println (.getErrors e) r))))
(def jshint-processor (proxy [JsHintProcessor] []
                       (onLinterException [e r] (println (.getErrors e) r))))

(defn uglify-resource
  [resource]
  (process resource uglify-js-processor))

(defn uglify
  "Uglifies its input."
  [resources]
  (transform-all uglify-resource resources))

(defn jslint-resource
  [resource]
  (process resource jslint-processor))

(defn jslint
  "Runs jslint on its input."
  [resources]
  (transform-all jslint-resource resources))

(defn jshint-resource
  [resource]
  (process resource jshint-processor))

(defn jshint
  "Runs jshint on its input."
  [resources]
  (transform-all jshint-resource resources))

;; ... other processors/compilers here