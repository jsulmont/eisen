; Inspired by the snakes that have gone before:
; Abhishek Reddy's snake: http://www.plt1.com/1070/even-smaller-snake/
; Mark Volkmann's snake: http://www.ociweb.com/mark/programming/ClojureSnake.html 

(ns snake.worm
  (:import (java.awt Color Dimension) 
	   (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:use snake.import-static
        [blancas.eisen.core :only (init-eisen eisen ->m m-> run->)]))

(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)


; ----------------------------------------------------------
; To run:
; ----------------------------------------------------------
(comment
  (load "snake/worm")
  (ns snake.worm)
  (game)
)

; ----------------------------------------------------------
; functional model
; ----------------------------------------------------------

(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 75)
(def dirs { VK_LEFT  [-1  0] 
            VK_RIGHT [ 1  0]
            VK_UP    [ 0 -1] 
	    VK_DOWN  [ 0  1]})

(defn add-points [& pts] 
  (vec (apply map + pts)))

(defn point-to-screen-rect [pt] 
  (map #(* point-size %) 
       [(pt 0) (pt 1) 1 1]))

(defn create-apple [] 
  {:location [(rand-int width) (rand-int height)]
   :color (Color. 210 50 90)
   :type :apple}) 

(defn create-snake []
  {:body (m-> body)
   :dir  (m-> dir)
   :type :snake
   :color (Color. 15 160 70)})

(defn move [{:keys [body dir] :as snake} & grow]
  (assoc snake :body (cons (add-points (first body) dir)
			   (if grow body (butlast body)))))

(defn turn [snake newdir] 
  (assoc snake :dir newdir))

(defn win? [{body :body}]
  (>= (count body) (m-> win-length)))

(defn head-overlaps-body? [{[head & body] :body}]
  (contains? (set body) head))

(def lose? head-overlaps-body?)

(defn eats? [{[snake-head] :body} {apple :location}]
   (= snake-head apple))

; ----------------------------------------------------------
; mutable model
; ----------------------------------------------------------

(defn update-positions [snake apple]
  (dosync
   (if (eats? @snake @apple)
     (do (ref-set apple (create-apple))
	 (alter snake move :grow))
     (alter snake move)))
  nil)

(defn update-direction [snake newdir]
  (when newdir (dosync (alter snake turn newdir))))

(defn reset-game [snake apple]
  (dosync (ref-set apple (create-apple))
	  (ref-set snake (create-snake)))
  nil)

; ----------------------------------------------------------
; extensibility
; ----------------------------------------------------------

(->m win-length 5
     body       (list [1 1])
     dir        [1 0])

(defn run-eisen []
  (let [code (JOptionPane/showInputDialog
	        nil "Paste your code here:" "Change the game"
	        JOptionPane/PLAIN_MESSAGE)]
    (when (seq code)
      (let [result (eisen code)]
	(when-not (:ok result)
  	  (JOptionPane/showMessageDialog nil (:error result)))))))


; ----------------------------------------------------------
; gui
; ----------------------------------------------------------

(defn fill-point [g pt color] 
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g color) 
    (.fillRect g x y width height)))

(defmulti paint (fn [g object & _] (:type object)))

(defmethod paint :apple [g {:keys [location color]}]
  (fill-point g location color))

(defmethod paint :snake [g {:keys [body color]}]
  (doseq [point body]
    (fill-point g point color)))

(defn game-panel [frame snake apple]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (paint g @apple))
    (actionPerformed [e]
      (update-positions snake apple)
      (when (lose? @snake)
	(JOptionPane/showMessageDialog frame "You lose!")
	(run-eisen)
	(reset-game snake apple))
      (when (win? @snake)
	(JOptionPane/showMessageDialog frame "You win!")
	(run-eisen)
	(reset-game snake apple))  
      (.repaint this))
    (keyPressed [e]
      (update-direction snake (dirs (.getKeyCode e))))
    (getPreferredSize [] 
      (Dimension. (* (inc width) point-size) 
		  (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

(defn game []
  (init-eisen)
  (let [snake (ref (create-snake))
	apple (ref (create-apple))
	frame (JFrame. "Worm")
	panel (game-panel frame snake apple)
	timer (Timer. turn-millis panel)]
    (doto panel
      (.setFocusable true)
      (.addKeyListener panel))
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (binding [blancas.eisen.core/model blancas.eisen.core/model]
      (.start timer))))


