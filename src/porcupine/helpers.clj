(ns porcupine.helpers
  (:require [porcupine.core :as porcupine]))

(defn token-handler
  "Creates a token handler that will resolve the mapping
   between a generated token and the actual resources that
   need to be injected into the template."
  [resources handler]
  (let [token (str (java.util.UUID/randomUUID))]
    (porcupine/add-token-handler resources token handler)
    (porcupine/token-format token)))

(defn all-resources [resources] (token-handler resources porcupine/->all-html))
(defn javascript-resources [resources] (token-handler resources porcupine/->javascript-html))
(defn stylesheet-resources [resources] (token-handler resources porcupine/->stylesheet-html))
(defn header-resources [resources] (token-handler resources porcupine/->header-html))
(defn footer-resources [resources] (token-handler resources porcupine/->footer-html))

(defn all-helpers
  "Merge this in before calling render and these helpers will be
   available during the render phase.  They will inject the appropriate
   resources into the results HTML *after* the HTML has been fully
   rendered."
  []
  {:add-resource (fn [& args] (apply porcupine/add-resource args) "") ;; TODO: make output configurable
   :javascript-resources javascript-resources
   :stylesheet-resources stylesheet-resources
   :header-resources header-resources
   :footer-resources footer-resources
   :all-resources all-resources})
