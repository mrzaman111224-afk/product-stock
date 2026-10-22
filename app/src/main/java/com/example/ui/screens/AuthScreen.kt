package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.Text
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
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, IndigoLight.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.prantej_logo),
                    contentDescription = "PRANTEJ Official Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PRANTEJ CloudStock",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Professional Store & Inventory Management",
                style = MaterialTheme.typography.bodyMedium,
                color = EmeraldLight,
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
                                    viewModel.authRepo.quickLoginAs("admin@prantej.com")
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
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পাবলিক লিংক ও ডাউনলোড (App Download Link)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "অ্যাপটি ফোনে ইনস্টল বা অনলাইনে ব্যবহারের জন্য নিচের লিংকটি ব্যবহার ও শেয়ার করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                            text = publicShareUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = IndigoLight,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(publicShareUrl))
                                    Toast.makeText(context, "ডাউনলোড লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "PRANTEJ CloudStock App: $publicShareUrl")
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "RBAC Security Protocol • PRANTEJ Technologies Ltd.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
