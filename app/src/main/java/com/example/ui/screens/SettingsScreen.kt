package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.activeProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val settings by viewModel.allSettings.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val publicShareUrl = settings.find { it.key == "public_share_url" }?.value
        ?: "https://ais-pre-a3omklob3sh7jfdsfqrb6o-760393657966.asia-northeast1.run.app"
    val quickAdminLoginEnabled = settings.find { it.key == "quick_admin_login_enabled" }?.value == "true"
    val allowStaffRequests = settings.find { it.key == "allow_staff_requests" }?.value != "false"
    val enforceSingleAdmin = settings.find { it.key == "enforce_single_admin" }?.value != "false"

    var storeName by remember { mutableStateOf("PRANTEJ Store") }
    var currency by remember { mutableStateOf("BDT (৳)") }
    var storeAddress by remember { mutableStateOf("PRANTEJ Commercial Center, Banani, Dhaka") }

    var showPasswordModal by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Store Settings & Security",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "পাবলিক লিংক, অ্যাডমিন সুরক্ষা, ডাটা ব্যাকআপ এবং এক্সেস কন্ট্রোল",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. PUBLIC APP & DOWNLOAD LINK CARD
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Public App & Download Link",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "এই পাবলিক লিংকের মাধ্যমে যে কেউ সরাসরি অ্যাপে ব্রাউজার দিয়ে প্রবেশ করতে পারবে বা মোবাইলের জন্য APK ডাউনলোড করতে পারবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = publicShareUrl,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPrimary,
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
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "PRANTEJ CloudStock Official App:\n$publicShareUrl")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share PRANTEJ App Link"))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = EmeraldSecondary, modifier = Modifier.size(18.dp))
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
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "PRANTEJ CloudStock Mobile App Download Link:\n$publicShareUrl")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Download Link"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Link", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(publicShareUrl))
                                context.startActivity(browserIntent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in Web", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 2. ADMIN ACCESS CONTROL & PROTECTION
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Admin Security & Protection (অ্যাডমিন সুরক্ষা)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "অ্যাডমিন অনুমতি ছাড়া কেউ প্রবেশ করতে পারবে না",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider()

                    // Toggle 1: Quick Admin Login Button on Public Login Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সবার জন্য ডিরেক্ট অ্যাডমিন লগইন (Bypass Button)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (quickAdminLoginEnabled) "সক্রিয়: লগইন পেইজে অ্যাডমিন বাটন দেখা যাবে (Testing Only)" else "নিষ্ক্রিয়: লগইন পেইজে কোনো অ্যাডমিন শর্টকাট নেই (সুরক্ষিত)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (quickAdminLoginEnabled) Color(0xFFD97706) else EmeraldSecondary
                            )
                        }
                        Switch(
                            checked = quickAdminLoginEnabled,
                            onCheckedChange = { isChecked ->
                                if (currentUser?.role == UserRole.ADMIN) {
                                    viewModel.saveSetting("quick_admin_login_enabled", if (isChecked) "true" else "false")
                                }
                            },
                            enabled = currentUser?.role == UserRole.ADMIN,
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                        )
                    }

                    Divider()

                    // Toggle 2: Allow Staff Registration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "নতুন কর্মীদের স্ব-নিবন্ধন (Staff Self-Request)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (allowStaffRequests) "চালু: নতুন স্টাফরা আবেদন পাঠাতে পারবে (অ্যাডমিন অনুমোদন আবশ্যক)" else "বন্ধ: শুধুমাত্র অ্যাডমিন নিজে স্টাফ যোগ করতে পারবে",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = allowStaffRequests,
                            onCheckedChange = { isChecked ->
                                if (currentUser?.role == UserRole.ADMIN) {
                                    viewModel.saveSetting("allow_staff_requests", if (isChecked) "true" else "false")
                                }
                            },
                            enabled = currentUser?.role == UserRole.ADMIN,
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                        )
                    }

                    Divider()

                    // Toggle 3: Strict 1-Admin Policy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict 1-Master Admin Policy (একক মালিকানা)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "স্টোরের মালিক শুধুমাত্র ১ জন মূল অ্যাডমিন। অন্য কাউকে অ্যাডমিন বানানো নিষিদ্ধ রাখা হয়েছে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enforceSingleAdmin,
                            onCheckedChange = { isChecked ->
                                if (currentUser?.role == UserRole.ADMIN) {
                                    viewModel.saveSetting("enforce_single_admin", if (isChecked) "true" else "false")
                                }
                            },
                            enabled = currentUser?.role == UserRole.ADMIN,
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                        )
                    }

                    Divider()

                    // Change Admin Password Button
                    if (currentUser?.role == UserRole.ADMIN) {
                        Button(
                            onClick = {
                                passwordError = null
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                showPasswordModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Master Admin Password (পাসওয়ার্ড পরিবর্তন)")
                        }
                    }
                }
            }
        }

        // 3. STORE PROFILE
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Store Profile (দোকানের তথ্য)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Brand Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = currentUser?.role == UserRole.ADMIN
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency Symbol") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = currentUser?.role == UserRole.ADMIN
                    )

                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text("Store Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = currentUser?.role == UserRole.ADMIN
                    )
                }
            }
        }

        // 4. DATABASE BACKUP & JSON EXPORT
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Database Snapshot Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "দোকানের সকল পণ্য, বিক্রয় লেনদেন এবং স্টক মুভমেন্ট লগ সম্পূর্ণ JSON ফাইলে ব্যাকআপ বা শেয়ার করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val nowStr = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.US).format(Date())
                            val backupSummary = """
                                {
                                  "exportDate": "$nowStr",
                                  "system": "PRANTEJ CloudStock Pro",
                                  "metrics": {
                                    "totalProducts": ${products.size},
                                    "totalTransactions": ${transactions.size},
                                    "totalAuditLogs": ${logs.size},
                                    "totalUsers": ${users.size}
                                  },
                                  "products": [
                                    ${products.joinToString(",\n") { "{\"sku\":\"${it.sku}\",\"name\":\"${it.name}\",\"stock\":${it.stock},\"price\":${it.sellingPrice}}" }}
                                  ],
                                  "transactions": [
                                    ${transactions.joinToString(",\n") { "{\"invoice\":\"${it.invoiceNumber}\",\"amount\":${it.totalAmount},\"date\":${it.timestamp}}" }}
                                  ]
                                }
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, backupSummary)
                                type = "application/json"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export PRANTEJ CloudStock JSON Backup"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate & Share JSON Backup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. ROLE & PERMISSION MATRIX
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Role & Permission Access Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val permissions = listOf(
                        "Point of Sale & Checkout" to ("Staff & Admin" to true),
                        "View Inventory & Products" to ("Staff & Admin" to true),
                        "Add New Catalog Products" to ("Staff & Admin" to true),
                        "Adjust & Restock Inventory" to ("Staff & Admin" to true),
                        "View Audit Movement Logs" to ("Staff & Admin" to true),
                        "Delete Products" to ("Admin Only" to false),
                        "Approve / Reject Staff Accounts" to ("Admin Only" to false),
                        "Suspend / Activate Staff Accounts" to ("Admin Only" to false),
                        "Change Store Settings & Password" to ("Admin Only" to false),
                        "Export Full Database Snapshots" to ("Admin Only" to false)
                    )

                    permissions.forEachIndexed { index, (perm, details) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(perm, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                details.first,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (details.second) EmeraldSecondary else IndigoLight
                            )
                        }
                        if (index < permissions.lastIndex) {
                            Divider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }

        // 6. RESET STORE TO ZERO (ADMIN ONLY)
        item {
            var showConfirmReset by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = RoseDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fresh Start / Reset to Zero", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoseDanger)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "সমস্ত পণ্য, স্টক সংখ্যা, বিক্রয় ও লগ মুছে ফেলে নতুন করে শূন্য (০) থেকে শুরু করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showConfirmReset = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset Store to 0 Items", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showConfirmReset) {
                AlertDialog(
                    onDismissRequest = { showConfirmReset = false },
                    title = { Text("Confirm Store Reset", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to clear all products and sales to start fresh at 0? Your admin login account will be preserved.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetAllStoreData()
                                showConfirmReset = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                        ) {
                            Text("Yes, Reset to 0")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmReset = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    // Modal to Change Admin Password
    if (showPasswordModal) {
        AlertDialog(
            onDismissRequest = { showPasswordModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Admin Password", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (passwordError != null) {
                        Text(passwordError.orEmpty(), color = RoseDanger, style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password (বর্তমান পাসওয়ার্ড)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (নতুন পাসওয়ার্ড)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password (পুনরায় লিখুন)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (oldPassword.isBlank() || newPassword.isBlank()) {
                            passwordError = "সবগুলো ফিল্ড পূরণ করুন।"
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            passwordError = "নতুন পাসওয়ার্ড দুটি মিলছে না।"
                            return@Button
                        }
                        if (newPassword.length < 4) {
                            passwordError = "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে।"
                            return@Button
                        }

                        viewModel.updateAdminPassword(oldPassword, newPassword) {
                            showPasswordModal = false
                            Toast.makeText(context, "অ্যাডমিন পাসওয়ার্ড সফলভাবে পরিবর্তিত হয়েছে!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Save Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
