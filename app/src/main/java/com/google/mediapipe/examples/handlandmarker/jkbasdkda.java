//fun iudaso( result: HandLandmarkerResult) {
//        PoseCoords.fill(0.0f)
//
//
//
//        var twoHnadsfloat: FloatArray//每一帧存储的坐标
//        if (result.landmarks().isNotEmpty()) {
//        Log.e("TestMessage", "initA: " +result.handednesses()[0][0].displayName())
//        /*判断是否为左，如果是，将第一只手写左手，在判断是否有第二只手，如果有，写入右手坐标*/
//        if (result.handednesses()[0][0].displayName().equals("Left")){
//        val leftHandLandmarks = result.landmarks()[0]
//        for (i in 0 until numKeyPoints) {
//        leftHandCoords[i * numCoords] = leftHandLandmarks[i].x()
//        leftHandCoords[i * numCoords + 1] = leftHandLandmarks[i].y()
//        leftHandCoords[i * numCoords + 2] = leftHandLandmarks[i].z()
//        }
//        if (result.landmarks().size > 1 && result.landmarks()[1].isNotEmpty()) {
//        val rightHandLandmarks = result.landmarks()[1]
//        for (i in 0 until numKeyPoints) {
//        rightHandCoords[i * numCoords] = rightHandLandmarks[i].x()
//        rightHandCoords[i * numCoords + 1] = rightHandLandmarks[i].y()
//        rightHandCoords[i * numCoords + 2] = rightHandLandmarks[i].z()
//        }
//        } else {
//        rightHandCoords.fill(0.0F)
//        }
//        }
//        /*判断是否为左，如果不是，将第一只手写右手，在判断是否有第二只手，如果有，写入左手坐标*/
//        else {
//        val rightHandLandmarks = result.landmarks()[0]
//        for (i in 0 until numKeyPoints) {
//        rightHandCoords[i * numCoords] = rightHandLandmarks[i].x()
//        rightHandCoords[i * numCoords + 1] = rightHandLandmarks[i].y()
//        rightHandCoords[i * numCoords + 2] = rightHandLandmarks[i].z()
//        }
//        if (result.landmarks().size > 1 && result.landmarks()[1].isNotEmpty()) {
//        val leftHandLandmarks = result.landmarks()[1]
//        for (i in 0 until numKeyPoints) {
//        leftHandCoords[i * numCoords] = leftHandLandmarks[i].x()
//        leftHandCoords[i * numCoords + 1] = leftHandLandmarks[i].y()
//        leftHandCoords[i * numCoords + 2] = leftHandLandmarks[i].z()
//        }
//        } else {
//        leftHandCoords.fill(0.0F)
//        }
//        }
//        //将两个数组拼接
//        twoHnadsfloat = leftHandCoords.plus(rightHandCoords)
//        //将两个含有双手坐标的数组写进handCoordinatesList集合里
//        handCoordinatesList.add(twoHnadsfloat)
//        //判断是否有50帧
//        if (handCoordinatesList.size == 50) {
//        //将集合转化为一维浮点数组
//        var twoHnadsfloat12 = handCoordinatesList.flatMap { it.toList() }.toFloatArray()
//
//        Log.e("TestMessage", "initA: " + twoHnadsfloat12.contentToString())
//        Log.e("TestMessage", "initB: " + twoHnadsfloat12.size)
//
//        handCoordinatesList.clear()
//        }
//        }
//
//        }