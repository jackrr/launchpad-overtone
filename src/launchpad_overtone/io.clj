(ns launchpad-overtone.io
  (:use overtone.core)
  (:require [overtone.midi :as midi])
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

(defonce source (-> (midi/midi-sources) last with-transmitter))
(defonce sink (-> (midi/midi-sinks) last with-receiver))

(defn send-illumination-signal
  "Send illumination signal to cell.
  cell-id  -- integer address of button/pad
  channel  -- channel corresponding to static/flash/pulse
  color-id -- integer mapping for desired color"
  [cell-id channel color-id]
  (midi/midi-control sink cell-id color-id channel))

(defn send-sysex-msg [ints]
  (let [msg (SysexMessage.)]
    (.setMessage msg (byte-array ints) (count ints))
    (midi/midi-send-msg (:receiver sink) msg -1)))

(defn subscribe
  "Register a callback for midi events from pad"
  [cb]
  (midi/midi-handle-events source cb))

(defn subscribe-sysex
  "Register a callback for sysex events from pad"
  [cb]
  (midi/midi-handle-events source identity cb))


