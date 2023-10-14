package com.baokiin.hackathon.ui.main.detail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.transition.TransitionInflater
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.activity.BaseActivity
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.databinding.ActivityBitmapDetailBinding
import com.baokiin.hackathon.extension.fromJsonTypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BitmapDetailActivity :
    BaseActivity<ActivityBitmapDetailBinding>(R.layout.activity_bitmap_detail), View.OnTouchListener{


    companion object {
        val EXTRA_LIST_MODEL = "EXTRA_LIST_MODEL"
    }

    private val bitmapDetailAdapter by lazy {
        BitmapDetailAdapter()
    }

    private val bitmapList by lazy {
        intent.getStringExtra(EXTRA_LIST_MODEL)?.fromJsonTypeToken<List<BitmapModel>>()
    }

    private val TAG = "Touch"
    private val MIN_ZOOM = 1f
    val MAX_ZOOM = 1f

    // These matrices will be used to scale points of the image
    var matrix: Matrix = Matrix()
    var savedMatrix: Matrix = Matrix()

    // The 3 states (events) which the user is trying to perform
    val NONE = 0
    val DRAG = 1
    val ZOOM = 2
    var mode = NONE

    // these PointF objects are used to record the point(s) the user is touching
    var start = PointF()
    var mid = PointF()
    var oldDist = 1f
    var downPoint = PointF()


    private var scaleFactor = 1.0f
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onInitView() {
        super.onInitView()
        window.sharedElementEnterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition)
        binding.imgDetail.transitionName = bitmapList?.get(0)?.path
        setupRecyclerView()
        setupImageDetail()
    }

    private fun setupRecyclerView() {
        binding.rcvBitmapDetail.apply {
            layoutManager = LinearLayoutManager(
                this@BitmapDetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = bitmapDetailAdapter
        }
        bitmapDetailAdapter.updateList(bitmapList)
    }

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private fun setupImageDetail() {
        binding.imgDetail.setOnTouchListener(this)
        if (!bitmapList.isNullOrEmpty()) {
            bitmapList?.get(0)?.let {
                loadImage(it.path)
            }
        }
    }

    var imageMatrix:Matrix? =null
    override fun listenerView() {
        imageMatrix = binding.imgDetail.imageMatrix
        bitmapDetailAdapter.setOnListener {
            scaleFactor = scaleFactor.coerceIn(1f, 1f)
            matrix.setScale(scaleFactor, scaleFactor)
            binding.imgDetail.imageMatrix = matrix
            loadImage(it.path)
        }
    }

    private fun loadImage(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap: Bitmap? = BitmapFactory.decodeFile(url)
            withContext(Dispatchers.Main) {
                binding.imgDetail.setImageBitmap(bitmap)
            }
        }
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f) // Set minimum and maximum zoom limits

            matrix.setScale(scaleFactor, scaleFactor)
            binding.imgDetail.imageMatrix = matrix
            return true
        }
    }



    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        view.scaleType = ImageView.ScaleType.MATRIX
        val scale: Float
        dumpEvent(event)
        // Handle touch events here...

        when (event.getAction() and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                matrix.set(view.imageMatrix)
                savedMatrix.set(matrix)
                downPoint.set(event.x,event.y)
                start[event.getX()] = event.getY()
                Log.d(TAG, "mode=DRAG") // write to LogCat
                mode = DRAG
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                Log.d(TAG, "mode=NONE")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                Log.d(TAG, "oldDist=$oldDist")
                if (oldDist > 5f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                    Log.d(TAG, "mode=ZOOM")
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode === DRAG) {
                    val x = event.x - downPoint.x
                    if(x < 0 )
                        bitmapDetailAdapter.nextItem()
                    else
                        bitmapDetailAdapter.prevItem()
                    mode = NONE
                } else if (mode === ZOOM) {
                    // pinch zooming
                    val newDist: Float = spacing(event)
                    Log.d(TAG, "newDist=$newDist")
                    if (newDist > 5f) {
                        matrix.set(savedMatrix)
                        scale = newDist / oldDist // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y)
                    }
                }
            }
        }

        view.imageMatrix = matrix // display the transformation on screen


        return true // indicate event was handled

    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    /** Show an event in the LogCat view, for debugging  */
    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf(
            "DOWN",
            "UP",
            "MOVE",
            "CANCEL",
            "OUTSIDE",
            "POINTER_DOWN",
            "POINTER_UP",
            "7?",
            "8?",
            "9?"
        )
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action shr MotionEvent.ACTION_POINTER_ID_SHIFT)
            sb.append(")")
        }
        sb.append("[")
        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount) sb.append(";")
        }
        sb.append("]")
        Log.d("Touch Events ---------", sb.toString())
    }
}
