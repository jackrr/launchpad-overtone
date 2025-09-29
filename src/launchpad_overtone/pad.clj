(ns launchpad-overtone.pad
  (:require [launchpad-overtone.io :as io]))

(def modes
  {:mode/programmer [240 0 32 41 2 12 0 127 247]
   :mode/note [240 0 32 41 2 12 0 1 247]})

(defn set-mode [mode]
  (-> mode modes io/send-sysex-msg))

(def illumination-kinds
  {:led/static 0
   ;; NOTE: Seemingly cannot transition from pulse or static to flash
   :led/flash  1
   :led/pulse  2})

(def colors
  ;; Note: 127 colors are available, more here:
  ;; https://userguides.novationmusic.com/hc/en-gb/articles/24001475492498-Controlling-the-Launchpad-X-surface
  {:color/off 0
   :color/grey 1
   :color/red 72
   :color/teal 37
   :color/purple 80
   :color/vomit 74
   :color/pink 94
   :color/magenta 53
   :color/violet 81
   :color/green 21
   :color/forest-green 64
   :color/blue 79
   :color/orange 84
   :color/brown 127
   :color/white 119
   :color/yellow 13})

(def ^:private cells
  (reduce (fn [cells y]
            (merge cells
                   (reduce (fn [cells x]
                             (print cells)
                             (assoc cells
                                    (+ (* (- 7 y) 10) x) ;; 00-77, TL is origin
                                    (+ (* (+ 1 y) 10) (+ 1 x)))) ;; 11 - 88, BL is origin
                           {}
                           (range 8))))
          {}
          (range 8)))

(defn- cell-id [x y]
  (when (or (> x 7)
            (> y 7)
            (< x 0)
            (< y 0))
    (throw (Exception. (str "Cell id " x "x" y " is out of bounds"))))
  (get cells (+ x (* 10 y))))

(defn illuminate
  ([x y color]
   (illuminate x y color :led/static))
  ([x y color kind]
   (io/send-illumination-signal (cell-id x y)
                                (kind illumination-kinds)
                                (color colors))))

(defn clear
  "Send control signal for off color to all cells"
  []
  (let [channel (:led/static illumination-kinds)
        color (:color/off colors)]
    (doall
     (for [cell-id (vals cells)]
       (io/send-illumination-signal cell-id channel color)))))

;; TODO: expose API for subscribing to button presses

(comment
  (set-mode :mode/programmer)

  (illuminate 0 5 :color/off)

  (clear)
  )
