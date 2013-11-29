(ns porcupine.handler
  (:require [porcupine.core :as porcupine]
            [porcupine.helpers :as helpers]))


(defn curry-helper
  [helper resources]
  (partial helper resources))

(defn curry-helpers
  [helpers resources]
  (into {} (map #(vector (first %) (curry-helper (second %) resources)) (seq helpers))))

(defn wrap-page-resources
  [handler]
  (fn [request]
    (let [page-resources (porcupine/fresh-resource-collection)
          curried-helpers (curry-helpers (helpers/all-helpers) page-resources)
          response (handler (merge (assoc request :page-resources page-resources) curried-helpers))]
      (if-let [body (:body response)]
          (let [new-response (assoc response :body (porcupine/replace-tokens page-resources body))]
            new-response)
          response))))

(defn foo
  []
  (let [resources (porcupine/fresh-resource-collection)
        help (curry-helpers (helpers/all-helpers) resources)]
    (-> help :add-resource (apply [:javascript "arse"]))
    (-> help :add-resource (apply [:stylesheet "bandit"]))
    @resources))

;;;(foo)
