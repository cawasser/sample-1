(ns sample-1.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sample-1.core-test]))

(doo-tests 'sample-1.core-test)

