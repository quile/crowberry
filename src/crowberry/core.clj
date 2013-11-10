(ns crowberry.core
  (:require [flatland.ordered.set :as ordered]
            [clojure.string :as string]))

(def ^:dynamic *page-resources* nil)

(def resource-cache (ref {}))

(defprotocol HTML
  "Something that can render itself as a tag"
  (tag [this]))

;;; This represents a javascript that is to be pulled into the page
(defrecord ScriptResource [path opts]
  HTML
  (tag [p]
    (str "<script type=\"text/javascript\" "
         "src=\"" (:path p) "\">")))

;;; This represents a stylesheet that is to be pulled into the page
(defrecord StylesheetResource [path opts]
  HTML
  (tag [p]
    (str "<link rel=\"stylesheet\" href=\"" (:path p) "\">")))

(defn fresh-resource-collection
  "Returns a new empty collection of page resources"
  []
  (atom {:javascript (ordered/ordered-set)
         :stylesheet (ordered/ordered-set)
         :tokens {}}))

(defn replace-tokens
  [source]
  (let [token-map (-> @*page-resources* :tokens seq)
        resolutions (map #(vector (first %) (apply (second %) [])) token-map)]
    (reduce
     #(string/replace %1 (str "<!!!" (first %2) "!!!>") (second %2))
     source
     resolutions)))

(defn wrap-page-resources
  "Ring handler middleware to enable page resource handling"
  [handler]
  (fn [request]
    (binding [*page-resources* (fresh-resource-collection)]
      (let [response (handler request)]
        (if-let [body (:body response)]
          (assoc response :body (replace-tokens body))
          response)))))

(defn add-resource
  ([resource-type resource] (add-resource resource-type resource {}))
  ([resource-type resource opts]
   (let [defaults {:location (if (= :javascript resource-type)
                               :footer
                               :header)}
         mopts (merge defaults (assoc opts :type resource-type))
         resource-record (if (= resource-type :javascript)
                        (ScriptResource. resource mopts)
                        (StylesheetResource. resource mopts))]
     (swap! *page-resources*
      (fn thingy [s t p]
        (update-in s [t] conj p)) resource-type resource-record))))

(defn remove-resource
  [resource-type path]
  (let [rem (fn [c x]
              (remove #(= (:path %) x) c))]
    (swap! *page-resources*
     (fn [s t p]
       (update-in s [t] rem p)) resource-type path)))

(defn has-resource?
  [resource-type path]
  (let [resources (get @*page-resources* resource-type)
        found (filter #(= path (:path %)) resources)]
    (> (count found) 0)))

(defn add-token-handler
  [token handler]
  (swap! *page-resources* assoc-in [:tokens token] handler))

(defn matching
  [predicate]
  (let [all (concat (:stylesheet @*page-resources*) (:javascript @*page-resources*))]
    (filter #(predicate (:opts %)) all)))

(defn ->html
  ([] (->html (fn [x] (constantly true))))
  ([predicate]
   (let [filtered (matching predicate)
         tags (map tag filtered)]
     (string/join "\n" tags))))

(defn ->all-html        [] (->html))
(defn ->header-html     [] (->html (fn [x] (= :header (:location x)))))
(defn ->footer-html     [] (->html (fn [x] (= :footer (:location x)))))
(defn ->javascript-html [] (->html (fn [x] (= :javascript (:type x)))))
(defn ->stylesheet-html [] (->html (fn [x] (= :stylesheet (:type x)))))

(defn foo []
  (binding [*page-resources* (fresh-resource-collection)]
    (add-resource :javascript "banana/foo")
    (add-resource :javascript "zing/foo")
    (add-resource :javascript "bonk/foo")
    (add-resource :javascript "zing/foo" {:location :footer})
    (add-resource :stylesheet "banana/foo")
    (add-resource :stylesheet "zingbag/foo")
    (remove-resource :stylesheet "banana/foo")
    (remove-resource :stylesheet "bonk/foo")
    (remove-resource :javascript "bonk/foo")
    (->footer-html)))

(foo)

(comment
  (binding [core/*page-resources* (core/fresh-resource-collection)]
    (core/add-resource :javascript "foobar")
    (core/add-resource :stylesheet "barbaz")
    (let [bonk (str "hey there " (helpers/javascript-resources) " bastards")]
      (core/replace-tokens bonk))))
