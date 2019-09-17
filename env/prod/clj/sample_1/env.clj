(ns sample-1.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sample-1 started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sample-1 has shut down successfully]=-"))
   :middleware identity})
