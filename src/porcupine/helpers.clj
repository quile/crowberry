(ns porcupine.helpers
  (:require [porcupine.core :as porcupine]))

(defn token-handler
  [handler]
  (let [token (str (java.util.UUID/randomUUID))]
    (porcupine/add-token-handler token handler)
    (porcupine/token-format token)))

(defn all-resources [] (token-handler porcupine/->all-html))
(defn javascript-resources [] (token-handler porcupine/->javascript-html))
(defn stylesheet-resources [] (token-handler porcupine/->stylesheet-html))
(defn header-resources [] (token-handler porcupine/->header-html))
(defn footer-resources [] (token-handler porcupine/->footer-html))

(defn all-helpers
  "Merge this in before calling render and these helpers will be
   available during the render phase.  They will inject the appropriate
   resources into the results HTML *after* the HTML has been fully
   rendered."
  []
  {:add-resource (fn [& args] (apply porcupine/add-resource args) "<!-- added -->")
   :javascript-resources javascript-resources
   :stylesheet-resources stylesheet-resources
   :header-resources header-resources
   :footer-resources footer-resources
   :all-resources all-resources})
