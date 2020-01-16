package app_display_manager

import BOID_AMOUNT
import BOID_BODY_SIZE
import BOID_MAX_SPEED
import Boid
import BoidBehaviour
import BoidsParameter
import ENEMY_BODY_SIZE
import EnemyBehaviour
import Optimisation
import QuickSort
import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import boundBoidsNumber
import evaRst
import gEva
import groupNum
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


//メインウィンドウ
const val WINDOW_WIDTH = 1440f
const val WINDOW_HEIGHT = 900f

//ワールド設定
const val W_SIZE = 500f //立方体を想定。その中心を0としたときの面までの最短距離(つまり一辺の半分の長さ)

//実行モード
enum class MODE{
    NORMAL,
    EVOLUTION, //進化計算
    NORMAL_EVALUATION //評価関数の確認用
}
var mode = MODE.NORMAL
var isFulLRandom = false

var theArgs : Array<String> = arrayOf()
class AppDisplayManager : PApplet (){
    fun run(args: Array<String>) : Unit{ //args [0]->起動モード, [1](NORMAL, 読み込み世代), [2](NORMAL, 読み込み集団)
        PApplet.main(AppDisplayManager::class.qualifiedName)
        theArgs = args.clone()
        println("called1, ${args[0]}, ${theArgs[0]}")

    } //processing起動

    //進化計算用
    private var time = 0//経過時間
    private val TIME_LIMIT = 3000 //制限時間
    private var generation = 0//世代
    private val GENERATION_LIMIT = 1//最終世代
    private val MASS = 50//世代ごとの集団数

    //Boid設定
    private lateinit var boids : MutableList<Boid>
    private lateinit var enemies : MutableList<Boid>
    private lateinit var boidsParameters: Array<BoidsParameter>
    private lateinit var boidsEvaluation: Array<Float>

    override fun settings(){
        //TIS = Text Input Sources, TSM = Text Services Manager
        size(WINDOW_WIDTH.toInt(), WINDOW_HEIGHT.toInt(), PConstants.P3D)
        //fullScreen(PConstants.P2D)
    }

    override fun setup(){
        frameRate(60f)
        colorMode(PConstants.HSB) //カラーモードをHSBに変更
        println("called2, ${theArgs[0]}")

        if(theArgs.isNotEmpty()) {
            mode = when (theArgs[0]) {
                "EVOLUTION" -> MODE.EVOLUTION
                else -> MODE.NORMAL
            }
            if (theArgs.count() > 1 && theArgs[1].contains("FULL_RANDOM")) {
                isFulLRandom = true
            }
        }

        if(mode == MODE.NORMAL){
            //csvのパラメータ読み込み
            if(theArgs.size >= 3){
                val gen = theArgs[1].toInt()
                val massNum = theArgs[2].toInt()
                val path = System.getProperty("user.dir")
                val reader: CSVReader = CSVReader(FileReader(File("${path}/evaluation", "generation-${gen}.csv")))
                val data = reader.readAll()
                val params = data[massNum]
                val param_names = data[0]

                for ((index, param) in params.withIndex()) {
                    println(param_names[index] + " : " + param)
                }


                // "1世代,順位","評価点","R分離","R整列","R結合","R逃避","P分離","P整列","P結合","P逃避"
                boidsParameters = Array(1) {BoidsParameter(params[2].toFloat(), params[3].toFloat(), params[4].toFloat(), params[5].toFloat(), params[6].toFloat(), params[7].toFloat() ,params[8].toFloat() ,params[9].toFloat())}
            }else{
                /**DEBUG*/
                boidsParameters = Array(1) {BoidsParameter(20.0f, 100f, 100f, 70f +  ENEMY_BODY_SIZE, 1.5f, 1f ,1f ,10f)}
            }
        }else if(mode == MODE.EVOLUTION){
            if(theArgs.size >= 2){
                //TODO 世代を読み込むようにする
                exit()
            }else{
                //Boids集団のパラメータ R:各ベクトルの適応範囲{(sep),(ali),(coh):BOID_BODY_SIZE * 2 ~ W_SIZE/3,  (avoid) ENEMY_BODY_SIZE ~ W_SIZE/3}、 P:各ベクトルの倍率(1.0 ~ 10.0)
                boidsParameters = Array(MASS) {BoidsParameter(random(BOID_BODY_SIZE*2f, W_SIZE/3f), random(BOID_BODY_SIZE*2f, W_SIZE/3f), 100f, random(ENEMY_BODY_SIZE, W_SIZE/3f), random(1f, 10f), random(1f, 10f) ,random(1f, 10f) ,random(1f, 10f))}
                boidsEvaluation = Array(MASS) {0f}
            }
        }

//        hint(PConstants.DISABLE_DEPTH_TEST)
//        hint(PConstants.DISABLE_OPENGL_ERRORS)

        resetBoids()
    }
    private fun resetBoids(){
        //boids初期化
        boids = mutableListOf()
        enemies = mutableListOf()
        val r = 30f
        for (i in 1..BOID_AMOUNT){
            val position = PVector(random(-r, r), random(-r, r), random(-r, r))
//            val position = PVector(random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE))
            val velocity = PVector(random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED))
            val acceleration = PVector(0f, 0f, 0f)

            boids.add(Boid(position, velocity, acceleration))
        }

        /**敵の生成*/
        enemies.add(Boid(PVector(-550f, 550f, -550f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))
        enemies.add(Boid(PVector(500f, 500f, 500f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))
//        enemies.add(Boid(PVector(-300f, -300f, 300f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))
//        enemies.add(Boid(PVector(250f, -250f, -250f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))

        time = 0
    }

    override fun draw(){
        if(mode != MODE.EVOLUTION) {
            if (mode == MODE.NORMAL) {
                boidsUpdate(boids, boidsParameters[0])
                enemiesUpdate(enemies, boids)
                time++
            }

            background(0)
            stroke(0f, 0f, 255f)
            fill(0f, 0f, 255f)
            text("remaining boids: ${boids.size}", 10f, 30f) // 表示するテキスト, x座標, y座標
            text("time: $time", 10f, 50f)
            if (mode == MODE.NORMAL_EVALUATION) {
                text("group num: ${groupNum}, G: $gEva", 10f, 70f) // 表示するテキスト, x座標, y座標
                text(
                    "bound num: ${boundBoidsNumber}, delta: ${boundBoidsNumber < Optimisation.LIMIT_BIND_NUMBER}",
                    10f,
                    90f
                )
                text("ev: $evaRst", 10f, 110f)
            }

            translate(WINDOW_WIDTH / 2f, WINDOW_HEIGHT / 2f)
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
        }else{
            //MODE_EVOLUTION 進化計算
            runBlocking {
                runAllSimulation()
            }
        }

        mode = MODE.NORMAL
        val bestBoidsParameter = boidsParameters[0]
        boidsParameters = Array(1) {bestBoidsParameter}
    }

    //進化計算------------------------------------------------
    suspend fun runAllSimulation(){
        for (gen in 0 until GENERATION_LIMIT){
            val rst = HashMap<Int, Deferred<MutableList<Boid>>>()
            for (idx in 0 until MASS) {
                //シミュ開始
                println("第${gen}世代、idx:${idx}シミュ開始、R:${boids.size}")

                val tmp = GlobalScope.async {
                    runAMassSimulation(boidsParameters[idx])
                }
                rst[idx] = tmp
            }
            for (idx in 0 until MASS){
                val boids = rst[idx]?.await()
                if(boids != null){
                    //シミュ終了
                    Optimisation.evaluation(boids)
                    //評価
                    boidsEvaluation[idx] = evaRst

                    print("第${gen}世代、idx:${idx}シミュ終了、")
                    if (Optimisation.IS_DEBUG) {
                        print("R:${boids.size}、G:${groupNum}、Delta:${boundBoidsNumber < Optimisation.LIMIT_BIND_NUMBER}、")
                    }
                    kotlin.io.println("Eva:${evaRst}")
                }
            }

            //評価順にソート
            QuickSort.sortOfMass(boidsEvaluation, boidsParameters, 0, boidsEvaluation.size-1)

            /**DEBUG*/
            for (idx in 0 until MASS){
                println("$idx: ${boidsEvaluation[idx]}[${boidsParameters}]")
            }

            //csv書き出し
            exportCsv(gen)

            //次世代
            if(gen < GENERATION_LIMIT-1){
                var nextBoidsParameters = Array(MASS) {BoidsParameter(random(BOID_BODY_SIZE*2f, W_SIZE/3f), random(BOID_BODY_SIZE*2f, W_SIZE/3f), 100f, random(ENEMY_BODY_SIZE, W_SIZE/3f), random(1f, 10f), random(1f, 10f) ,random(1f, 10f) ,random(1f, 10f))}

                var idx = 0
                //エリート保存戦略
                val mass10per = MASS/10
                while (idx < mass10per){
                    //評価点が1より大きいなら残す
                    if(boidsEvaluation[idx] > 1f){
                        nextBoidsParameters[idx] = boidsParameters[idx]
                    }else{
                        break
                    }
                    idx++
                }

                val parentsIdx = idx
                if(parentsIdx != 0){
                    val mass90per = MASS/10*9
                    val mutation = 0.5f //突然変異確率
                    while(idx < mass90per){
                        nextBoidsParameters[idx] =
                            BoidsParameter(
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].separateR + random(-BOID_BODY_SIZE, BOID_BODY_SIZE) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].separateR,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].alignR+ random(-BOID_BODY_SIZE, BOID_BODY_SIZE) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].alignR,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].cohesionR + random(-BOID_BODY_SIZE, BOID_BODY_SIZE) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].cohesionR,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].avoidR + random(-BOID_BODY_SIZE, BOID_BODY_SIZE) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].avoidR,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].separateP + random(-1f, 1f) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].separateP,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].alignP + random(-1f, 1f) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].alignP,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].cohesionP + random(-1f, 1f) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].cohesionP,
                                if(random(1f) > mutation) boidsParameters[random(0f, parentsIdx.toFloat()).toInt()].avoidP + random(-1f, 1f) else nextBoidsParameters[random(mass90per.toFloat(), MASS.toFloat()).toInt()].avoidP
                            )
                        idx++
                    }
                }

                //世代交代
                boidsParameters = nextBoidsParameters
            }
        }
    }

    //進化計算コルーチン用
    suspend fun runAMassSimulation(boidsParameter: BoidsParameter): MutableList<Boid>{
        val boids : MutableList<Boid> = mutableListOf()
        val enemies : MutableList<Boid> = mutableListOf()
//boids初期化
        val r = 30f
        var time = 0

        for (i in 1..BOID_AMOUNT){
            val position = PVector(random(-r, r), random(-r, r), random(-r, r))
//            val position = PVector(random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE), random(-W_SIZE, W_SIZE))
            val velocity = PVector(random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED), random(-BOID_MAX_SPEED, BOID_MAX_SPEED))
            val acceleration = PVector(0f, 0f, 0f)

            boids.add(Boid(position, velocity, acceleration))
        }

        /**敵の生成*/
        enemies.add(Boid(PVector(-550f, 550f, -550f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))
        enemies.add(Boid(PVector(500f, 500f, 500f), PVector(0f, 0f, 0f), PVector(0f, 0f, 0f)))

        while (time++ < TIME_LIMIT){
            boidsUpdate(boids, boidsParameter)
            enemiesUpdate(enemies, boids)
            if(boids.size  < BOID_AMOUNT * 0.5){//半数以下まで個体数が減った時は強制終了
                break
            }
        }

        return boids
    }

    private fun exportCsv(generation: Int){
        //CSVファイル生成
        var csvw: CSVWriter? = null
        val path = System.getProperty("user.dir")
        try { //インスタンス生成
            csvw = CSVWriter(
                FileWriter(File("${path}/evaluation", "generation-${generation}.csv"))
                , ","[0]
                , "\""[0]
                , "\""[0]
                , "\r\n"
            )
            //Stringの配列が1行分のデータになる
            val outStrList: MutableList<Array<String?>> =
                ArrayList()
            //配列の数には項目数を定義

            //ラベル
            val outStr = arrayOfNulls<String>(10)
            outStr[0] = "${generation}世代,順位"
            outStr[1] = "評価点"
            outStr[2] = "R分離"
            outStr[3] = "R整列"
            outStr[4] = "R結合"
            outStr[5] = "R逃避"
            outStr[6] = "P分離"
            outStr[7] = "P整列"
            outStr[8] = "P結合"
            outStr[9] = "P逃避"
            outStrList.add(outStr)

            //データ
            for (idx in 0 until MASS){
                val boidsParameter = boidsParameters[idx]
                val outStr = arrayOf<String?>(
                    "$idx", "${boidsEvaluation[idx]}",
                    boidsParameter.separateR.toString(), boidsParameter.alignR.toString(),
                    boidsParameter.cohesionR.toString(), boidsParameter.avoidR.toString(),
                    boidsParameter.separateP.toString(), boidsParameter.alignP.toString(),
                    boidsParameter.cohesionP.toString(), boidsParameter.avoidP.toString()
                )
                outStrList.add(outStr)
            }

            //書込み
            csvw.writeAll(outStrList)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally { // 最後はクローズする
            csvw?.close()
        }

    }

    //更新------------------------------------------------------------------

    //Boidsの更新
    private fun boidsUpdate(boids: MutableList<Boid>, boidsParameter: BoidsParameter) {
        val sepR = boidsParameter.separateR
        val aliR = boidsParameter.alignR
        val cohR = boidsParameter.cohesionR
        val avoR = boidsParameter.avoidR

        val sepP = boidsParameter.separateP
        val aliP = boidsParameter.alignP
        val cohP = boidsParameter.cohesionP
        val avoP = boidsParameter.avoidP

        //加速度更新
       boids.forEach{
           val sep: PVector = BoidBehaviour.separate(it, boids, sepR) //分離
           val ali: PVector = BoidBehaviour.align(it, boids, aliR) //整列
           val coh: PVector = BoidBehaviour.cohesion(it, boids, cohR) //結合
           val avo: PVector = BoidBehaviour.avoid(it, enemies, avoR) //逃避
           //パラメータ調整
           sep.mult(sepP) //分離
           ali.mult(aliP) //整列
           coh.mult(cohP) //結合
           avo.mult(avoP) //逃避
           it.acceleration.add(sep).add(ali).add(coh).add(avo)
       }

        //position更新
        boids.forEach{
            BoidBehaviour.update(it)
        }
    }
    //enemiesの更新
    private fun enemiesUpdate(enemies: MutableList<Boid>, boids: MutableList<Boid>) {
        enemies.forEach {
            val atk = EnemyBehaviour.attack(it, boids, this)

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
        noStroke()
        if(mode == MODE.NORMAL){
            fill(0f, 0f, 255f)

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
        }else if (mode  == MODE.NORMAL_EVALUATION){
            while (Optimisation.isEvaluation){ }//評価終了を待つ
            if(Optimisation.boidsGroups != null){
                val boidsGroups = Optimisation.boidsGroups!!
                val hue = 255f / boidsGroups.size

                for (i in 0 until boidsGroups.size){
                    fill(hue * i, 255f, 255f)
                    boidsGroups[i].forEach {
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
            }
        }

    }

    //enemiesの描画
    private fun enemiesRender() {
        noFill()
        stroke(255f, 50f)
        enemies.forEach{
            pushMatrix()
            val position = it.position
            translate(position.x, position.y, position.z)
            sphere(ENEMY_BODY_SIZE)
            popMatrix()
        }
    }

    override fun keyPressed() {
//        println("Pressed:$keyCode")
        when(keyCode){
            //0x46 = fキー押下
            0x46 -> if(mode == MODE.NORMAL){
                mode = MODE.NORMAL_EVALUATION
                Optimisation.isEvaluation = true
                Optimisation.evaluation(boids)
                Optimisation.isEvaluation = false
            }
        }
    }

    override fun keyReleased() {
        //println("Released:$keyCode")
        when(keyCode){
            0x46 -> if(mode == MODE.NORMAL_EVALUATION) mode = MODE.NORMAL
        }
    }
}