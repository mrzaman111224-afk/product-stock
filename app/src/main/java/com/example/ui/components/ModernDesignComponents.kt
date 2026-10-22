package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Product
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.BanglalinkOrangeDark
import com.example.ui.theme.BanglalinkOrangeLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PrantejDeepEmerald
import com.example.ui.theme.PrantejEmeraldBright
import com.example.ui.theme.PrantejGold
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

/**
 * Gen-Z Spring Press Effect Modifier
 * Gives a tactile bouncy feel when tapping cards and buttons.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = this.pointerInput(Unit) {
    var isPressed by mutableStateOf(false)
    while (true) {
        awaitPointerEventScope {
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true
            val upOrCancel = waitForUpOrCancellation()
            isPressed = false
            if (upOrCancel != null) {
                onClick()
            }
        }
    }
}

/**
 * Animated scale click wrapper
 */
@Composable
fun BouncyCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BouncyCardScale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val upOrCancel = waitForUpOrCancellation()
                        isPressed = false
                        if (upOrCancel != null) {
                            onClick()
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

/**
 * Pulsing live cloud sync status indicator
 */
@Composable
fun PulsingStatusDot(
    modifier: Modifier = Modifier,
    color: Color = PrantejEmeraldBright
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Helper to map product types/names to realistic generated visual assets
 */
object ProductAssetResolver {
    fun getDrawableForProduct(product: Product): Int {
        val nameLower = product.name.lowercase()
        val catLower = product.category.lowercase()

        return when {
            nameLower.contains("phone") || catLower.contains("phone") || nameLower.contains("smartphone") || nameLower.contains("mobile") ->
                R.drawable.prod_flagship_phone_1792690428158
            nameLower.contains("watch") || catLower.contains("watch") || nameLower.contains("smartwatch") ->
                R.drawable.prod_smart_watch_1792690441179
            nameLower.contains("earbud") || nameLower.contains("audio") || nameLower.contains("headphone") || catLower.contains("audio") ->
                R.drawable.prod_earbuds_case_1792690455207
            nameLower.contains("charger") || nameLower.contains("cable") || nameLower.contains("gan") || catLower.contains("power") || catLower.contains("accessories") ->
                R.drawable.prod_fast_charger_1792690476604
            else -> {
                // Modulo based on SKU hash to distribute visually
                val hash = kotlin.math.abs(product.sku.hashCode()) % 4
                when (hash) {
                    0 -> R.drawable.prod_flagship_phone_1792690428158
                    1 -> R.drawable.prod_smart_watch_1792690441179
                    2 -> R.drawable.prod_earbuds_case_1792690455207
                    else -> R.drawable.prod_fast_charger_1792690476604
                }
            }
        }
    }
}

/**
 * Modern Exit Confirmation Dialog (Requirement 1)
 * Pops up when the user attempts to exit the application.
 * Offers "Exit" and "Cancel" (which closes the dialog leaving current state intact).
 */
@Composable
fun ExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning / Exit Icon with glowing gradient ring
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(BanglalinkOrange.copy(alpha = 0.2f), RoseDanger.copy(alpha = 0.15f))
                            )
                        )
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(BanglalinkOrange, RoseDanger)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Exit App Icon",
                        tint = BanglalinkOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Exit PRANTEJ CloudStock?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to exit the application? Your store inventory and active cashier session will remain completely safe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons Row (Exit & Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = onConfirmExit,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BanglalinkOrange
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Banglalink-style Modern Promo Banner Card
 */
@Composable
fun BanglalinkPromoHeroBanner(
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.promo_banner_smart_pos_1792690407728),
                contentDescription = "Smart Retail Promo Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic scrim gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.82f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Content text & CTA badge
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BanglalinkOrange)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "CLOUD SPEED POS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrantejDeepEmerald)
                            .border(1.dp, PrantejGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PRANTEJ OFFICIAL",
                            color = PrantejGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title and Subtitle
                Column {
                    Text(
                        text = "Instant 1-Tap POS Checkout",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time stock ledger, thermal bills & cloud audit sync",
                        color = Slate200,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
