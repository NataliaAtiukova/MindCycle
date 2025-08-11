package com.app.mindcycle.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.app.mindcycle.R
import com.app.mindcycle.ads.YandexAdsManager
import com.yandex.mobile.ads.banner.BannerAdView

/**
 * Compose component for displaying Yandex banner ads
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { context ->
            BannerAdView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        modifier = modifier,
        update = { bannerAdView ->
            YandexAdsManager().setupBanner(
                bannerAdView = bannerAdView,
                onLoaded = onAdLoaded,
                onFailed = { error ->
                                                    onAdFailed?.invoke(error.description ?: "Ad failed to load")
                }
            )
        }
    )
} 