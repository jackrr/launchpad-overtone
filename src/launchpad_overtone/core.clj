(ns launchpad-overtone.core)

(use 'overtone.core)
(use 'overtone.live)

;; (require '[overtone.midi :as midi])
(require '[overtone.studio.midi :as midis])
(require '[overtone.inst.drum :as drum])

;; (midi/midi-sources)
;; (midi/midi-find-device "Launchpad")
(def device (midis/midi-find-connected-devices "Launchpad"))
(def receiver (midis/midi-find-connected-receivers "Launchpad"))


(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(comment
  (do
    (boot-external-server)
    (init))
     )
