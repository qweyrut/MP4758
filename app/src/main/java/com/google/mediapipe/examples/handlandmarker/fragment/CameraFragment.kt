/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.handlandmarker.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.mediapipe.examples.handlandmarker.HandLandmarkerHelper
import com.google.mediapipe.examples.handlandmarker.MainViewModel
import com.google.mediapipe.examples.handlandmarker.R
import com.google.mediapipe.examples.handlandmarker.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), HandLandmarkerHelper.LandmarkerListener {

    companion object {
        private const val TAG = "Hand Landmarker"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService
//主要
    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }

        // Start the HandLandmarkerHelper again when users come back
//        // to the foreground.
        backgroundExecutor.execute {
            if (handLandmarkerHelper.isClose()) {
                handLandmarkerHelper.setupHandLandmarker()
            }
        }
    }
//没用，主要用来切换是保存数据
//    override fun onPause() {
//        super.onPause()
//        if(this::handLandmarkerHelper.isInitialized) {
//            viewModel.setMaxHands(handLandmarkerHelper.maxNumHands)
//            viewModel.setMinHandDetectionConfidence(handLandmarkerHelper.minHandDetectionConfidence)
//            viewModel.setMinHandTrackingConfidence(handLandmarkerHelper.minHandTrackingConfidence)
//            viewModel.setMinHandPresenceConfidence(handLandmarkerHelper.minHandPresenceConfidence)
//            viewModel.setDelegate(handLandmarkerHelper.currentDelegate)
//
//            // Close the HandLandmarkerHelper and release resources
//            backgroundExecutor.execute { handLandmarkerHelper.clearHandLandmarker() }
//        }
//    }
//这段代码的作用是在 Fragment 的 onDestroyView 方法中执行一些操作。它释放对视图绑定对象的引用，关闭后台执行器，并等待后台执行器中的任务完成。这样可以确保在销毁视图时释放相关资源并等待后台任务的完成。
    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    //主要结构
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the HandLandmarkerHelper that will handle the inference
        //设置初始化
        backgroundExecutor.execute {
            handLandmarkerHelper = HandLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = 0.5f,
                minHandTrackingConfidence = 0.5f,
                minHandPresenceConfidence = 0.5f,
                maxNumHands = 2,
                currentDelegate = 1,
                handLandmarkerHelperListener = this
            )
        }

        // Attach listeners to UI control widgets
//        initBottomSheetControls()
    }
//该方法通过异步方式获取相机提供者的实例，并在初始化完成后绑定相机用例。
    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    //用于绑定相机使用案例
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        //用来描绘手部坐标
                        detectHand(image)
                        // 在这里侦测是可以的
//                        val handDetectTensorflow = context?.let { it1 -> HandDetectTensorflow(it1) }
//                        handDetectTensorflow?.detect()
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }
//用于调用handLandmarkerHelper的方法对图像进行实时手势检测，传递相应的参数。
    private fun detectHand(imageProxy: ImageProxy) {
        handLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
    }
//这段代码的作用是在配置发生变化时更新图像分析器的目标旋转角度，以适应新的配置。这可能在处理相机预览或图像处理时特别有用，以确保图像在不同的设备方向或屏幕旋转角度下正确显示和处理。
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after hand have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    override fun onResults(
        resultBundle: HandLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
//                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
//                    String.format("%d ms", resultBundle.inferenceTime)
//                val regex = """"([^"]+)"""".toRegex()
//                if(resultBundle.results.first().handednesses().size>1){
//                    val matchResult = regex.find(resultBundle.results.first().handednesses()[0].toString())
//                    Log.e("ResultTest", "onResults: "+matchResult?.groupValues?.get(1))
//                }else if (resultBundle.results.first().handednesses().size>0){
//                    val matchResult = regex.find(resultBundle.results.first().handednesses()[0].toString())
//                    Log.e("ResultTest", "onResults: "+matchResult?.groupValues?.get(1) )
//                }else Log.e("ResultTest", "onResults: None" )
//                Log.e("ResultTest", String.format("ResultMessage:集合大小 %d 该结果大小 %d" ,resultBundle.results.size,resultBundle.results[0].handednesses().size) )
                // Pass necessary information to OverlayView for drawing on the canvas
                /** As the same, 我们可以在这里取到结果以便于识别，用来传输参数，来强制绘图*/
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )


                // Force a redraw
                // 强制重新绘图
                //不用可以删除
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            //大概用来将CPU换成GPU
//            if (errorCode == HandLandmarkerHelper.GPU_ERROR) {
//                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
//                    HandLandmarkerHelper.DELEGATE_CPU, false
//                )
//            }
        }
    }
}