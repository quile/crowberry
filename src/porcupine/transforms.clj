(ns porcupine.transforms
  (:require [porcupine.core :as core])
  (:import ro.isdc.wro.model.resource.Resource
           ro.isdc.wro.extensions.processor.js.UglifyJsProcessor
           ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor
           ro.isdc.wro.extensions.processor.js.JsLintProcessor
           ro.isdc.wro.extensions.processor.js.JsHintProcessor
           ro.isdc.wro.model.resource.processor.impl.js.ConsoleStripperProcessor
           java.io.StringReader
           java.io.StringWriter))

(defn transform-all
  [f resources]
  (let [out (map f resources)]
    (into [] (map (fn [r o] (assoc r :content o)) resources out))))

(defn process
  [resource processor]
  (let [writer (StringWriter.)]
    (.process processor
              (Resource/create (:path resource))
              (StringReader. (core/resource-content resource))
              writer)
    (str (.getBuffer writer))))

;; functions that transform resources in different ways.
;; these functions must take a vector of resources
;; and return a vector of transformed resources

(defn ident
  "Returns its input."
  [resources] resources)

;; TODO - these need a protocol

(def uglify-js-processor (UglifyJsProcessor.))
(def beautify-js-processor (BeautifyJsProcessor.))
(def jslint-processor (proxy [JsLintProcessor] []
                       (onLinterException [e r] (println (.getErrors e) r))))
(def jshint-processor (proxy [JsHintProcessor] []
                       (onLinterException [e r] (println (.getErrors e) r))))
(def console-stripper-processor (ConsoleStripperProcessor.))

(defn uglify-resource
  [resource]
  (process resource uglify-js-processor))

(defn uglify
  "Uglifies its input."
  [resources]
  (transform-all uglify-resource resources))

(defn beautify-resource
  [resource]
  (process resource beautify-js-processor))

(defn beautify
  "Beautifies its input."
  [resources]
  (transform-all beautify-resource resources))

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

(defn console-strip-resource
  [resource]
  (process resource console-stripper-processor))

(defn console-stripper
  "Strips console.* from JS"
  [resources]
  (transform-all console-strip-resource resources))

(defn concatenator
  "Concatenates files together"
  [resources]
  (let [ty (-> resources first :opts :type)
        _ (println "concatenating to " ty)
        content (apply str (map core/resource-content resources))
        _ (print "content is " content)
        result (core/construct-resource ty nil {})]
    (assoc result :content content)))