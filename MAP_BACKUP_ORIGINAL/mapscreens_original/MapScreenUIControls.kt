package com.example.taxi.screens.mapscreens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.taxi.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
class MapScreenUIControls(
    private val coroutineScope: CoroutineScope,
    private val cameraPositionState: CameraPositionState
) {
    private val tag = "MapScreenUIControls"
    
    fun recenterMap(location: LatLng?, defaultZoom: Float) {
        location?.let { targetLocation ->
            coroutineScope.launch {
                try {
                    // Performance optimization: move CameraUpdateFactory to background thread with error handling
                    val cameraUpdate = withContext(Dispatchers.Default) {
                        try {
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(targetLocation, defaultZoom)
                            )
                        } catch (e: Exception) {
                            Timber.tag(tag).w(e, "CameraUpdateFactory not ready for recenter")
                            return@withContext null
                        }
                    }
                    
                    // Only proceed if camera update was created successfully
                    if (cameraUpdate == null) {
                        Timber.tag(tag).d("Skipping recenter - camera not ready")
                        return@launch
                    }
                    
                    // Animate on main thread
                    withContext(Dispatchers.Main) {
                        cameraPositionState.animate(cameraUpdate, 400) // Faster animation
                    }
                    
                    Timber.tag(tag).d("Map recentered to: ${targetLocation.latitude}, ${targetLocation.longitude}")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Error recentering map: ${e.message}")
                }
            }
        }
    }
}
@Composable
fun MapTypeToggleButton(
    mapType: MapType,
    onToggle: () -> Unit,
    cornerRadius: Dp,
    textSize: TextUnit,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(cornerRadius)
            )
    ) {
        val buttonText = if (mapType == MapType.NORMAL)
            "Hybrid" else "Normal"
        Text(
            text = buttonText,
            modifier = Modifier
                .clickable { onToggle() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.Black,
            fontSize = textSize,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun RecenterMapButton(
    onClick: () -> Unit,
    cornerRadius: Dp,
    iconSize: Dp,

backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .size(iconSize)
            .background(
                backgroundColor,
                RoundedCornerShape(cornerRadius)
            )
            .padding(4.dp) // This should match centerMapPadding from original
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.currentlocation),
            contentDescription = "Re-center Map",
            tint = Color.Unspecified,
            modifier = Modifier.size(iconSize * 0.8f)
        )
    }
}
@Composable
fun MapControlsRow(
    mapType: MapType,
    onMapTypeToggle: () -> Unit,
    onRecenterMap: () -> Unit,
    cornerRadius: Dp,
    mapButtonTextSize: TextUnit,
    mapIconSize: Dp,
    controlsBackgroundAlpha: Float,
    smallSpacing: Dp = 10.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = smallSpacing, start = 10.dp, end = 10.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
// Map type toggle button
        MapTypeToggleButton(
            mapType = mapType,
            onToggle = onMapTypeToggle,
            cornerRadius = cornerRadius,
            textSize = mapButtonTextSize,
            backgroundColor = Color.White.copy(alpha = controlsBackgroundAlpha)
        )
// Re-center map button
        RecenterMapButton(
            onClick = onRecenterMap,
            cornerRadius = cornerRadius,
            iconSize = mapIconSize,
            backgroundColor = Color.White.copy(alpha = controlsBackgroundAlpha)
        )
    }
}
@Composable
fun rememberMapScreenUIControls(
    mapState: MapScreenState,
    cameraPositionState: CameraPositionState
): MapScreenUIControls {
    val coroutineScope = rememberCoroutineScope()
    return remember(mapState, cameraPositionState, coroutineScope) {
        MapScreenUIControls(coroutineScope, cameraPositionState)
    }
}