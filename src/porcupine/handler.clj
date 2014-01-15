(ns porcupine.handler
  (:require [porcupine.core :as porcupine]
            [porcupine.helpers :as helpers]
            [porcupine.transforms :as transforms]))


(defn curry-helper
  [helper resources]
  (partial helper resources))

(defn curry-helpers
  [helpers resources]
  (into {} (map #(vector (first %) (curry-helper (second %) resources)) (seq helpers))))

(defn transform!
  [resources]
  (let [input (-> @resources :resources)
        transformed (-> input
                        (transforms/identity))]
    (swap! resources assoc :output transformed)))

(defn wrap-page-resources
  [handler & [opts]]
  (fn [request]
    (let [page-resources (porcupine/fresh-resource-collection opts)
          curried-helpers (curry-helpers (helpers/all-helpers) page-resources)
          response (handler (merge (assoc request :page-resources page-resources) curried-helpers))]
      (if-let [body (:body response)]
        (do
          (transform! page-resources) ; modifies resources atom - be warned!
          (let [new-response (assoc response :body (porcupine/replace-tokens page-resources body))]
            new-response))
        response))))

(defn foo
  []
  (let [resources (porcupine/fresh-resource-collection)
        help (curry-helpers (helpers/all-helpers) resources)]
    (-> help :add-resource (apply [:javascript "arse"]))
    (-> help :add-resource (apply [:stylesheet "bandit"]))
    (transform! resources)
    @resources))

(comment (foo))
