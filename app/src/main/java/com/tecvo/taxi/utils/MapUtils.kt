package com.tecvo.taxi.utils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tecvo.taxi.constants.AppConstants
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
/**
 * Draws a radius circle around a location point on the map.
 *
 * @param center The center point of the circle (user's location)
 * @param radiusKm The radius in kilometers
 * @param fillColor The fill color of the circle (semi-transparent)
 * @param strokeColor The border color of the circle
 * @param strokeWidth The width of the border in pixels
 */
@Composable
fun RadiusCircle(
    center: LatLng,
    radiusKm: Float,
    fillColor: Color = Color(0x1A6495ED), // Very light blue (10% opacity)
    strokeColor: Color = Color(0x4D3333FF), // Light blue border (30% opacity)
    strokeWidth: Float = 2f
) {
    // Only show radius circle if feature flag is enabled
    if (AppConstants.MapFeatures.ENABLE_RADIUS_VISUALIZATION) {
        // Enhanced visual styling for better visibility
        val enhancedFillColor = Color(0x15FF5722) // Light orange with transparency
        val enhancedStrokeColor = Color(0x80FF5722) // Semi-transparent orange
        val enhancedStrokeWidth = 3f
        
        // Convert km to meters for the Circle component
        val radiusInMeters = radiusKm * 1000.0
        
        Circle(
            center = center,
            radius = radiusInMeters,
            fillColor = enhancedFillColor,
            strokeColor = enhancedStrokeColor,
            strokeWidth = enhancedStrokeWidth
        )
    } else {
        // Original basic circle
        val radiusInMeters = radiusKm * 1000.0
        Circle(
            center = center,
            radius = radiusInMeters,
            fillColor = fillColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
    }
}