(ns first-person-play-clj.core
  (:require [play-clj.core :refer :all]
            [play-clj.g3d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]))

(def ^:const velocity 5)

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

(defn calc-move-forward [screen]
  (calc-move-x screen true))

(defn calc-move-backward [screen]
  (calc-move-x screen false))

(defn calc-move-left [screen]
  (calc-move-strafe screen true)
)

(defn calc-move-right [screen]
  (calc-move-strafe screen false)
)

;;TODO: Better way of dealing with movement cases
(defn process-input [screen]
  (if (key-pressed? :w)
    (let [f (calc-move-forward screen)]
      (position! screen (x f) (y f) (z f))))
  (if (key-pressed? :s)
    (let [f (calc-move-backward screen)]
      (position! screen (x f) (y f) (z f))))
  (if (key-pressed? :a)
    (let [f (calc-move-left screen)]
      (position! screen (x f) (y f) (z f))))
  (if (key-pressed? :d)
    (let [f (calc-move-right screen)]
      (position! screen (x f) (y f) (z f))))

)


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
    (render! screen entities)))

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
