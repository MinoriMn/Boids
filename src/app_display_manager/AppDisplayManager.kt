package app_display_manager

import BOID_AMOUNT
import BOID_BODY_SIZE
import BOID_MAX_SPEED
import Boid
import BoidBehaviour
import ENEMY_BODY_SIZE
import ENEMY_MAX_FORCE
import ENEMY_MAX_SPEED
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

//メインウィンドウ
const val WINDOW_WIDTH = 1440f
const val WINDOW_HEIGHT = 900f

//ワールド設定
const val W_SIZE = 600f //立方体を想定。その中心を0としたときの面までの最短距離(つまり一辺の半分の長さ)

class AppDisplayManager : PApplet (){
    fun run(args: Array<String>) : Unit = PApplet.main(AppDisplayManager::class.qualifiedName) //processing起動

    override fun settings(){
        //TIS = Text Input Sources, TSM = Text Services Manager
        size(WINDOW_WIDTH.toInt(), WINDOW_HEIGHT.toInt(), PConstants.P3D)
        //fullScreen(PConstants.P2D)
    }

    //Boid設定
    private lateinit var boids : MutableList<Boid>
    private lateinit var enemies : MutableList<Boid>

    override fun setup(){
        stroke(0xff)
        frameRate(60f)

        hint(PConstants.DISABLE_DEPTH_TEST)
        hint(PConstants.DISABLE_OPENGL_ERRORS)

        //boids初期化
        boids = mutableListOf()
        enemies = mutableListOf()
        val r = 30f
        for (i in 0..BOID_AMOUNT){
            val position = PVector(random(-r, r), random(-r, r), random(-r, r))
//            val position = PVector(random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE))
            val velocity = PVector(random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED))
            val acceleration = PVector(0f, 0f, 0f)

            boids.add(Boid(position, velocity, acceleration))
        }

        /**敵の生成*/
        enemies.add(Boid(PVector(600f, 600f, 600f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))
        enemies.add(Boid(PVector(-600f, -600f, -600f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))
    }

    override fun draw(){
        boidsUpdate()
        enemiesUpdate()

        background(0)
        translate(WINDOW_WIDTH/2f, WINDOW_HEIGHT/2f)
        rotateX(map(mouseY.toFloat(), 0f, WINDOW_HEIGHT, -HALF_PI, HALF_PI))
        rotateY(map(mouseX.toFloat(), 0f, WINDOW_WIDTH, -HALF_PI, HALF_PI))

        //境界線表示
        line(-W_SIZE, -W_SIZE, -W_SIZE, W_SIZE, -W_SIZE, -W_SIZE)
        line(W_SIZE, -W_SIZE, -W_SIZE, W_SIZE, -W_SIZE, W_SIZE)
        line(W_SIZE, -W_SIZE, W_SIZE, -W_SIZE, -W_SIZE, W_SIZE)
        line(-W_SIZE, -W_SIZE, W_SIZE, -W_SIZE, -W_SIZE, -W_SIZE)

        line(-W_SIZE, W_SIZE, -W_SIZE, W_SIZE, W_SIZE, -W_SIZE)
        line(W_SIZE, W_SIZE, -W_SIZE, W_SIZE, W_SIZE, W_SIZE)
        line(W_SIZE, W_SIZE, W_SIZE, -W_SIZE, W_SIZE, W_SIZE)
        line(-W_SIZE, W_SIZE, W_SIZE, -W_SIZE, W_SIZE, -W_SIZE)

        line(-W_SIZE, -W_SIZE, -W_SIZE, -W_SIZE, W_SIZE, -W_SIZE)
        line(W_SIZE, -W_SIZE, -W_SIZE, W_SIZE, W_SIZE, -W_SIZE)
        line(W_SIZE, -W_SIZE, W_SIZE, W_SIZE, W_SIZE, W_SIZE)
        line(-W_SIZE, -W_SIZE, W_SIZE, -W_SIZE, W_SIZE, W_SIZE)

        boidsRender()
        enemiesRender()
    }

    //更新------------------------------------------------------------------

    //Boidsの更新
    private fun boidsUpdate() {
        //加速度更新
       boids.forEach{
           val sep: PVector = BoidBehaviour.separate(it, boids) //分離
           val ali: PVector = BoidBehaviour.align(it, boids) //整列
           val coh: PVector = BoidBehaviour.cohesion(it, boids) //結合
           val avo: PVector = BoidBehaviour.avoid(it, enemies) //逃避
           //パラメータ調整
           sep.mult(1.5f) //分離
           ali.mult(1.0f) //整列
           coh.mult(1.0f) //結合
           avo.mult(10.0f) //逃避
           it.acceleration.add(sep).add(ali).add(coh).add(avo)
       }

        //position更新
        boids.forEach{
            BoidBehaviour.update(it)
        }
    }
    //enemiesの更新
    private fun enemiesUpdate() {
        enemies.forEach {
            val atk = EnemyBehaviour.attack(it, boids)

            it.acceleration.add(atk)
        }

        //position更新
        enemies.forEach{
            EnemyBehaviour.update(it)
        }
    }

    //描画------------------------------------------------------------------

    //Boidの描画
    private fun boidsRender(): Unit {
        fill(255f)
        boids.forEach {
            pushMatrix()
            translate(it.position.x, it.position.y, it.position.z)
            rotateZ(atan2(it.velocity.y, it.velocity.x) + PConstants.HALF_PI)
            rotateY(atan2(it.velocity.x, it.velocity.z) + PConstants.HALF_PI)
            beginShape(PConstants.TRIANGLES)
            vertex(0f, -BOID_BODY_SIZE * 2, 0f)
            vertex(-BOID_BODY_SIZE / 2, BOID_BODY_SIZE * 2, 0f)
            vertex(BOID_BODY_SIZE / 2, BOID_BODY_SIZE * 2, 0f)
            endShape()
            popMatrix()
        }
    }
    //enemiesの描画
    private fun enemiesRender() {
        noFill()
        enemies.forEach{
            pushMatrix()
            val position = it.position
            translate(position.x, position.y, position.z)
            sphere(ENEMY_BODY_SIZE)
            popMatrix()
        }
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
}