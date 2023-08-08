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
package com.google.mediapipe.examples.handlandmarker

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.handlandmarker.databinding.ActivityMainBinding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
//    private val viewModel : MainViewModel by viewModels()
    fun doSomething(): AssetManager? {
        val assetManager = applicationContext.assets
         return assetManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {

        }



// 打开 action.tflite 文件并读取字节数据
        val assetManager = applicationContext.assets
        val modelInputStream = assetManager.open("model.tflite")
        val modelData = modelInputStream.readBytes()
// 将字节数据转换为 ByteBuffer 对象
        val modelBuffer = ByteBuffer.allocateDirect(modelData.size)
        modelBuffer.order(ByteOrder.nativeOrder())
        modelBuffer.put(modelData)
        modelBuffer.rewind()

        val tfliteModel = Interpreter( modelBuffer)
        val inputShape = intArrayOf(1,50,258)
        val inputBuffer = TensorBuffer.createFixedSize(tfliteModel.getInputTensor(0).shape(), tfliteModel.getInputTensor(0).dataType())
        val inputShape2 = intArrayOf(1,4)
        val outputBuffer = TensorBuffer.createFixedSize(inputShape2, tfliteModel.getInputTensor(0).dataType())
        var outputk = outputBuffer.buffer
        var twoHnadsfloat= FloatArray( 1*50*258)
        twoHnadsfloat.fill(0.5849898f)
        inputBuffer.loadArray(twoHnadsfloat)
        val byteBuffer = inputBuffer.buffer
        tfliteModel.run(byteBuffer,outputk )
        Log.e("TestMessage", "initB: " + outputk)
        var inputTensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1,4) , DataType.FLOAT32)
        inputTensorBuffer.loadBuffer(outputk)
        val result = outputk.toString()
        val inputArray = inputTensorBuffer.floatArray
        val inputArrayString = Arrays.toString(inputArray)

        val confidences: FloatArray = inputArray
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }
        val classes = arrayOf("爱国", "敬业", "诚信","友善")
        Log.e("TestMessage", "initC: " + inputArrayString)
        Log.e("TestMessage", "initA: " +classes[maxPos] )
    }

    override fun onBackPressed() {
       finish()
    }

}
