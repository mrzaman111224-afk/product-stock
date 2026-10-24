package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.MonerRuby
import com.example.ui.theme.MonerGold
import com.example.ui.theme.MonerGoldLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val settings by viewModel.allSettings.collectAsState()
    val quickAdminLoginEnabled = settings.find { it.key == "quick_admin_login_enabled" }?.value == "true"
    val allowStaffRequests = settings.find { it.key == "allow_staff_requests" }?.value != "false"
    val publicShareUrl = settings.find { it.key == "public_share_url" }?.value
        ?: "https://ais-pre-a3omklob3sh7jfdsfqrb6o-760393657966.asia-northeast1.run.app"

    var selectedTab by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showStatusCheckDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate950, Slate900, Color(0xFF1E1B4B))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Icon & Brand Title
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(2.dp, MonerGoldLight.copy(alpha = 0.7f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.moner_upohar_logo),
                    contentDescription = "মনের উপহার Official Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "মনের উপহার",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "— প্রিয়জনের জন্য, হৃদয়ের উপহার —",
                style = MaterialTheme.typography.bodyMedium,
                color = MonerGoldLight,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Boutique Jewelry & Gift Store POS",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate800, Slate700)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tabs: Sign In / Staff Request
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Slate950,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = IndigoPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                                successMessage = null
                            },
                            text = { Text("Sign In", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                                successMessage = null
                            },
                            text = { Text("Staff Request", fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(visible = errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RoseDanger.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = RoseDanger)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFFB4AB),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = successMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldSecondary.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successMessage.orEmpty(),
                                    color = EmeraldLight,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (selectedTab == 1 && !allowStaffRequests) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "অনলাইন রেজিস্ট্রেশন বন্ধ আছে (Closed)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFCC00),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "স্টোর অ্যাডমিন কর্তৃক নতুন কর্মীদের স্ব-নিবন্ধন বন্ধ রাখা হয়েছে। আপনার একাউন্টের জন্য সরাসরি স্টোর অ্যাডমিনের সাথে যোগাযোগ করুন।",
                                    color = Slate200,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (selectedTab == 1 && allowStaffRequests) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Full Name (আপনার নাম)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate100,
                                focusedLabelColor = IndigoLight,
                                unfocusedLabelColor = Slate400
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (selectedTab == 0) "Account Email (ইমেইল)" else "Work Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate400) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Slate100,
                            focusedLabelColor = IndigoLight,
                            unfocusedLabelColor = Slate400
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (পাসওয়ার্ড)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = Slate400
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Slate100,
                            focusedLabelColor = IndigoLight,
                            unfocusedLabelColor = Slate400
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                if (selectedTab == 0) {
                                    val res = viewModel.authRepo.login(email, password)
                                    res.onFailure {
                                        errorMessage = it.message ?: "Login failed"
                                    }
                                } else {
                                    if (displayName.isBlank()) {
                                        errorMessage = "Please enter your full name."
                                        isLoading = false
                                        return@launch
                                    }
                                    val res = viewModel.authRepo.requestStaffAccount(email, displayName, password)
                                    res.onSuccess {
                                        successMessage = "আবেদন সফল হয়েছে! অ্যাডমিন অনুমোদন করার পর আপনি লগইন করতে পারবেন। (Awaiting Admin approval)"
                                        displayName = ""
                                        email = ""
                                        password = ""
                                    }.onFailure {
                                        errorMessage = it.message ?: "Request failed"
                                    }
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && (selectedTab == 0 || allowStaffRequests),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (selectedTab == 0) "Sign In to CloudStock" else "Submit Staff Request",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }

                    // Check approval status button
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { showStatusCheckDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "স্টাফ আবেদন স্ট্যাটাস দেখুন (Check Approval Status)",
                            fontSize = 12.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Optional 1-click admin bypass ONLY shown if explicit setting is enabled by Admin
                    if (quickAdminLoginEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Admin Bypass (Testing Mode):",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    viewModel.authRepo.quickLoginAs("admin@monerupohar.com")
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IndigoLight)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quick Admin Login", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🔒 সুরক্ষিত স্টোর: শুধুমাত্র অনুমোদিত অ্যাডমিন ও স্টাফ লগইন করতে পারবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Public App Link & Share Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.7f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate800, Slate700)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFFFF5B00), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GitHub Download & Public App",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF5B00).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "APK & Web",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5B00)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "অ্যাপটি অ্যান্ড্রয়েড মোবাইলে ব্যবহারের জন্য GitHub Releases থেকে লেটেস্ট APK ফাইল সরাসরি ডাউনলোড করতে পারেন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val githubRepoUrl = "https://github.com/mrzaman111224/Moner-Upohar"
                    val githubReleasesUrl = "https://github.com/mrzaman111224/Moner-Upohar/releases"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate950)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = githubReleasesUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = IndigoLight,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(githubReleasesUrl))
                                    Toast.makeText(context, "GitHub Releases ডাউনলোড লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "মনের উপহার (Moner Upohar) Download Link:\n$githubReleasesUrl")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share App Link"))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubReleasesUrl))
                                context.startActivity(browserIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5B00)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubRepoUrl))
                                context.startActivity(browserIntent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp), tint = Slate200)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GitHub Repo", fontSize = 12.sp, color = Slate200)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Boutique Store & Staff Terminal • মনের উপহার",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showStatusCheckDialog) {
        StatusCheckDialog(
            viewModel = viewModel,
            onDismiss = { showStatusCheckDialog = false }
        )
    }
}

@Composable
fun StatusCheckDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var checkEmail by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultColor by remember { mutableStateOf(Color.White) }
    var checking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = IndigoPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check Approval Status", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "আপনার রেজিস্টার্ড ইমেইল দিন। স্টোর অ্যাডমিন আপনার অ্যাকাউন্ট অনুমোদন করেছেন কিনা তা সাথে সাথে দেখতে পাবেন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = checkEmail,
                    onValueChange = { checkEmail = it },
                    label = { Text("Staff Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (resultText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(resultColor.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = resultText.orEmpty(),
                            color = resultColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (checkEmail.isBlank()) return@Button
                    scope.launch {
                        checking = true
                        val res = viewModel.authRepo.checkStaffStatus(checkEmail)
                        res.onSuccess { (name, status) ->
                            when (status) {
                                com.example.data.model.UserStatus.PENDING -> {
                                    resultText = "⏳ Waiting for admin approval (অনুমোদনের অপেক্ষায়)\n\n$name, আপনার আবেদনটি জমা রয়েছে। স্টোর অ্যাডমিন অনুমোদন করা মাত্রই আপনি লগইন করতে পারবেন।"
                                    resultColor = Color(0xFFD97706)
                                }
                                com.example.data.model.UserStatus.APPROVED -> {
                                    resultText = "✅ Approved (অনুমোদিত)\n\n$name, আপনার অ্যাকাউন্টটি অনুমোদিত! এখন আপনি পাসওয়ার্ড দিয়ে সরাসরি লগইন করতে পারবেন।"
                                    resultColor = Color(0xFF10B981)
                                }
                                com.example.data.model.UserStatus.REJECTED -> {
                                    resultText = "❌ Rejected (অনুরোধ প্রত্যাখ্যাত)\n\n$name, আপনার স্টাফ অ্যাকাউন্ট আবেদনটি অ্যাডমিন কর্তৃক বাতিল করা হয়েছে।"
                                    resultColor = Color(0xFFEF4444)
                                }
                                com.example.data.model.UserStatus.SUSPENDED -> {
                                    resultText = "⚠️ Suspended (স্থগিত)\n\n$name, আপনার অ্যাকাউন্টটি বর্তমানে স্থগিত রয়েছে।"
                                    resultColor = Color(0xFFEF4444)
                                }
                            }
                        }.onFailure {
                            resultText = "❌ ${it.message ?: "Account not found"}"
                            resultColor = Color(0xFFEF4444)
                        }
                        checking = false
                    }
                },
                enabled = !checking && checkEmail.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (checking) "Checking..." else "Check Status")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Close")
            }
        }
    )
}
