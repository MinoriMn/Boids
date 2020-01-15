object QuickSort {
    fun sortOfMass(boidsEvaluation: Array<Float>, boidsParameters: Array<BoidsParameter>, left: Int, right: Int) {
        if (left <= right) {
            val p = boidsEvaluation[left + right ushr 1]
            var l = left
            var r = right
            while (l <= r) {
                while (boidsEvaluation[l] > p) {
                    l++
                }
                while (boidsEvaluation[r] < p) {
                    r--
                }
                if (l <= r) {
                    val tmp = boidsEvaluation[l]
                    val tmpP = boidsParameters[l]
                    boidsEvaluation[l] = boidsEvaluation[r]
                    boidsParameters[l] = boidsParameters[r]
                    boidsEvaluation[r] = tmp
                    boidsParameters[r] = tmpP
                    l++
                    r--
                }
            }
            sortOfMass(boidsEvaluation, boidsParameters, left, r)
            sortOfMass(boidsEvaluation, boidsParameters, l, right)
        }
    }
}