(ns launchpad-overtone.core)

(use 'overtone.core)
;; (use 'overtone.midi)
;; (use 'overtone.live)

(require '[overtone.midi :as midi])
;; (require '[overtone.inst.drum :as drum])

(def illumination-kinds
  {:led/static 0
   ;; NOTE: Seemingly cannot transition from pulse or static to flash
   :led/flash  1
   :led/pulse  2})

;; https://github.com/josephwilk/overtone.device.launchpad/blob/master/src/launchpad/device.clj
;; https://userguides.novationmusic.com/hc/en-gb/articles/24001475492498-Controlling-the-Launchpad-X-surface

(defn init-midi-devices []
  ;; (let [
  ;;       ;; pad-in (midi/midi-in "Launchpad") 
  ;;       ;; pad-out (midi/midi-out "Launchpad")
  ;;       ]
  ;;   {:in pad-in :out pad-out})
  {:in (-> (midi/midi-sources) last with-transmitter)
   :out (-> (midi/midi-sinks) last with-receiver)}
  )

(defn- with-receiver
  "Add a midi receiver to the sink device info. This is a connection
   from which the MIDI device will receive MIDI data"
  [sink-info]
  (println sink-info)
  (let [^javax.sound.midi.MidiDevice dev (:device sink-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (assoc sink-info :receiver (.getReceiver dev))))

(defn- with-transmitter
  "Add a midi transmitter to the source info. This is a connection from
   which the MIDI device will transmit MIDI data."
  [source-info]
  (let [^javax.sound.midi.MidiDevice dev (:device source-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (assoc source-info :transmitter (.getTransmitter dev))))


(defn illuminate
  "Trigger LED of kind on device at slot with given color"
  [device button kind color]
  (let [msg (javax.sound.midi.ShortMessage.)]
    (.setMessage msg javax.sound.midi.ShortMessage/CONTROL_CHANGE (kind illumination-kinds) button color)
    (midi/midi-send-msg (:receiver device) msg 0)))

(comment

  (boot-external-server)

  (let [{:keys [in out]} (init-midi-devices)]
    (midi/midi-handle-events in
                           (fn [msg] (println "GOT MSG:" msg))
                           (fn [msg]
                             (println "GOT SYSEX MSG:" msg)
                             (-> msg :data vec println)))
    
    )

  (let [{:keys [in out]} (init-midi-devices)]
    (illuminate out 56 :led/static 0)
    )

  

  

  (let [msg (javax.sound.midi.SysexMessage.)]
    ;; This might be device inquiry message
    ;; (.setMessage msg (byte-array [240 126 127 6 1 247]) 6)
    ;; This is programmer/live mode layout
    (.setMessage msg (byte-array [240 0 32 41 2 12 0 127 247]) 9)
    ;; This is note mode layout
    ;; (.setMessage msg (byte-array [240 0 32 41 2 12 0 1 247]) 9)
    (midi/midi-send-msg (:receiver lpout) msg -1)
    )


  )
