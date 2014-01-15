(ns porcupine.core
  (:require [flatland.ordered.set :as ordered]
            [clojure.string :as string]
            [ring.util.response :as response])
  (:import java.net.URL
           java.util.Date))

(defn now [] (.getTime (Date.)))

(defn maybe-deref
  [thing]
  (if (instance? clojure.lang.IDeref thing) @thing thing))

(defn load-resource [path & [opts]]
  (try
    (let [url (URL. path)]
      (response/url-response url))
    (catch Exception e
      (response/resource-response path opts))))

(defn resource-content
  [resource]
  (slurp (:body resource)))

;; found this on stackoverflow:
(defn record-factory [recordname]
  (let [recordclass ^Class (resolve (symbol recordname))
        max-arg-count (apply max (map #(count (.getParameterTypes %))
                                      (.getConstructors recordclass)))
        args (map #(symbol (str "x" %)) (range (- max-arg-count 2)))]
    (eval `(fn [~@args] (new ~(symbol recordname) ~@args)))))

(defn new-resource [factory path opts]
  "Create a new resource record using the appropriate factory, and populate
   its content from the file system or over the interwebs."
  (if-let [response (load-resource path opts)]
    (factory path (delay (resource-content response)) opts)
    (factory path nil (assoc opts :error (format "No such resource: %s" path)))))

(defprotocol Resource
  "Something that can render itself as a tag"
  (tag [this])
  (contents [this]))

;;; This represents a javascript that is to be pulled into the page
(defrecord ScriptResource [path content opts]
  Resource
  (tag [p]
    (str "<script type=\"text/javascript\" "
         "src=\"" (:path p) "\"></script>"))
  (contents [p] (maybe-deref (:content p))))

(def script-factory (record-factory "ScriptResource"))
(defn new-script [path opts]
  (new-resource script-factory path opts))

;;; This represents a stylesheet that is to be pulled into the page
(defrecord StylesheetResource [path content opts]
  Resource
  (tag [p]
    (str "<link rel=\"stylesheet\" href=\"" (:path p) "\">"))
  (contents [p] (maybe-deref (:content p))))

(def stylesheet-factory (record-factory "StylesheetResource"))
(defn new-stylesheet [path opts]
  (new-resource stylesheet-factory path opts))

;;; This represents a favicon
(defrecord ShortcutIconResource [path content opts]
  Resource
  (tag [p]
       (str "<link rel=\"icon\" href=\"" (:path p) "\">"))
  (contents [p] (maybe-deref (:content p))))

(def shortcut-icon-factory (record-factory "ShortcutIconResource"))
(defn new-shortcut-icon [path opts]
  (new-resource shortcut-icon-factory path opts))

(def constructors {:javascript new-script
                   :stylesheet new-stylesheet
                   :icon new-shortcut-icon})

(defn fresh-resource-collection
  "Returns a new empty collection of page resources"
  ([] (fresh-resource-collection {}))
  ([opts]
    (atom {:resources (ordered/ordered-set)
           :tokens {}
           :opts opts})))

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

(defn construct-resource
  [resource-type path opts]
  (let [mapped-type (if (or (= :shortcut resource-type)
                            (= :shortcut-icon resource-type))
                      :icon
                      resource-type)
        defaults {:location (if (= :javascript mapped-type)
                              :footer
                              :header)}
        mopts (merge defaults (assoc opts :type mapped-type))]
    ((get constructors mapped-type) path mopts)))

(defn add-resource
  "Add a resource to the resources atom."
  ([resources resource-type path] (add-resource resources resource-type path {}))
  ([resources resource-type path opts]
  (swap! resources
   (fn thingy [s t p]
    (update-in s [t] conj p)) :resources (construct-resource resource-type path opts))))

(defn remove-resource
  "Remove a resource that's been added already. This can be
   useful if *things change* during rendering."
  [resources resource-type path]
  (let [rem (fn [c x]
              (remove #(and (= (-> % :opts :type) resource-type)
                            (= (:path %) x)) c))]
    (swap! resources
     (fn [s t p]
       (update-in s [t] rem p)) :resources path)))

(defn has-resource?
  "Check if a given resource has been included yet."
  [resources resource-type path]
  (let [resources (:resources @resources)
        found (filter #(and (= resource-type (-> % :opts :type))
                            (= path (:path %))) resources)]
    (> (count found) 0)))

(defn add-token-handler
  "Adds a token handler to the atom - a token handler is a function
   that will be invoked later to resolve the injection of page
   resources into the page."
  [resources token handler]
  (swap! resources assoc-in [:tokens token] handler))

(defn matching
  "Returns all matching resources in the resources atom.
   If there are any resources found under the key :output,
   they will be used, otherwise the original resources will
   be used untouched."
  [resources predicate]
  (let [all (if (empty? (:output @resources))
              (:resources @resources)
              (:output @resources))]
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
  (let [resources (fresh-resource-collection)]
    (add-resource resources :javascript "foobar")
    (add-resource resources :stylesheet "barbaz")
    (let [bonk (str "hey there " (helpers/javascript-resources) " fiddly bits")]
      (replace-tokens resources bonk))))
