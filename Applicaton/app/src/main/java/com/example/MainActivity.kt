package com.example

import android.Manifest
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.zIndex
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import com.example.ui.components.CardCollect360Overlay
import com.example.ui.components.CollectedCartModalDialog
import com.example.ui.components.FloatingCartButton
import com.example.ui.components.TopSlideNotificationBanner
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MuseumViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MuseumViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()

                // Register real-time GPS tracker if permissions are granted
                LocationPermissionAndTracker(viewModel = viewModel)

                // Xử lý nút quay lại vật lý hoặc cử chỉ vuốt thông minh của Android
                BackHandler(enabled = state.currentScreen != AppScreen.Welcome && state.currentScreen !is AppScreen.Game) {
                    when (state.currentScreen) {
                        AppScreen.Welcome -> { /* Không phản hồi */ }
                        AppScreen.MuseumInfo, AppScreen.Login -> viewModel.navigateTo(AppScreen.Welcome)
                        AppScreen.Dashboard -> viewModel.navigateTo(AppScreen.Welcome)
                        is AppScreen.Game -> {}
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = state.currentScreen,
                            transitionSpec = {
                                if (targetState is AppScreen.Game || targetState is AppScreen.Dashboard) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut()
                                    )
                                } else {
                                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                }
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                is AppScreen.Welcome -> WelcomeScreen(viewModel = viewModel)
                                is AppScreen.MuseumInfo -> MuseumInfoScreen(viewModel = viewModel)
                                is AppScreen.Login -> LoginScreen(viewModel = viewModel)
                                is AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                                is AppScreen.Game -> GameScreen(artifactId = screen.artifactId, viewModel = viewModel)
                            }
                        }

                        // Top Slide-Down System Notification Banner Layer
                        TopSlideNotificationBanner(
                            message = state.topNotificationMessage,
                            type = state.topNotificationType,
                            onDismiss = { viewModel.dismissTopNotification() },
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 4.dp)
                                .zIndex(9999f)
                        )

                        // Floating Cart Button (Giỏ Hàng Mảnh Ảnh) at Bottom-Right
                        if (state.currentScreen is AppScreen.Dashboard || state.currentScreen is AppScreen.Game) {
                            val totalCollectedCount = state.collectedImages.values.sumOf { it.size }
                            FloatingCartButton(
                                itemCount = totalCollectedCount,
                                onClick = {
                                    viewModel.openCartDialog()
                                },
                                modifier = Modifier
                                    .align(androidx.compose.ui.Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 24.dp)
                            )
                        }

                        // 360-Degree Card Collect Animation Overlay Dialog
                        CardCollect360Overlay(
                            cardData = state.activeCollectedCard,
                            onDismiss = { viewModel.dismissCardCollectAnimation() }
                        )

                        // Collected Cart Modal Dialog
                        CollectedCartModalDialog(
                            isOpen = state.showCartDialog,
                            collectedMap = state.collectedImages,
                            artifacts = state.artifacts,
                            onDismiss = { viewModel.closeCartDialog() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionAndTracker(viewModel: MuseumViewModel) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Automatically request permissions on launch
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    if (permissionState.allPermissionsGranted) {
        DisposableEffect(Unit) {
            val locationClient = LocationServices.getFusedLocationProviderClient(context)
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                4000L // update every 4 seconds
            ).apply {
                setMinUpdateIntervalMillis(2000L)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val lastLoc = result.lastLocation
                    if (lastLoc != null) {
                        // Only feed real location if simulator/mock is disabled, or update it so that distances can be shown
                        viewModel.updateLocation(lastLoc.latitude, lastLoc.longitude)
                    }
                }
            }

            try {
                locationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (se: SecurityException) {
                android.util.Log.e("LocationTracker", "SecurityException requesting location updates", se)
            }

            onDispose {
                try {
                    locationClient.removeLocationUpdates(locationCallback)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}

