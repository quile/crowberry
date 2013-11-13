(ns porcupine.core
  (:require [flatland.ordered.set :as ordered]
            [clojure.string :as string]))

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
  [resources source]
  (let [token-map (-> @resources :tokens seq)
        resolutions (map
                     #(vector (first %)
                              (apply (second %) [resources])) token-map)]
    (reduce
     #(string/replace %1 (str (token-format (first %2))) (second %2))
     source
     resolutions)))

(defn add-resource
  "Add a resource to the resources atom."
  ([resources resource-type resource] (add-resource resources resource-type resource {}))
  ([resources resource-type resource opts]
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
     (swap! resources
      (fn thingy [s t p]
        (update-in s [t] conj p)) mapped-type resource-record))))

(defn remove-resource
  "Remove a resource that's been added already. This can be
   useful if *things change* during rendering."
  [resources resource-type path]
  (let [rem (fn [c x]
              (remove #(= (:path %) x) c))]
    (swap! resources
     (fn [s t p]
       (update-in s [t] rem p)) resource-type path)))

(defn has-resource?
  "Check if a given resource has been included yet."
  [resources resource-type path]
  (let [resources (get @resources resource-type)
        found (filter #(= path (:path %)) resources)]
    (> (count found) 0)))

(defn add-token-handler
  "Adds a token handler to the atom - a token handler is a function
   that will be invoked later to resolve the injection of page
   resources into the page."
  [resources token handler]
  (swap! resources assoc-in [:tokens token] handler))

(defn matching
  "Returns all matching resources in the resources atom."
  [resources predicate]
  (let [all (concat (:stylesheet @resources)
                    (:javascript @resources)
                    (:icon @resources))]
    (filter #(predicate (:opts %)) all)))

(defn ->html
  "Given a predicate, renders all matching resources
   as HTML tags."
  ([resources] (->html resources (fn [x] (constantly true))))
  ([resources predicate]
   (let [filtered (matching resources predicate)
         tags (map tag filtered)]
     (string/join "\n" tags))))

(defn ->all-html        [resources] (->html resources))
(defn ->header-html     [resources] (->html resources (fn [x] (= :header (:location x)))))
(defn ->footer-html     [resources] (->html resources (fn [x] (= :footer (:location x)))))
(defn ->javascript-html [resources] (->html resources (fn [x] (= :javascript (:type x)))))
(defn ->stylesheet-html [resources] (->html resources (fn [x] (= :stylesheet (:type x)))))

(comment
  (let [resources (core/fresh-resource-collection)]
    (core/add-resource resources :javascript "foobar")
    (core/add-resource resources :stylesheet "barbaz")
    (let [bonk (str "hey there " (helpers/javascript-resources) " bastards")]
      (core/replace-tokens resources bonk))))
