package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImageSharingApp(viewModel: ImageSharingViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val imageList by viewModel.imageList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showUploadFormDialog by remember { mutableStateOf(false) }
    var selectedImageForDetail by remember { mutableStateOf<ImageItem?>(null) }

    // Radial and linear vibrant gradients for background to give that true glassmorphism atmosphere
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // deep slate blue
            Color(0xFF1E1B4B), // indigo twilight
            Color(0xFF311042)  // dark futuristic violet
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            floatingActionButton = {
                FloatingUploadButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.testTag("fab_upload_image")
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Panel (Title + Search)
                HeaderPanel(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) }
                )

                if (imageList.isEmpty()) {
                    EmptyStateView(
                        isSearching = searchQuery.isNotEmpty(),
                        onClearSearch = { viewModel.onSearchQueryChange("") }
                    )
                } else {
                    // Masonry / Staggered Gallery
                    GalleryGrid(
                        imageList = imageList,
                        onImageClick = { selectedImageForDetail = it },
                        onImageLongClick = { image ->
                            // Fast download option
                            downloadImage(context, image)
                        }
                    )
                }
            }
        }

        // 1. Password Dialog
        if (showPasswordDialog) {
            PasswordVerificationDialog(
                onDismiss = { showPasswordDialog = false },
                onCorrectPassword = {
                    showPasswordDialog = false
                    showUploadFormDialog = true
                },
                verifyPassword = { viewModel.verifyPassword(it) }
            )
        }

        // 2. Upload Form Dialog
        if (showUploadFormDialog) {
            UploadFormDialog(
                uploadStatus = uploadStatus,
                onDismiss = {
                    showUploadFormDialog = false
                    viewModel.clearUploadStatus()
                },
                onUpload = { title, uri ->
                    viewModel.uploadImage(title, uri)
                }
            )
        }

        // 3. Full Screen Detail Viewer
        AnimatedVisibility(
            visible = selectedImageForDetail != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            selectedImageForDetail?.let { image ->
                FullScreenDetailViewer(
                    image = image,
                    onDismiss = { selectedImageForDetail = null },
                    onDownload = {
                        downloadImage(context, image)
                    },
                    onDelete = {
                        viewModel.deleteImage(image)
                        selectedImageForDetail = null
                        Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderPanel(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // App Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF818CF8), Color(0xFFF472B6))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Logo icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "LUMINA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Responsive Glassmorphic Gallery",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Search Bar (Glassmorphism design)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Search images by title...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF818CF8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryGrid(
    imageList: List<ImageItem>,
    onImageClick: (ImageItem) -> Unit,
    onImageLongClick: (ImageItem) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .testTag("image_staggered_grid"),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp
    ) {
        items(imageList, key = { it.id }) { image ->
            // Image card aspect ratio derived dynamically to replicate masonry beautifully
            val imageRatio = remember(image.id) {
                val ratio = image.width.toFloat() / image.height.toFloat()
                // clamp aspect ratios to avoid extreme values
                ratio.coerceIn(0.6f, 1.6f)
            }

            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "press_scale"
            )

            // Dynamic continuous float and breathe animations for each photo to feel alive like an animated web app
            val baseDuration = remember(image.id) { 3200 + (image.id % 4) * 450 }
            val animDelay = remember(image.id) { (image.id % 3) * 250 }
            val infiniteTransition = rememberInfiniteTransition(label = "image_float_infinite_${image.id}")
            
            val floatY by infiniteTransition.animateFloat(
                initialValue = -4f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = baseDuration, delayMillis = animDelay, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float_offset_y"
            )

            val breathingScale by infiniteTransition.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = baseDuration + 600, delayMillis = animDelay, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breath_scale"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = floatY.dp)
                    .scale(scale * breathingScale)
                    .combinedClickable(
                        onClick = { onImageClick(image) },
                        onLongClick = { onImageLongClick(image) }
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .testTag("image_card_${image.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = image.filePath,
                        contentDescription = image.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageRatio)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Bottom glass header info overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = image.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = image.uploadDate,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingUploadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF818CF8), Color(0xFFEC4899))
                ),
                CircleShape
            )
            .clickable(onClick = onClick)
            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Upload Image",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun PasswordVerificationDialog(
    onDismiss: () -> Unit,
    onCorrectPassword: () -> Unit,
    verifyPassword: (String) -> Boolean
) {
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Glassmorphic dialog canvas
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1E1B4B).copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock key icon",
                        tint = Color(0xFFF472B6),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Upload Auth Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Please enter the admin password to unlock image uploading privileges.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    placeholder = {
                        Text(
                            "Enter password",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility", tint = Color.White.copy(alpha = 0.6f))
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFF818CF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input_field")
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Incorrect Password",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("password_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (verifyPassword(password)) {
                                onCorrectPassword()
                            } else {
                                showError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF818CF8)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_password_button")
                    ) {
                        Text("Verify", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun UploadFormDialog(
    uploadStatus: UploadStatus,
    onDismiss: () -> Unit,
    onUpload: (String, Uri) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Media Picker launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                localErrorMsg = null
            }
        }
    )

    // Automatically close form when successfully uploaded
    LaunchedEffect(uploadStatus) {
        if (uploadStatus is UploadStatus.Success) {
            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF0F172A).copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Form Header
                Text(
                    text = "Upload New Image",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Only image formats under 10MB are allowed.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Image Picker Box / Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable {
                            pickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("upload_image_picker"),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected image preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Tiny change overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Change Image", color = Color.White, fontSize = 11.sp)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Cloud upload icon",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Select Image from Gallery",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Supports JPG, PNG, WEBP",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Image Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            "Image Title (Optional)",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEC4899),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = Color(0xFFEC4899)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("image_title_input")
                )

                // Error feedback
                val errorMsg = when {
                    localErrorMsg != null -> localErrorMsg
                    uploadStatus is UploadStatus.Error -> (uploadStatus as UploadStatus.Error).message
                    else -> null
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMsg,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (selectedImageUri == null) {
                                localErrorMsg = "Please pick an image first."
                            } else {
                                onUpload(title, selectedImageUri!!)
                            }
                        },
                        enabled = uploadStatus !is UploadStatus.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_image_submit_button")
                    ) {
                        if (uploadStatus is UploadStatus.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Upload Now", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenDetailViewer(
    image: ImageItem,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Zoomable/Full Scale Image
            AsyncImage(
                model = image.filePath,
                contentDescription = image.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
                    .padding(24.dp),
                contentScale = ContentScale.Fit
            )

            // Top Menu Overlay (Dismiss + Actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back/Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Delete & Download Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("delete_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete image",
                            tint = Color(0xFFEF4444)
                        )
                    }

                    // Download Button
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("download_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download image",
                            tint = Color(0xFF4ADE80)
                        )
                    }
                }
            }

            // Bottom Meta Details Overlay (Title, Date, Dimensions, Size)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = image.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Uploaded on ${image.uploadDate}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    MetaItem(
                        label = "Dimensions",
                        value = "${image.width} × ${image.height} px"
                    )
                    MetaItem(
                        label = "File Size",
                        value = formatFileSize(image.sizeInBytes)
                    )
                }
            }
        }
    }
}

@Composable
fun MetaItem(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStateView(
    isSearching: Boolean,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Empty art",
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearching) "No matching images found" else "Your Gallery is Empty",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isSearching) "Try adjusting your search filters or clear the search query to view all images." else "Click the floating '+' button in the bottom right corner to upload your first image.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        if (isSearching) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear Search", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "Unknown"
    val kb = bytes / 1024f
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024f
    return String.format("%.1f MB", mb)
}

private fun downloadImage(context: Context, image: ImageItem) {
    val sourceFile = File(image.filePath)
    if (!sourceFile.exists()) {
        Toast.makeText(context, "Error: Source file does not exist", Toast.LENGTH_SHORT).show()
        return
    }

    val handler = android.os.Handler(context.mainLooper)

    // Run on background thread
    Thread {
        try {
            val fileName = "download_${System.currentTimeMillis()}.png"
            val mimeType = "image/png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LuminaGallery")
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(output)
                        }
                    }
                    handler.post {
                        Toast.makeText(context, "Saved successfully to Pictures/LuminaGallery!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    handler.post {
                        Toast.makeText(context, "Download failed to create destination URI.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Legacy devices
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appPicturesDir = File(picturesDir, "LuminaGallery")
                if (!appPicturesDir.exists()) {
                    appPicturesDir.mkdirs()
                }
                val destFile = File(appPicturesDir, fileName)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                handler.post {
                    Toast.makeText(context, "Saved successfully to: ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Download", "Error saving image", e)
            handler.post {
                Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}
