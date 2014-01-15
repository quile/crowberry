(ns porcupine.cache
  (:import java.util.Date))

;;; TODO - fix this!
(defprotocol Cache
  (cget [this key])
  (has? [this key])
  (purge [this])
  (expired? [this key])
  (cset [this key val] [this key val opts])
  (hide [this val] [this val opts]))

(defrecord MapCache [a]
  Cache
  (cget [c key]
    (when-not (expired? c key)
      (-> @a :cache (get key))))
  (expired? [c key]
    (if-let [exp (-> @a :timeout (get key))]
      (let [now (Date.)
            seconds (.getTime now)]
        (> seconds exp))
      false))
  (has? [this key]
    (contains? (-> @a :cache) (get key)))
  (purge [this]
    (reset! a {:cache {} :timeout {}}))
  (cset [c key value]
    (cset c key value {}))
  (cset [c key value opts]
    (swap! a assoc-in [:cache key] value)
    (if-let [timeout (:timeout opts)]
      (let [now (Date.)
            seconds (.getTime now)
            expiration (+ seconds (* 1000 timeout))]
        (swap! a assoc-in [:timeout key] expiration))
      (swap! a assoc-in [:timeout key] nil)))
  (hide [c value]
    (hide c value {}))
  (hide [c value opts]
    (let [key (java.util.UUID/randomUUID)]
      (cset c key value opts)
      key)))

(defn map-cache []
  (MapCache. (atom {})))
