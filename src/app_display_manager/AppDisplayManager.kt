package app_display_manager

import BOID_AMOUNT
import BOID_MAX_SPEED
import Boid
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector


//メインウィンドウ
const val WINDOW_WIDTH = 920
const val WINDOW_HEIGHT = 600

//

class AppDisplayManager : PApplet (){
    fun run(args: Array<String>) : Unit = PApplet.main(AppDisplayManager::class.qualifiedName) //processing起動

    override fun settings(){
        //TIS = Text Input Sources, TSM = Text Services Manager
        size(WINDOW_WIDTH, WINDOW_HEIGHT, PConstants.P3D)
        //fullScreen(PConstants.P2D)
    }


    //Boid設定
    private lateinit var boids : MutableList<Boid>

    override fun setup(){
        fill(0xff)
        frameRate(60f)

        hint(PConstants.DISABLE_DEPTH_TEST)
        hint(PConstants.DISABLE_OPENGL_ERRORS)

        boids = mutableListOf()
        for (i in 0..BOID_AMOUNT){
            val position = PVector(random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE))
            val velocity = PVector(random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED))
            val acceleration = PVector(0f, 0f, 0f)

            boids.add(Boid(position, velocity, acceleration))
        }

    }

    var rotX = 0f
    var rotY = 0f
    var rotZ = 0f

    override fun draw(){
        background(0)
        translate(WINDOW_WIDTH/2f, WINDOW_HEIGHT/2f)
        //X軸を中心に回転
        rotateX(rotX)
        //Y軸を中心に回転
        rotateY(rotY)
        //Z軸を中心に回転
        rotateZ(rotZ)
        //四角形を描く
        rect(0f,0f,200f,200f)
        //それぞれの軸の回転角度を更新
        rotX += 0.02f
        rotY += 0.03f
        rotZ += 0.05f
    }

    override fun keyPressed() {
        //println("Pressed:$keyCode")
        when(keyCode){

        }
    }

    override fun keyReleased() {
        //println("Released:$keyCode")
        when(keyCode){

        }
    }

    companion object {
        //フィールドの設定
        private const val W_SIZE = 100f //立方体を想定。その中心を0としたときの面までの最短距離(つまり一辺の半分の長さ)
    }
}