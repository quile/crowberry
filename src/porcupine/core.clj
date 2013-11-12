(ns porcupine.core
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
         "src=\"" (:path p) "\"></script>")))

;;; This represents a stylesheet that is to be pulled into the page
(defrecord StylesheetResource [path opts]
  HTML
  (tag [p]
    (str "<link rel=\"stylesheet\" href=\"" (:path p) "\">")))

;;; This represents a favicon
(defrecord ShortcutIconResource [path opts]
  HTML
  (tag [p]
       (str "<link rel=\"icon\" href=\"" (:path p) "\">")))

(defn fresh-resource-collection
  "Returns a new empty collection of page resources"
  []
  (atom {:javascript (ordered/ordered-set)
         :stylesheet (ordered/ordered-set)
         :icon (ordered/ordered-set)
         :tokens {}}))

(defn token-format
  "This is how resources are injected into HTML, to be replaced after rendering."
  [token]
  (str "[!!!" token "!!!]"))

(defn replace-tokens
  "Replaces all rendered tokens with resources."
  [source]
  (let [token-map (-> @*page-resources* :tokens seq)
        resolutions (map #(vector (first %) (apply (second %) [])) token-map)]
    (reduce
     #(string/replace %1 (str (token-format (first %2))) (second %2))
     source
     resolutions)))

(defn wrap-page-resources
  "Ring handler middleware to enable page resource handling"
  [handler]
  (fn [request]
    (binding [*page-resources* (fresh-resource-collection)]
      (let [response (handler request)]
        (if-let [body (:body response)]
          (let [new-response (assoc response :body (replace-tokens body))]
            new-response)
          response)))))

(defn add-resource
  "Add a resource to the *page-resources* atom."
  ([resource-type resource] (add-resource resource-type resource {}))
  ([resource-type resource opts]
   (let [mapped-type (if (or (= :shortcut resource-type)
                             (= :shortcut-icon resource-type))
                       :icon
                       resource-type)
         defaults {:location (if (= :javascript mapped-type)
                               :footer
                               :header)}
         mopts (merge defaults (assoc opts :type mapped-type))
         resource-record (condp = mapped-type
                           :javascript (ScriptResource. resource mopts)
                           :stylesheet (StylesheetResource. resource mopts)
                           :icon (ShortcutIconResource. resource mopts))]
     (swap! *page-resources*
      (fn thingy [s t p]
        (update-in s [t] conj p)) mapped-type resource-record))))

(defn remove-resource
  "Remove a resource that's been added already. This can be
   useful if *things change* during rendering."
  [resource-type path]
  (let [rem (fn [c x]
              (remove #(= (:path %) x) c))]
    (swap! *page-resources*
     (fn [s t p]
       (update-in s [t] rem p)) resource-type path)))

(defn has-resource?
  "Check if a given resource has been included yet."
  [resource-type path]
  (let [resources (get @*page-resources* resource-type)
        found (filter #(= path (:path %)) resources)]
    (> (count found) 0)))

(defn add-token-handler
  "Adds a token handler to the atom - a token handler is a function
   that will be invoked later to resolve the injection of page
   resources into the page."
  [token handler]
  (swap! *page-resources* assoc-in [:tokens token] handler))

(defn matching
  "Returns all matching resources in the *page-resources* atom."
  [predicate]
  (let [all (concat (:stylesheet @*page-resources*)
                    (:javascript @*page-resources*)
                    (:icon @*page-resources*))]
    (filter #(predicate (:opts %)) all)))

(defn ->html
  "Given a predicate, renders all matching resources
   as HTML tags."
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

(comment
  (binding [core/*page-resources* (core/fresh-resource-collection)]
    (core/add-resource :javascript "foobar")
    (core/add-resource :stylesheet "barbaz")
    (let [bonk (str "hey there " (helpers/javascript-resources) " bastards")]
      (core/replace-tokens bonk))))
