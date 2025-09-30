(ns launchpad-overtone.core
  (:require [launchpad-overtone.pad :as pad]))

(comment
  (use 'overtone.core)
  (boot-external-server)
  
  (pad/alternate 1 2 :color/magenta :color/blue)
  
  )
