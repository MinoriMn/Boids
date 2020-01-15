class Optimisation {
    companion object{
        fun evaluation(boids : MutableList<Boid>) : Float{
            val r = boids.size.toFloat()
            val g = countGroup(boids).toFloat()
            val delta = isNotBind(boids).toFloat()

            return r * g * delta
        }

        //グループ数のカウント
        private fun countGroup(boids : MutableList<Boid>) : Int{
            return 0
        }

        //結合してないか
        private fun isNotBind(boids : MutableList<Boid>) : Int{
            return 0
        }
    }

}