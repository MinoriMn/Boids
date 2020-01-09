import processing.core.PVector

//position: 位置座標, velocity:速度, acceleration:加速度
data class Boid(var position: PVector, var velocity: PVector, var acceleration: PVector)
const val BOID_BODY_SIZE = 2f
const val BOID_MAX_SPEED = 0.5f
const val BOID_AMOUNT = 100