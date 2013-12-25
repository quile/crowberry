(ns porcupine.transforms
  (:import ro.isdc.wro.model.resource.Resource
           ro.isdc.wro.extensions.processor.js.UglifyJsProcessor
           ro.isdc.wro.extensions.processor.js.JsLintProcessor
           ro.isdc.wro.model.resource.processor.impl.js.ConsoleStripperProcessor
           java.io.StringReader
           java.io.StringWriter))

(defn transform-all
  [f resources]
  (let [out (map f resources)]
    (into [] (map (fn [r o] (assoc r :contents o)) resources out))))

(defn fake-resource
  [resource]
  (let [ty (get-in resource [:opts :type])]
    (cond
      (= ty :javascript) "foo.js"
      (= ty :stylesheet) "bar.css"
      (= ty :icon)       "baz.ico"
      :else              "quux.js")))

(defn process
  [resource processor]
  (let [writer (StringWriter.)]
    (.process processor
              (Resource/create (fake-resource resource))
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
(def jslint-processor (JsLintProcessor.))
(def console-stripper-processor (ConsoleStripperProcessor.))

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

(defn console-strip-resource
  [resource]
  (process resource console-stripper-processor))

(defn console-stripper
  "Strips console.* from JS"
  [resources]
  (transform-all console-strip-resource resources))
