(ns first-person-play-clj.core
  (:require [play-clj.core :refer :all]
            [play-clj.g3d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]))

(def ^:const velocity 5)
(def ^:const degrees-per-pixel 0.1)

(defn calc-move-strafe [{:keys [delta-time] :as screen} left?]
  (let [cam-dir (direction screen)
        cam-up (up screen)
        cam-pos (position screen)
        l? (if left? -1 1)
        tmp (vector-3 (x cam-dir) (y cam-dir) (z cam-dir))]
    (->
      tmp
      (.crs cam-up)
      (.nor)
      (.scl (float (* l? velocity delta-time)))
      (.add cam-pos))))


(defn calc-move-x [{:keys [delta-time] :as screen} forward?]
  (let [cam-dir (direction screen)
        cam-pos (position screen)
        f? (if forward? 1 -1)
        tmp (vector-3 (x cam-dir) (y cam-dir) (z cam-dir))]
    (->
      tmp
      (.nor)
      (.scl (float (* f? velocity delta-time)))
      (.add cam-pos))))

(defn calc-move-z [{:keys [delta-time] :as screen} up?]
  (let [cam-up (up screen)
        cam-pos (position screen)
        u? (if up? 1 -1)
        tmp (vector-3 (x cam-up) (y cam-up) (z cam-up))]
    (->
      tmp
      (.nor)
      (.scl (float (* u? velocity delta-time)))
      (.add cam-pos))))

(defn calc-move-forward [screen]
  (calc-move-x screen true))

(defn calc-move-backward [screen]
  (calc-move-x screen false))

(defn calc-move-left [screen]
  (calc-move-strafe screen true))

(defn calc-move-right [screen]
  (calc-move-strafe screen false))

(defn calc-move-up [screen]
  (calc-move-z screen true))

(defn calc-move-down [screen]
  (calc-move-z screen false))


(defn turn-fn [screen]
    (let [dx (input! :get-delta-x)
          dy (input! :get-delta-y)
          rdx (float (* (- 0 dx) degrees-per-pixel))
          rdy (float (* (- 0 dy) degrees-per-pixel))
          cam-dir (direction screen)
          cam-up (up screen)
          tmp (vector-3 (x cam-dir) (y cam-dir) (z cam-dir))]
       (.rotate cam-dir cam-up rdx)
       (.rotate cam-dir (-> tmp (.crs cam-up) (.nor)) rdy)))

;;TODO: Better way of dealing with movement cases
(defn process-input [screen]
  (when (key-pressed? :w) (position! screen (calc-move-forward screen)))
  (when (key-pressed? :s) (position! screen (calc-move-backward screen)))
  (when (key-pressed? :a) (position! screen (calc-move-left screen)))
  (when (key-pressed? :d) (position! screen (calc-move-right screen)))
  (when (key-pressed? :q) (position! screen (calc-move-up screen)))
  (when (key-pressed? :e) (position! screen (calc-move-down screen)))
  (when (key-pressed? :escape) (app! :exit)))


(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen
             :renderer (model-batch)
             :attributes (let [attr-type (attribute-type :color :ambient-light)
                               attr (attribute :color attr-type 0.8 0.8 0.8 1)]
                           (environment :set attr))
             :camera (doto (perspective 75 (game :width) (game :height))
                       (position! 0 0 3)
                       (direction! 0 0 0)
                       (near! 0.1)
                       (far! 300)))
    (input! :set-cursor-catched true)
    (let [attr (attribute! :color :create-diffuse (color :blue))
          model-mat (material :set attr)
          model-attrs (bit-or (usage :position) (usage :normal))
          builder (model-builder)]
      (-> (model-builder! builder :create-box 2 2 2 model-mat model-attrs)
          model
          (assoc :x 0 :y 0 :z 0))))
  
  :on-render
  (fn [screen entities]
    (clear! 1 1 1 1)
    (process-input screen)
    (render! screen entities))

  :on-mouse-moved
  (fn [screen entities]
    (let [new-dir (turn-fn screen)]
      (cam-update! screen)))

  :on-touch-dragged
  (fn [screen entities]
    (let [new-dir (turn-fn screen)]
      (cam-update! screen))))

(defscreen text-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    (assoc (label "0" (color :black))
           :id :fps
           :x 5))
  
  :on-render
  (fn [screen entities]
    (->> (for [entity entities]
           (case (:id entity)
             :fps (doto entity (label! :set-text (str (game :fps))))
             entity))
         (render! screen)))
  
  :on-resize
  (fn [screen entities]
    (height! screen 300)))


(defgame first-person-play-clj
  :on-create
  (fn [this]
    (set-screen! this main-screen text-screen)))
