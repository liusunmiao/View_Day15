package com.lsm.view_day15

class MathUtil {
    companion object {
        fun distance(x1:Double,y1:Double,x2:Double,y2:Double):Double{
            return Math.sqrt(Math.abs(x1-x2)*Math.abs(x1-x2)
                    +Math.abs(y1-y2)*Math.abs(y1-y2))
        }
        fun pointTotoDegrees(x:Double,y:Double):Double{
            return Math.toDegrees(Math.atan2(x,y))
        }

        /**
         * 判断按下的点是否在九宫格里面
         */
        fun checkInRound(sx:Double, sy:Double, r:Double, x:Double, y:Double):Boolean{
            //x的平方+y的平方 开根号 和半径大小的比较
            return Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) < r
        }
    }
}