package com.pilltip.pilltip.composable.SearchComposable

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.ReviewItem
import com.pilltip.pilltip.model.search.ReviewViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray300
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun InteractiveImageRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 32.dp,
    starSpacing: Dp = 4.dp,
    filledStarResId: Int = R.drawable.ic_review_star_fill, // 벡터 XML
    emptyStarResId: Int = R.drawable.ic_review_star_empty    // 벡터 XML
) {
    var containerWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val starWidth = containerWidth / maxRating
                    val touchedRating = ((offset.x / starWidth) + 1).toInt().coerceIn(1, maxRating)
                    onRatingChanged(touchedRating)
                }
            }
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width.toFloat()
            }
    ) {
        Row {
            for (i in 1..maxRating) {
                val resId = if (i <= rating) filledStarResId else emptyStarResId
                Image(
                    imageVector = ImageVector.vectorResource(id = resId),
                    contentDescription = "별 $i",
                    modifier = Modifier
                        .size(starSize)
                        .padding(end = if (i < maxRating) starSpacing else 0.dp)
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    size: Int = 18,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val starSize = size.dp
    val starSpacing = 2.dp

    Row(
        modifier = Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val iconTintColor = if (isSelected) primaryColor else gray200
            Icon(
                imageVector = ImageVector.vectorResource(if (isSelected) R.drawable.ic_review_star_fill else R.drawable.ic_review_star_empty),
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}

@Composable
fun ReviewRatingBar(
    title: String,
    total: Int,
    progress: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            ),
            modifier = Modifier.width(22.dp)
        )
        WidthSpacer(10.dp)
        LinearProgressIndicator(
            progress = if (total == 0) 0f else progress.div(total).toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = primaryColor,
            trackColor = gray200,
        )
        WidthSpacer(10.dp)
        Text(
            text = progress.toString(),
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            ),
            modifier = Modifier.width(30.dp)
        )
    }
}

@Composable
fun EfficiencyRow(
    title: String,
    description: String,
    percentage: Int,
    num: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray400
            ),
            modifier = Modifier.width(38.dp)
        )
        WidthSpacer(12.dp)
        Text(
            text = description,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray700
            )
        )
        WidthSpacer(20.dp)
        DottedDivider(
            color = gray200,
            modifier = Modifier.weight(1f)
        )
        WidthSpacer(20.dp)
        Text(
            text = "${if (num != 0) percentage / num * 100 else 0}%",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray700,
            ),
            modifier = Modifier.width(38.dp)
        )
        Text(
            text = "(${percentage}명)",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray400,
                textAlign = TextAlign.Right,
            ),
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun ReviewItemCard(
    review: ReviewItem,
    reviewViewModel: ReviewViewModel
) {
    var context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 24.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.userNickname,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            WidthSpacer(8.dp)
            StarRatingBar(
                rating = review.rating.toFloat(),
                size = 16
            ) {}
            WidthSpacer(6.dp)
            Text(
                text = "${review.rating}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            if (review.isMine == true) {
                Text(
                    text = "삭제",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        color = gray500,
                    ),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.noRippleClickable {
                        reviewViewModel.deleteReview(review.id, onSuccess = {
                            Toast.makeText(context, "삭제되었습니다. 새로고침 시 반영됩니다", Toast.LENGTH_SHORT)
                                .show()
                        })
                    }
                )
            }
        }
        HeightSpacer(6.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.gender,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray600,
                )
            )
            VerticalDivider(
                thickness = 1.dp,
                color = gray300,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = formatDateTime(review.createdAt),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray400,
                )
            )
        }
        HeightSpacer(16.dp)
        ReviewImageRow(review.imageUrls)
        HeightSpacer(16.dp)
        Text(
            text = review.content,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            )
        )
        //ExpandableText(text = review.content)
        HeightSpacer(16.dp)
        if (review.efficacyTags.isNotEmpty()) {
            Row {
                review.efficacyTags.forEach {
                    TagChip(label = it)
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = gray100,
            modifier = Modifier.padding(vertical = 18.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = if (review.isLiked)
                    ImageVector.vectorResource(R.drawable.btn_review_thumb_filled)
                else
                    ImageVector.vectorResource(R.drawable.btn_review_thumb),
                contentDescription = "좋아요",
                modifier = Modifier.noRippleClickable {
                    if (!review.isMine) {
                        reviewViewModel.likeReview(review.id)
                    }
                }
            )
            WidthSpacer(4.dp)
            Text(
                text = "${review.likeCount}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray400,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "신고하기",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray500,
                ),
                modifier = Modifier.noRippleClickable {
                    Toast.makeText(context, "신고가 접수되었습니다", Toast.LENGTH_SHORT).show()
                }
            )
        }

    }
}

fun formatDateTime(isoString: String): String {
    return try {
        val parsed = LocalDateTime.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm:ss")
        parsed.format(formatter)
    } catch (e: Exception) {
        isoString
    }
}


@Composable
fun TagChip(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isSelected) primaryColor050 else Color.White
    val borderColor = if (isSelected) primaryColor else gray300
    val textColor = if (isSelected) primaryColor else gray800

    Box(
        modifier = Modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(size = 1000.dp))
            .height(30.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 1000.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = textColor
            )
        )
    }
}

@Composable
fun ReviewImageRow(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) return
    var isImageViewerOpen by remember { mutableStateOf(false) }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(imageUrls.size) { index ->
            val bitmap = remember(imageUrls[index]) {
                base64ToBitmap(imageUrls[index])
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "리뷰 이미지",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .noRippleClickable {
                            isImageViewerOpen = true
                        },
                    contentScale = ContentScale.Crop
                )
                if (isImageViewerOpen) {
                    BitmapZoomableImageDialog(
                        bitmap = it,
                        onDismiss = { isImageViewerOpen = false }
                    )
                }
            }
        }
    }

}

fun base64ToBitmap(dataUri: String): Bitmap? {
    return try {
        val base64String = dataUri.substringAfter("base64,")
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ExpandableText(
    text: String,
    maxLength: Int = 300
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = remember(text, expanded) {
        if (expanded || text.length <= maxLength) {
            text
        } else {
            text.take(maxLength) + "..."
        }
    }

    Text(
        text = displayText,
        style = TextStyle(
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = gray800,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                if (text.length > maxLength) expanded = !expanded
            }
    )
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row {
        for (i in 1..5) {
            val filledRatio = (rating - (i - 1)).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(i.toFloat()) }
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_review_star_empty),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )

                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_review_star_fill),
                    contentDescription = "별점",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RectangleShape)
                        .graphicsLayer {
                            clip = true
                            shape = RectangleShape
                        }
                        .drawWithContent {
                            clipRect(right = size.width * filledRatio) {
                                this@drawWithContent.drawContent()
                            }
                        }
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSection(
    title: String,
    tags: List<String>,
    selectedTags: MutableList<String>
) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = gray800,
        ),
        modifier = Modifier.padding(vertical = 6.dp)
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            val isSelected = tag in selectedTags
            TagChip(
                label = tag,
                isSelected = isSelected,
                onClick = {
                    if (isSelected) selectedTags.remove(tag)
                    else selectedTags.add(tag)
                }
            )
        }
    }
}

@Composable
fun ReviewPhotoPicker(
    maxCount: Int = 4,
    onImagesChanged: (List<Uri>) -> Unit = {}
) {
    val context = LocalContext.current
    val currentApi = Build.VERSION.SDK_INT
    val imageUris = remember { mutableStateListOf<Uri>() }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageViewerOpen by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data
        val uri = data?.data

        when {
            uri != null -> {
                if (imageUris.size < maxCount) {
                    imageUris.add(uri)
                    onImagesChanged(imageUris)
                }
            }

            cameraImageUri != null -> {
                imageUris.add(cameraImageUri!!)
                onImagesChanged(imageUris)

                val path = getRealPathFromUri(context, cameraImageUri!!)
                path?.let {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(it),
                        arrayOf("image/jpeg"),
                        null
                    )
                }
            }
        }
    }

    fun showImagePicker() {
        val cameraFileUri = createImageUri(context)
        cameraImageUri = cameraFileUri

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri)
        }
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooser = Intent.createChooser(galleryIntent, "이미지 선택 또는 촬영")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        imageLauncher.launch(chooser)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            showImagePicker()
        } else {
            Toast.makeText(context, "카메라 및 갤러리 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (imageUris.size < maxCount) {
            Box(
                modifier = Modifier
                    .border(1.dp, gray200, RoundedCornerShape(10.dp))
                    .background(gray050, RoundedCornerShape(10.dp))
                    .size(85.dp)
                    .noRippleClickable {
                        val permissions = mutableListOf(
                            Manifest.permission.CAMERA,
                            getGalleryPermission(currentApi)
                        )
                        permissionLauncher.launch(permissions.toTypedArray())
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_camera),
                        contentDescription = "사진 추가"
                    )
                    Text(
                        text = "사진 추가",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }

        imageUris.forEachIndexed { index, uri ->
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .noRippleClickable {
                            isImageViewerOpen = true
                        }
                )
                if (isImageViewerOpen) {
                    ZoomableImageDialog(
                        imageUrl = uri.toString(),
                        onDismiss = { isImageViewerOpen = false }
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-5).dp, y = 5.dp)
                        .size(20.dp)
                        .background(Color(0x66000000), shape = CircleShape)
                        .noRippleClickable {
                            imageUris.removeAt(index)
                            onImagesChanged(imageUris)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

fun getGalleryPermission(api: Int): String {
    return if (api >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE
}

fun createImageUri(context: Context): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
}

fun getRealPathFromUri(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        if (cursor.moveToFirst()) {
            return cursor.getString(columnIndex)
        }
    }
    return null
}


@Composable
fun ReviewTextField(
    nickname: String,
    text: String,
    onTextChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    BasicTextField(
        value = text,
        onValueChange = {
            if (it.length <= 200) onTextChange(it)
        },
        modifier = Modifier
            .border(width = 1.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.5.dp)
            .fillMaxWidth()
            .height(183.dp)
            .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 16.dp)
            .focusRequester(focusRequester),
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight.W400,
            color = Color.Black
        ),
        cursorBrush = SolidColor(primaryColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "${nickname}님의 솔직한 후기를 남겨주세요!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.W400,
                            color = gray400
                        )
                    )
                }
                innerTextField()
            }
        }
    )

}

