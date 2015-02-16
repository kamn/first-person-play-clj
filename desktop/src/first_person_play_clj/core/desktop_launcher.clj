(ns first-person-play-clj.core.desktop-launcher
  (:require [first-person-play-clj.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. first-person-play-clj "first-person-play-clj" 800 600)
  (Keyboard/enableRepeatEvents true))
