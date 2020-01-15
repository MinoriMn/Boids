import processing.core.PVector
import kotlin.math.exp

var groupNum = 0f//グループ数
var gEva = 0f//グループ数評価
var boundBoidsNumber = 0//結合数
var evaRst = 0f//評価値

class Optimisation {
    companion object{
        var isEvaluation = false //評価中か
        const val IS_DEBUG = true
        var boidsGroups : ArrayList<MutableList<Boid>>? = null

        fun evaluation(boids : MutableList<Boid>):Float{
            //結合確認
            var copiedBoids = boids.toMutableList()
            val delta = isNotBind(copiedBoids)

            evaRst = if(!IS_DEBUG || delta == 0){
                0f
            }else{
                //残存数
                val r = boids.size.toFloat()

                //グループ数カウント
                boidsGroups = ArrayList()
                copiedBoids = boids.toMutableList()
                val groupNum = countGroup(copiedBoids, boidsGroups!!).toFloat()
                //シグモイド関数の適応
                gEva = 1f / (1f + exp(3f * groupNum - 9f))

                r * gEva
            }
            return evaRst
        }

        //グループ数のカウント
        private fun countGroup(boids : MutableList<Boid>, boidsGroups: ArrayList<MutableList<Boid>>) : Int{
            val detectedDist = 100f
            var boidsIterator = boids.iterator()
            while(boidsIterator.hasNext()){
                val boid = boidsIterator.next()
                val group = mutableListOf<Boid>()
                group.add(boid)
                boidsGroups.add(group)
                boidsIterator.remove()

                checkGroup(boid, boids, detectedDist, group)

                boidsIterator = boids.iterator()
            }

            /**DEBUG*/
//            if(IS_DEBUG){
//                println("グループ数:${boidsGroups.size}")
//            }

            return boidsGroups.size
        }
        private fun checkGroup(boidA: Boid, boids : MutableList<Boid>, detectedDist : Float, group : MutableList<Boid>){
            var boidsIterator = boids.iterator()
            while(boidsIterator.hasNext()){
                val boidB = boidsIterator.next()
                //結合範囲内 = グループの一部
                if(PVector.dist(boidA.position, boidB.position) < detectedDist){
                    group.add(boidB)
                    boidsIterator.remove()
                    checkGroup(boidB, boids, detectedDist, group)
                    boidsIterator = boids.iterator()
                }
            }
        }

        //結合してないか
        const val LIMIT_BIND_NUMBER = (BOID_AMOUNT * 0.1).toInt() //これ以上結合しているなら破棄する

        private fun isNotBind(boids : MutableList<Boid>) : Int{
            val detectedDist = BOID_BODY_SIZE * 2f
            var boidsIterator = boids.iterator()
            boundBoidsNumber = 0
            while(boidsIterator.hasNext()){
                val boid = boidsIterator.next()
                boidsIterator.remove()
                val boundBoids = checkBind(boid, boids, detectedDist, mutableListOf())
                boidsIterator = boids.iterator()

                if(boundBoids.isNotEmpty()){
                    boundBoidsNumber += boundBoids.size + 1
                }
//                if(boundBoidsNumber >= LIMIT_BIND_NUMBER) return 0
            }

            /**DEBUG*/
//            if(IS_DEBUG){
//                println("結合数:${boundBoidsNumber}")
//            }
//            return 0
            return if(boundBoidsNumber < LIMIT_BIND_NUMBER) 1 else 0
        }
        private fun checkBind(boidA: Boid, boids : MutableList<Boid>, detectedDist : Float, boundBoids : MutableList<Boid>) : MutableList<Boid>{
            var boidsIterator = boids.iterator()
            while(boidsIterator.hasNext()){
                val boidB = boidsIterator.next()
                //接近しすぎている
                if(PVector.dist(boidA.position, boidB.position) < detectedDist){
                    boundBoids.add(boidB)
                    boidsIterator.remove()
                    checkBind(boidB, boids, detectedDist, boundBoids)
                    boidsIterator = boids.iterator()
                }
            }

            return boundBoids //自分自身の加算
        }
    }

}