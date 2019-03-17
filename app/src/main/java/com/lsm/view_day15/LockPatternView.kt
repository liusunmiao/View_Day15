package com.lsm.view_day15

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 自定义九宫格解锁
 */
class LockPatternView : View {
    //是否初始化 确保只初始化一次
    private var mIsInt: Boolean = false
    //圆心的大小 外圆的半径
    private var mDotRadius = 0
    //二维数组用于存储九宫格点
    private var mPoints: Array<Array<Point?>> = Array(3) { Array<Point?>(3, { null }) }
    //画笔
    private lateinit var mLinePaint: Paint
    private lateinit var mPressedPaint: Paint
    private lateinit var mErrorPaint: Paint
    private lateinit var mNormalPaint: Paint
    private lateinit var mArrowPaint: Paint
    //颜色
    private val mOuterPressedColor = 0xff8cbad8.toInt()
    private val mInnerPressedColor = 0xff0596f6.toInt()
    private val mOuterNormalColor = 0xffd9d9d9.toInt()
    private val mInnerNormalColor = 0xff929292.toInt()
    private val mOuterErrorColor = 0xff901032.toInt()
    private val mInnerErrorColor = 0xffea0945.toInt()
    //按下的时候是否是按在一个点上面
    private var mIsTouchPoint: Boolean = false
    //存储选中的所有的点
    private val mSelectPoint = ArrayList<Point>()
    private lateinit var listener: OnPatterChangeListener

    //构造函数
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onDraw(canvas: Canvas?) {
        if (!mIsInt) {
            initDot()
            initPaint()
            mIsInt = true
        }
        //绘制九个宫格
        drawShow(canvas)
    }

    //手指触摸按下的位置
    private var mMovingX = 0f
    private var mMovingY = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mMovingX = event!!.x
        mMovingY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //重新进行绘制
                listener.onPatterStart(true)
                //恢复重新绘制的状态
                resetsPoint()
                //重置缓存的密码数据
                mSelectPoint.clear()
                //判断手指是不是按在一个宫格上面
                //如何判断一个点在圆里面  点到圆心的距离 小于半径
                val point = point
                if (point != null) {
                    mIsTouchPoint = true
                    //存储按下的点
                    mSelectPoint.add(point)
                    //改变当前点的状态
                    point.setStatusPressed()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsTouchPoint) {
                    //按下的时候一点要在一个点上 不断触摸的时候 不断去判断新的点
                    val point = point
                    if (point != null) {
                        //不包含的时候添加到集合里面
                        if (!mSelectPoint.contains(point)) {
                            mSelectPoint.add(point)
                        }
                        //改变当前状态
                        point.setStatusPressed()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mIsTouchPoint = false
                //抬起的时候回调密码监听
                if (mSelectPoint.size <= 1) {
                    //绘制不成立
                    resetsPoint()
                } else if (mSelectPoint.size in 2..4) {
                    //绘制错误
                    errorPoint()
                    //进行错误回调
                    listener.onPatterFailure()
                } else {
                    //绘制成功
                    //拼接密码
                    val sb = StringBuilder()
                    for (pwdPoint in mSelectPoint) {
                        sb.append(pwdPoint.index)
                    }
                    //进行回调
                    listener.onPatterSuccess(sb.toString())
                }
            }
        }
        invalidate()
        return true
    }

    /**
     * 绘制不成立
     */
    private fun resetsPoint() {
        for (point in mSelectPoint) {
            point.setStatusNormal()
        }
        mSelectPoint.clear()
    }

    /**
     * 绘制错误
     */
    private fun errorPoint() {
        for (point in mSelectPoint) {
            point.setStatusError()
        }
    }

    /**
     * 绘制九宫格显示
     */
    private fun drawShow(canvas: Canvas?) {
        for (i in 0..2) {
            for (point in mPoints[i]) {
                //普通状态
                if (point!!.statusIsNormal()) {
                    //绘制外圆
                    mNormalPaint.color = mOuterNormalColor
                    canvas?.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(),
                            mDotRadius.toFloat(), mNormalPaint)
                    //绘制内圆
                    mNormalPaint.color = mInnerNormalColor
                    canvas?.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(),
                            mDotRadius / 6.toFloat(), mNormalPaint)
                }
                //按下状态
                if (point.statusIsPressed()) {
                    //绘制外圆
                    mPressedPaint.color = mOuterPressedColor
                    canvas?.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(),
                            mDotRadius.toFloat(), mPressedPaint)
                    //绘制内圆
                    mPressedPaint.color = mInnerPressedColor
                    canvas?.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(),
                            mDotRadius / 6.toFloat(), mPressedPaint)
                }
                //错误
                if (point.statusIsError()) {
                    //绘制外圆
                    mErrorPaint.color = mOuterErrorColor
                    canvas?.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(),
                            mDotRadius.toFloat(), mErrorPaint)
                    //绘制内圆
                    mErrorPaint.color = mInnerErrorColor
                    canvas?.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(),
                            mDotRadius / 6.toFloat(), mErrorPaint)
                }
            }
        }
        //绘制两个点之间的线和箭头
        drawLine(canvas)
    }

    private fun drawLine(canvas: Canvas?) {
        if (mSelectPoint.size >= 1) {
            //两个点之间需要绘制一条线和箭头
            var lastPoint = mSelectPoint[0]
            for (point in mSelectPoint) {
                //两个点之间绘制一条线
                drawLine(lastPoint, point, canvas!!, mLinePaint)
                //两个点之间绘制一个箭头
                drawArrow(canvas!!, mArrowPaint, lastPoint, point, (mDotRadius / 4).toFloat(), 38)
                lastPoint = point
            }
            //如果手指在内圆就不要绘制
            val isNnnerPoint = MathUtil.checkInRound(lastPoint.centerX.toDouble(), lastPoint.centerY.toDouble(),
                    mDotRadius.toDouble() / 4, mMovingX.toDouble(), mMovingY.toDouble())
            //绘制最后一个点到手指当前位置的连线
            if (!isNnnerPoint && mIsTouchPoint) {
                drawLine(lastPoint, Point(mMovingX.toInt(), mMovingY.toInt(), -1), canvas!!, mLinePaint)
            }
        }
    }

    /**
     * 画线
     */
    private fun drawLine(start: Point, end: Point, canvas: Canvas, paint: Paint) {
        val pointDistance = MathUtil.distance(start.centerX.toDouble(), start.centerY.toDouble(),
                end.centerX.toDouble(), end.centerY.toDouble())
        val dx = end.centerX - start.centerX
        val dy = end.centerY - start.centerY

        val rx = (dx / pointDistance * (mDotRadius / 6.0)).toFloat()
        val ry = (dy / pointDistance * (mDotRadius / 6.0)).toFloat()
        canvas.drawLine(start.centerX + rx, start.centerY + ry, end.centerX - rx, end.centerY - ry, paint)
    }

    /**
     * 绘制箭头
     */
    private fun drawArrow(canvas: Canvas, paint: Paint, start: Point, end: Point, arrowHeight: Float, angle: Int) {
        val d = MathUtil.distance(start.centerX.toDouble(), start.centerY.toDouble(),
                end.centerX.toDouble(), end.centerY.toDouble())
        val sin_B = ((end.centerX - start.centerX) / d).toFloat()
        val cos_B = ((end.centerY - start.centerY) / d).toFloat()
        val tan_A = Math.tan(Math.toRadians(angle.toDouble())).toFloat()
        val h = (d - arrowHeight.toDouble() - mDotRadius * 1.1).toFloat()
        val l = arrowHeight * tan_A
        val a = l * sin_B
        val b = l * cos_B
        val x0 = h * sin_B
        val y0 = h * cos_B
        val x1 = start.centerX + (h + arrowHeight) * sin_B
        val y1 = start.centerY + (h + arrowHeight) * cos_B
        val x2 = start.centerX + x0 - b
        val y2 = start.centerY.toFloat() + y0 + a
        val x3 = start.centerX.toFloat() + x0 + b
        val y3 = start.centerY + y0 - a
        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.close()
        canvas.drawPath(path, paint)


    }

    /**
     * 初始化画笔
     * 3个点状的画笔  线的画笔  箭头的画笔
     */
    private fun initPaint() {
        //线的画笔
        mLinePaint = Paint()
        mLinePaint.color = mInnerPressedColor
        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.isAntiAlias = true
        mLinePaint.strokeWidth = (mDotRadius / 9).toFloat()
        //按下的画笔
        mPressedPaint = Paint()
        mPressedPaint.style = Paint.Style.STROKE
        mPressedPaint.isAntiAlias = true
        mPressedPaint.strokeWidth = (mDotRadius / 6).toFloat()
        //错误的画笔
        mErrorPaint = Paint()
        mErrorPaint.style = Paint.Style.STROKE
        mErrorPaint.isAntiAlias = true
        mErrorPaint.strokeWidth = (mDotRadius / 6).toFloat()
        //默认的画笔
        mNormalPaint = Paint()
        mNormalPaint.style = Paint.Style.STROKE
        mNormalPaint.isAntiAlias = true
        mNormalPaint.strokeWidth = (mDotRadius / 9).toFloat()
        //箭头画笔
        mArrowPaint = Paint()
        mArrowPaint.color = mInnerPressedColor
        mArrowPaint.style = Paint.Style.FILL
        mArrowPaint.isAntiAlias = true
    }

    /**
     * 初始化点
     */
    private fun initDot() {
        //九宫格 存到集合
        //不断绘制的时候这几个点都有状态  而且后面肯定需要将密码进行回调  点需要有下标
        var width = this.width
        var height = this.height
        //兼容横竖屏
        var offsetsX = 0
        var offsetsY = 0
        //横竖屏
        if (width > height) {
            //横屏
            offsetsX = (width - height) / 2
            width = height
        } else {
            //竖屏
            offsetsY = (height - width) / 2
            height = width
        }

        //外圆的大小
        mDotRadius = width / 14

        mPoints[0][0] = Point(offsetsX + width / 4, offsetsY + width / 4, 0)
        mPoints[0][1] = Point(offsetsX + width / 2, offsetsY + width / 4, 1)
        mPoints[0][2] = Point(offsetsX + (width - width / 4), offsetsY + width / 4, 2)

        mPoints[1][0] = Point(offsetsX + width / 4, offsetsY + width / 2, 3)
        mPoints[1][1] = Point(offsetsX + width / 2, offsetsY + width / 2, 4)
        mPoints[1][2] = Point(offsetsX + (width - width / 4), offsetsY + width / 2, 5)

        mPoints[2][0] = Point(offsetsX + width / 4, offsetsY + (width - width / 4), 6)
        mPoints[2][1] = Point(offsetsX + width / 2, offsetsY + (width - width / 4), 7)
        mPoints[2][2] = Point(offsetsX + (width - width / 4), offsetsY + (width - width / 4), 8)

    }

    /**
     * 获取按下的点
     * 判断当前按钮的点是否在九宫格里面
     */
    private val point: Point?
        get() {
            for (i in mPoints.indices) {
                for (j in 0..mPoints[i].size - 1) {
                    val point = mPoints[i][j]
                    //判断按钮的点是否在九宫格里面
                    if (MathUtil.checkInRound(point!!.centerX.toDouble(), point.centerY.toDouble(),
                                    mDotRadius.toDouble(), mMovingX.toDouble(), mMovingY.toDouble())) {
                        return point
                    }
                }
            }
            return null
        }

    class Point(var centerX: Int, var centerY: Int, var index: Int) {
        //状态
        private val STATUS_NOMAL = 1
        private val STATUS_PRESSED = 2
        private val STATUS_ERROE = 3
        //当前绘制的状态
        private var status = STATUS_NOMAL

        fun setStatusPressed() {
            status = STATUS_PRESSED
        }

        fun setStatusNormal() {
            status = STATUS_NOMAL
        }

        fun setStatusError() {
            status = STATUS_ERROE
        }

        fun statusIsPressed(): Boolean {
            return status == STATUS_PRESSED
        }

        fun statusIsNormal(): Boolean {
            return status == STATUS_NOMAL
        }

        fun statusIsError(): Boolean {
            return status == STATUS_ERROE
        }
    }

    /**
     * 回调监听接口
     */
    interface OnPatterChangeListener {
        /**
         * 成功监听
         */
        fun onPatterSuccess(password: String)

        /**
         * 设置密码错误回调
         */
        fun onPatterFailure()

        /**
         * 重新绘制
         */
        fun onPatterStart(isStart: Boolean)
    }

    /**
     * 设置回调监听
     */
    fun setPatterChangeListener(listener: OnPatterChangeListener) {
        this.listener = listener
    }
}