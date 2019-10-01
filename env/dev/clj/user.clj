(ns user
  (:require
    [sample-1.config :refer [env]]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    ;[sample-1.figwheel :refer [start-fw stop-fw cljs]]
    [sample-1.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'sample-1.core/repl-server))

(defn stop []
  (mount/stop-except #'sample-1.core/repl-server))

(defn restart []
  (stop)
  (start))


