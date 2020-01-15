import processing.core.PVector

class Optimisation {
    companion object{
        var isEvaluation = false //評価中か
        private const val IS_DEBUG = true
        var boidsGroups : ArrayList<MutableList<Boid>>? = null

        fun evaluation(boids : MutableList<Boid>) : Float{
            //結合確認
            var copiedBoids = boids.toMutableList().listIterator()
            val delta = isNotBind(copiedBoids)

            return if(!IS_DEBUG || delta == 0){
                0f
            }else{
                //残存数
                val r = boids.size.toFloat()

                //グループ数カウント
                boidsGroups = ArrayList()
                copiedBoids = boids.toMutableList().listIterator()
                val g = countGroup(copiedBoids, boidsGroups!!).toFloat()


                r * g
            }
        }

        //グループ数のカウント
        private fun countGroup(boids : MutableIterator<Boid>, boidsGroups: ArrayList<MutableList<Boid>>) : Int{
            val detectedDist = 100f
            if(boids.hasNext()){
                val boid = boids.next()
                val group = mutableListOf<Boid>()
                group.add(boid)
                boidsGroups.add(group)
                boids.remove()

                checkGroup(boid, boids, detectedDist, group)
            }

            /**DEBUG*/
            if(IS_DEBUG){
                println("グループ数:${boidsGroups.size}")
            }

            return 0
        }
        private fun checkGroup(boidA: Boid, boids : MutableIterator<Boid>, detectedDist : Float, group : MutableList<Boid>){
            if(boids.hasNext()){
                val boidB = boids.next()
                //接近しすぎている
                if(PVector.dist(boidA.position, boidB.position) < detectedDist){
                    group.add(boidB)
                    boids.remove()
                    checkGroup(boidB, boids, detectedDist, group)
                }
            }
        }

        //結合してないか
        private val LIMIT_BIND_NUMBER = (BOID_AMOUNT * 0.1).toInt() //これ以上結合しているなら破棄する

        private fun isNotBind(boids : MutableIterator<Boid>) : Int{
            val detectedDist = BOID_BODY_SIZE * 2f
            var boundBoidsNumber = 0
            if(boids.hasNext()){
                val boid = boids.next()
                boids.remove()

                val boundBoids = checkBind(boid, boids, detectedDist, mutableListOf())
                if(boundBoids.isNotEmpty()){
                    boundBoidsNumber += boundBoids.size + 1
                }
//                if(boundBoidsNumber >= LIMIT_BIND_NUMBER) return 0
            }

            /**DEBUG*/
            if(IS_DEBUG){
                println("結合数:${boundBoidsNumber}")
            }
//            return 0
            return if(boundBoidsNumber < LIMIT_BIND_NUMBER) 1 else 0
        }
        private fun checkBind(boidA: Boid, boids : MutableIterator<Boid>, detectedDist : Float, boundBoids : MutableList<Boid>) : MutableList<Boid>{
            if(boids.hasNext()){
                val boidB = boids.next()
                //接近しすぎている
                if(PVector.dist(boidA.position, boidB.position) < detectedDist){
                    boundBoids.add(boidB)
                    boids.remove()
                    checkBind(boidB, boids, detectedDist, boundBoids)
                }
            }

            return boundBoids //自分自身の加算
        }
    }

}