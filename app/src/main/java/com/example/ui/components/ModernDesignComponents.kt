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
import com.example.ui.theme.MonerRuby
import com.example.ui.theme.MonerGold
import com.example.ui.theme.MonerGoldLight
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.BanglalinkOrangeDark
import com.example.ui.theme.BanglalinkOrangeLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate950
import com.example.ui.theme.Slate900

import androidx.compose.ui.composed

/**
 * Gen-Z Spring Press Effect Modifier
 * Gives a tactile bouncy feel when tapping cards and buttons.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = this.composed {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BounceScale"
    )

    this
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
                        onClick?.invoke()
                    }
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
    color: Color = EmeraldSecondary
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
 * Helper to map product types/names and jewelry categories for Moner Upohar
 */
object ProductAssetResolver {
    val JEWELRY_CATEGORIES = listOf(
        "Necklace",
        "Bracelet",
        "Kashmiri Churi",
        "Finger Ring",
        "Earring",
        "Payel"
    )

    fun getCategoryBanglaName(category: String): String {
        return when (category.trim().lowercase()) {
            "necklace" -> "নেকলেস / গলার হার"
            "bracelet" -> "ব্রেসলেট"
            "kashmiri churi", "churi" -> "কাশ্মীরি চুড়ি"
            "finger ring", "ring" -> "আংটি"
            "earring", "earrings" -> "কানের দুল"
            "payel", "anklet" -> "পায়েল"
            else -> category
        }
    }

    fun getDrawableForCategory(category: String): Int {
        val cat = category.trim().lowercase()
        return when {
            cat.contains("necklace") || cat.contains("নেকলেস") || cat.contains("হার") -> R.drawable.cat_necklace
            cat.contains("bracelet") || cat.contains("ব্রেসলেট") -> R.drawable.cat_bracelet
            cat.contains("kashmiri") || cat.contains("churi") || cat.contains("চুড়ি") || cat.contains("চুড়ি") -> R.drawable.cat_kashmiri_churi
            cat.contains("ring") || cat.contains("আংটি") -> R.drawable.cat_finger_ring
            cat.contains("earring") || cat.contains("দুল") || cat.contains("ঝুমকা") || cat.contains("সুই") -> R.drawable.cat_earring
            cat.contains("payel") || cat.contains("পায়েল") || cat.contains("নূপুর") || cat.contains("anklet") -> R.drawable.cat_payel
            else -> R.drawable.moner_upohar_logo
        }
    }

    fun getDrawableForProduct(product: Product): Int {
        val nameLower = product.name.lowercase()
        val catLower = product.category.lowercase()
        return when {
            nameLower.contains("necklace") || nameLower.contains("clover") || nameLower.contains("নেকলেস") || nameLower.contains("হার") || catLower.contains("necklace") -> R.drawable.cat_necklace
            nameLower.contains("bracelet") || nameLower.contains("ব্রেসলেট") || nameLower.contains("bangle") || catLower.contains("bracelet") -> R.drawable.cat_bracelet
            nameLower.contains("kashmiri") || nameLower.contains("churi") || nameLower.contains("চুড়ি") || nameLower.contains("চুড়ি") || catLower.contains("churi") -> R.drawable.cat_kashmiri_churi
            nameLower.contains("ring") || nameLower.contains("আংটি") || catLower.contains("ring") -> R.drawable.cat_finger_ring
            nameLower.contains("earring") || nameLower.contains("দুল") || nameLower.contains("ঝুমকা") || nameLower.contains("সুই") || catLower.contains("earring") -> R.drawable.cat_earring
            nameLower.contains("payel") || nameLower.contains("পায়েল") || nameLower.contains("নূপুর") || nameLower.contains("anklet") || catLower.contains("payel") -> R.drawable.cat_payel
            else -> getDrawableForCategory(product.category)
        }
    }
}

/**
 * Modern Exit Confirmation Dialog
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
                            listOf(MonerRuby.copy(alpha = 0.2f), RoseDanger.copy(alpha = 0.15f))
                        )
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(MonerRuby, MonerGold)),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Exit App Icon",
                        tint = MonerRuby,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Exit মনের উপহার?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to exit the application? Your boutique store inventory and active cashier session will remain completely safe.",
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
                            containerColor = MonerRuby
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
 * Moner Upohar Boutique Promotional Hero Banner Card
 */
@Composable
fun BanglalinkPromoHeroBanner(
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.moner_upohar_logo),
                contentDescription = "মনের উপহার Official Logo Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim gradient overlay for crisp readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.92f),
                                Color.Black.copy(alpha = 0.72f),
                                Color.Black.copy(alpha = 0.40f)
                            )
                        )
                    )
            )

            // Content text & boutique tags
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonerRuby)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MonerGoldLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXCLUSIVE JEWELRY & GIFTS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, MonerGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "মনের উপহার",
                            color = MonerGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title and Subtitle with the authentic Bangla product description
                Column {
                    Text(
                        text = "মনের উপহার (Moner Upohar)",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "— প্রিয়জনের জন্য, হৃদয়ের উপহার —",
                        color = MonerGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Necklace • Bracelet • Finger Ring • Kashmiri Churi • Earring • Payel",
                        color = Slate200,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
