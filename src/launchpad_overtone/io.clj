(ns launchpad-overtone.io
  (:use overtone.core)
  (:require [overtone.midi :as midi]
            [mount.core :refer [defstate] :as mount])
  (:import (javax.sound.midi MidiDevice SysexMessage)))

;; https://userguides.novationmusic.com/hc/en-gb/articles/24001475492498-Controlling-the-Launchpad-X-surface

(defn- with-receiver
  "Add a midi receiver to the sink device info. This is a connection
   from which the MIDI device will receive MIDI data"
  [sink-info]
  (let [^MidiDevice dev (:device sink-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (assoc sink-info :receiver (.getReceiver dev))))

(defn- with-transmitter
  "Add a midi transmitter to the source info. This is a connection from
   which the MIDI device will transmit MIDI data."
  [source-info]
  (let [^MidiDevice dev (:device source-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (assoc source-info :transmitter (.getTransmitter dev))))

(def ^:private midi-handlers
  "Registered callbacks for midi events"
  (atom {}))

(def ^:private sysex-handlers
  "Registered callbacks for sysex events"
  (atom {}))

(defn- handle-midi-event
  "Process incoming midi event to subscribers"
  [ev]
  (doall (for [s (vals @midi-handlers)]
           (s ev))))

(defn- handle-sysex-event
  "Process incoming midi event to subscribers"
  [ev]
  (doall (for [s (vals @sysex-handlers)]
           (s ev))))

(def ^:private last-receiver (atom nil))

(defstate ^:private launchpad
  :start (let [source (-> (midi/midi-sources) last with-transmitter)
               sink   (-> (midi/midi-sinks) last with-receiver)]
           (reset! last-receiver
                   (midi/midi-handle-events source
                                             handle-midi-event
                                             handle-sysex-event))
           {:source source
            :sink   sink})
  :stop (when-let [receiver @last-receiver]
          (print "Deregistering midi callbacks.")
          (.close receiver)))

(defn send-illumination-signal
  "Send illumination signal to cell.
  cell-id  -- integer address of button/pad
  channel  -- channel corresponding to static/flash/pulse
  color-id -- integer mapping for desired color"
  [cell-id channel color-id]
  (midi/midi-control (:sink launchpad) cell-id color-id channel))

(defn send-sysex-msg [ints]
  (let [msg (SysexMessage.)]
    (.setMessage msg (byte-array ints) (count ints))
    (midi/midi-send-msg (-> launchpad :sink :receiver) msg -1)))

(defn subscribe-midi
  "Register a callback for midi events from pad"
  [name cb]
  (swap! midi-handlers assoc name cb))

(defn unsubscribe-midi
  [name]
  (swap! midi-handlers dissoc name))

(defn subscribe-sysex
  "Register a callback for sysex events from pad"
  [name cb]
  (swap! sysex-handlers assoc name cb))

(defn unsubscribe-sysex
  [name]
  (swap! sysex-handlers dissoc name))

(defn init []
  (mount/start))

(defn reload []
  (mount/stop)
  (mount/start))

(comment
  (:source launchpad)
  
  (init)
  (reload)
  )
