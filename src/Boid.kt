import app_display_manager.W_SIZE
import processing.core.PVector


//position: 位置座標, velocity:速度, acceleration:加速度
data class Boid(var position: PVector, var velocity: PVector, var acceleration: PVector)
const val BOID_BODY_SIZE = 2f
const val BOID_MAX_FORCE = 0.03f
const val BOID_MAX_SPEED = 2f
const val BOID_AMOUNT = 100

class BoidBehaviour{
    companion object{
        //分離(他のBoidオブジェクトと接触しないように距離を取る)
        fun separate(me:Boid, others: MutableList<Boid>): PVector {
            val desiredSeparation = 25.0f //距離25程度の間は空けたい
            val steer = PVector(0f, 0f, 0f)
            var count = 0 //近すぎると判断した個体数
            for (other in others) {
                val d = PVector.dist(me.position, other.position)
                //距離が近い
                if (d < desiredSeparation && d > 0) {
                    val diff = PVector.sub(me.position, other.position)//離れるベクトル
                    diff.normalize()//正規化
                    diff.div(d)//距離が遠いほど小さくなる
                    steer.add(diff)
                    count++
                }
            }
            if (count > 0) {
                steer.div(count.toFloat())//平均化
            }
            if (steer.mag() > 0) {
                steer.normalize()
                steer.mult(BOID_MAX_SPEED)
                steer.sub(me.velocity)
                steer.limit(BOID_MAX_FORCE)
            }
            return steer
        }

        //整列(他のBoidオブジェクトと同じ方向を向くように修正)
        fun align(me:Boid, boids: MutableList<Boid>): PVector {
            val neighborDist = 50f //この距離の中で整列を試みる。
            val sum = PVector(0f, 0f, 0f)
            var count = 0
            for (other in boids) {
                val d = PVector.dist(me.position, other.position)
                if (d > 0 && d < neighborDist) {
                    sum.add(other.velocity)
                    count++
                }
            }
            return if (count > 0) {
                sum.div(count.toFloat())
                sum.normalize()
                sum.mult(BOID_MAX_SPEED)
                val steer = PVector.sub(sum, me.velocity)
                steer.limit(BOID_MAX_FORCE)
                steer
            } else {
                PVector(0f, 0f, 0f)
            }
        }

        //結合(群の中心に向かう)
        fun cohesion(me:Boid, boids: MutableList<Boid>): PVector {
            val neighborDist = 50f //この半径距離の中で群の中心を探す
            val sum = PVector(0f, 0f, 0f)
            var count = 0
            for (other in boids) {
                val d = PVector.dist(me.position, other.position)
                if (d < neighborDist && d > 0) {
                    sum.add(other.position)
                    count++
                }
            }
            return if (count > 0) {
                sum.div(count.toFloat())
                seek(me, sum)
            } else {
                PVector(0f, 0f, 0f)
            }
        }
        private fun seek(me:Boid, target: PVector): PVector {
            val desired = PVector.sub(target, me.position)
            desired.normalize()
            desired.mult(BOID_MAX_SPEED)
            val steer = PVector.sub(desired, me.velocity)
            steer.limit(BOID_MAX_FORCE)
            return steer
        }

        //位置情報更新
        fun update(me:Boid): Unit {
            me.velocity.add(me.acceleration).limit(BOID_MAX_SPEED)
            me.position.add(me.velocity)
            me.acceleration.mult(0f)

            //境界線
            if (me.position.x > W_SIZE) {me.position.x = -W_SIZE} else if (me.position.x < -W_SIZE) {me.position.x = W_SIZE}
            if (me.position.y > W_SIZE) {me.position.y = -W_SIZE} else if (me.position.y < -W_SIZE) {me.position.y = W_SIZE}
            if (me.position.z > W_SIZE) {me.position.z = -W_SIZE} else if (me.position.z < -W_SIZE) {me.position.z = W_SIZE}
        }
    }
}