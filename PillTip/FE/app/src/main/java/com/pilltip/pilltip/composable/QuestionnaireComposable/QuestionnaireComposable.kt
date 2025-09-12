package com.pilltip.pilltip.composable.QuestionnaireComposable

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard

@Composable
fun InformationBox(
    header: String,
    headerColor: Color = Color.Black,
    headerSize: Int = 24,
    desc: String,
    image: Int = R.drawable.logo_pilltip_typo
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = header,
            fontSize = headerSize.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = headerColor,
        )
        HeightSpacer(12.dp)
        Text(
            text = desc,
            fontSize = 14.sp,
            lineHeight = 19.6.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = gray500
        )
        HeightSpacer(42.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(color = gray100, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(image),
                contentDescription = "설명 이미지"
            )
        }
    }
}

@Composable
fun DottedDivider(
    color: Color = Color(0xFFE2E4EC),
    thickness: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(thickness)
) {
    Canvas(modifier = modifier) {
        val lineY = size.height / 2
        val dashPx = dashLength.toPx()
        val gapPx = gapLength.toPx()
        var startX = 0f

        while (startX < size.width) {
            drawLine(
                color = color,
                start = Offset(x = startX, y = lineY),
                end = Offset(x = (startX + dashPx).coerceAtMost(size.width), y = lineY),
                strokeWidth = thickness.toPx()
            )
            startX += dashPx + gapPx
        }
    }
}

@Composable
fun InfoRow(
    title : String,
    desc: String
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray600,
                textAlign = TextAlign.Justify
            ),
            modifier = Modifier.width(49.dp)
        )
        WidthSpacer(20.dp)
        Text(
            text = desc,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
                textAlign = TextAlign.Justify,
            )
        )
    }
}

@Composable
fun <T> QuestionnaireToggleSection(
    title: String,
    items: List<T>,
    getName: (T) -> String,
    getSubmitted: (T) -> Boolean,
    onToggle: (Int) -> Unit,
    enable: Boolean = true
) {
    var expanded by remember { mutableStateOf(true) }
    val rotationDegree by animateFloatAsState(
        targetValue = if (expanded) 0f else 90f,
        animationSpec = tween(durationMillis = 300),
        label = "toggle_arrow_rotation"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray600
                )
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_questionnaire_downward_arrow),
                contentDescription = "접기/펼치기",
                modifier = Modifier.rotate(rotationDegree).noRippleClickable { expanded = !expanded }
            )
        }

        if (expanded) {
            HeightSpacer(8.dp)
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getName(item),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = gray800
                        )
                    )
                    if(enable){
                        IosButton(
                            checked = getSubmitted(item),
                            onCheckedChange = { onToggle(index) }
                        )
                    }
                }
                HeightSpacer(12.dp)
            }
        }
    }
}

@Composable
fun CameraPermissionWrapper(
    onGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            permissionGranted = isGranted
            if (!isGranted) {
                Toast.makeText(context, "카메라 권한이 필요해요", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionGranted = ContextCompat.checkSelfPermission(
            context,
            cameraPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            permissionLauncher.launch(cameraPermission)
        }
    }

    if (permissionGranted) {
        onGranted()
    }
}


@Composable
fun QrScannerEntry(onQrScanned: (String) -> Unit) {
    CameraPermissionWrapper {
        QrScannerPage(onQrScanned = onQrScanned)
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerPage(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scanned by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        if (!scanned) {
                            val mediaImage = imageProxy.image
                            val rotation = imageProxy.imageInfo.rotationDegrees
                            if (mediaImage != null) {
                                val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
                                barcodeScanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            barcode.rawValue?.let { value ->
                                                scanned = true
                                                onQrScanned(value)
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalyzer
                    )

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Box(
            modifier = Modifier.align(Alignment.Center)

        ) {
            QrCornerGuideBox()
        }
//        Column(
//            modifier = Modifier.align(Alignment.TopCenter).padding(top = 22.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ){
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ){
//                Image(
//                    imageVector = ImageVector.vectorResource(R.drawable.ic_main_qrcode),
//                    contentDescription = "QR"
//                )
//                Image(
//                    imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_typo),
//                    contentDescription = "필팁 로고",
//                    modifier = Modifier.height(25.dp)
//                )
//            }
//            Text(
//                text = "스마트 문진표 전송",
//                style = TextStyle(
//                    fontSize = 14.sp,
//                    fontFamily = pretendard,
//                    fontWeight = FontWeight(500),
//                    color = Color.White,
//                )
//            )
//        }

    }
}

@Composable
fun QrCornerGuideBox(
    size: Dp = 240.dp,
    lineLength: Dp = 24.dp,
    lineWidth: Dp = 6.dp,
    color: Color = Color.White,
    alpha: Float = 0.6f
) {
    Box(
        modifier = Modifier
            .size(size)
    ) {
        // 상단 왼쪽
        Box(
            modifier = Modifier
                .offset(0.dp, 0.dp)
                .size(lineLength, lineWidth)
                .background(color.copy(alpha))
        )
        Box(
            modifier = Modifier
                .offset(0.dp, 0.dp)
                .size(lineWidth, lineLength)
                .background(color.copy(alpha))
        )

        // 상단 오른쪽
        Box(
            modifier = Modifier
                .offset(x = size - lineLength, y = 0.dp)
                .size(lineLength, lineWidth)
                .background(color.copy(alpha))
        )
        Box(
            modifier = Modifier
                .offset(x = size - lineWidth, y = 0.dp)
                .size(lineWidth, lineLength)
                .background(color.copy(alpha))
        )

        // 하단 왼쪽
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = size - lineWidth)
                .size(lineLength, lineWidth)
                .background(color.copy(alpha))
        )
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = size - lineLength)
                .size(lineWidth, lineLength)
                .background(color.copy(alpha))
        )

        // 하단 오른쪽
        Box(
            modifier = Modifier
                .offset(x = size - lineLength, y = size - lineWidth)
                .size(lineLength, lineWidth)
                .background(color.copy(alpha))
        )
        Box(
            modifier = Modifier
                .offset(x = size - lineWidth, y = size - lineLength)
                .size(lineWidth, lineLength)
                .background(color.copy(alpha))
        )
    }
}


