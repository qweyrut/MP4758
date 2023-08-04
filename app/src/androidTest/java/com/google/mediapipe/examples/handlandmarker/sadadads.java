package com.google.mediapipe.examples.handlandmarker;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import org.jblas.DoubleMatrix;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

//public class sadadads {
//    @Override
//    public int hashCode() {
//        HandLandmarkerResult results;
//        DoubleMatrix lh = new DoubleMatrix(21, 3);
//        if (results.landmarks()!= null) {
//            for (int i = 0; i < 21; i++) {
//                NormalizedLandmark re = (NormalizedLandmark) results.landmarks();
//                lh.put(i, 0, re.x());
//                lh.put(i, 1, re.y());
//                lh.put(i, 2, re.z());
//            }
//        } else {
//            lh = DoubleMatrix.zeros(21, 3);
//        }
//        double[] lh_flat = lh.data;
//    }
//
//
//}
