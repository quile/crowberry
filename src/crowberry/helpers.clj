(ns crowberry.helpers
  (:require [crowberry.core :as crowberry]))

(defn token-format
  [token]
  (str "<!!!" token "!!!>"))

(defn token-handler
  [handler]
  (let [token (str (java.util.UUID/randomUUID))]
    (crowberry/add-token-handler token handler)
    (token-format token)))

(defn all-resources [] (token-handler crowberry/->all-html))
(defn javascript-resources [] (token-handler crowberry/->javascript-html))
(defn stylesheet-resources [] (token-handler crowberry/->stylesheet-html))
(defn header-resources [] (token-handler crowberry/->header-html))
(defn footer-resources [] (token-handler crowberry/->footer-html))

(defn all-helpers
  "Merge this in before calling render and these helpers will be
   available during the render phase.  They will inject the appropriate
   resources into the results HTML *after* the HTML has been fully
   rendered."
  []
  {:javascript-resources javascript-resources
   :stylesheet-resources stylesheet-resources
   :header-resources header-resources
   :footer-resources footer-resources
   :all-resources all-resources})
