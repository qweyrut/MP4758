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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.mediapipe.examples.handlandmarker.R

private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class PermissionsFragment : Fragment() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    context,
                    "Permission request granted",
                    Toast.LENGTH_LONG
                ).show()
                navigateToCamera()
            } else {
                Toast.makeText(
                    context,
                    "Permission request denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
//这段代码的作用是在应用程序启动时检查是否授予了相机权限。如果已经授予了相机权限，将导航到相机界面；如果没有授予相机权限，将请求相机权限。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) -> {
                navigateToCamera()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }
//通过导航控制器将应用程序的当前界面导航到相机界面
    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(
                requireActivity(),
                R.id.fragment_container
            ).navigate(
                R.id.action_permissions_to_camera
            )
        }
    }
    //检查应用程序所需的所有权限是否已被授予
    companion object {

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
