package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffManagementScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val settings by viewModel.allSettings.collectAsState()
    val enforceSingleAdmin = settings.find { it.key == "enforce_single_admin" }?.value != "false"

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var newStaffName by remember { mutableStateOf("") }
    var newStaffEmail by remember { mutableStateOf("") }
    var newStaffPassword by remember { mutableStateOf("") }
    var addStaffError by remember { mutableStateOf<String?>(null) }

    if (currentUser?.role != UserRole.ADMIN) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = RoseDanger,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Access Restricted (অননুমোদিত)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "শুধুমাত্র স্টোর অ্যাডমিন কর্মীদের অনুমোদন এবং পারমিশন পরিচালনা করতে পারবেন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val pendingUsers = users.filter { it.status == UserStatus.PENDING }
    val approvedUsers = users.filter { it.status != UserStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Staff & RBAC Administration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "১ জন মূল অ্যাডমিন নিয়ন্ত্রণ • অনুমতি ছাড়া কোনো প্রবেশাধিকার নেই",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    newStaffName = ""
                    newStaffEmail = ""
                    newStaffPassword = ""
                    addStaffError = null
                    showAddStaffDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pendingUsers.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AmberWarning)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "অনুমোদনের অপেক্ষায় (${pendingUsers.size} টি আবেদন)",
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "আপনার অনুমতি ছাড়া এই ব্যবহারকারীরা অ্যাপে প্রবেশ করতে পারবে না।",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                items(pendingUsers, key = { it.id }) { user ->
                    StaffUserCard(
                        user = user,
                        isCurrent = user.id == currentUser?.id,
                        enforceSingleAdmin = enforceSingleAdmin,
                        onApprove = { viewModel.approveStaff(user.id) },
                        onReject = { viewModel.rejectStaff(user.id) },
                        onSuspend = { viewModel.suspendStaff(user.id) },
                        onDelete = { viewModel.deleteUser(user.id) },
                        onToggleRole = {
                            val newRole = if (user.role == UserRole.ADMIN) UserRole.STAFF else UserRole.ADMIN
                            viewModel.changeRole(user.id, newRole)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "অনুমোদিত কর্মকর্তা ও কর্মচারী (${approvedUsers.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(approvedUsers, key = { it.id }) { user ->
                StaffUserCard(
                    user = user,
                    isCurrent = user.id == currentUser?.id,
                    enforceSingleAdmin = enforceSingleAdmin,
                    onApprove = { viewModel.approveStaff(user.id) },
                    onReject = { viewModel.rejectStaff(user.id) },
                    onSuspend = { viewModel.suspendStaff(user.id) },
                    onDelete = { viewModel.deleteUser(user.id) },
                    onToggleRole = {
                        val newRole = if (user.role == UserRole.ADMIN) UserRole.STAFF else UserRole.ADMIN
                        viewModel.changeRole(user.id, newRole)
                    }
                )
            }
        }
    }

    // Add Staff Modal Dialog
    if (showAddStaffDialog) {
        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Authorized Staff", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (addStaffError != null) {
                        Text(addStaffError.orEmpty(), color = RoseDanger, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "অ্যাডমিন সরাসরি স্টাফ যুক্ত করলে তিনি সঙ্গে সঙ্গে অনুমোদিত (Approved) হয়ে যাবেন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newStaffName,
                        onValueChange = { newStaffName = it },
                        label = { Text("Staff Full Name (কর্মীর নাম)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newStaffEmail,
                        onValueChange = { newStaffEmail = it },
                        label = { Text("Work Email (ইমেইল)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newStaffPassword,
                        onValueChange = { newStaffPassword = it },
                        label = { Text("Password (পাসওয়ার্ড)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStaffName.isBlank() || newStaffEmail.isBlank() || newStaffPassword.isBlank()) {
                            addStaffError = "সবগুলো তথ্য পূরণ করুন।"
                            return@Button
                        }
                        if (newStaffPassword.length < 4) {
                            addStaffError = "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে।"
                            return@Button
                        }

                        viewModel.createStaffDirectly(
                            email = newStaffEmail.trim(),
                            displayName = newStaffName.trim(),
                            passwordRaw = newStaffPassword.trim(),
                            role = UserRole.STAFF
                        ) {
                            showAddStaffDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Create Staff")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StaffUserCard(
    user: UserAccount,
    isCurrent: Boolean,
    enforceSingleAdmin: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onDelete: () -> Unit,
    onToggleRole: () -> Unit
) {
    val isPrimaryOwner = user.email == "admin@prantej.com" || user.id == "admin-primary-001"
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val statusColor = when (user.status) {
        UserStatus.APPROVED -> EmeraldSecondary
        UserStatus.PENDING -> AmberWarning
        UserStatus.SUSPENDED, UserStatus.REJECTED -> RoseDanger
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isPrimaryOwner) Color(0xFFD97706).copy(alpha = 0.2f) else if (user.role == UserRole.ADMIN) IndigoPrimary.copy(alpha = 0.15f) else EmeraldSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPrimaryOwner) Icons.Default.Shield else if (user.role == UserRole.ADMIN) Icons.Default.Security else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isPrimaryOwner) Color(0xFFD97706) else if (user.role == UserRole.ADMIN) IndigoPrimary else EmeraldSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isPrimaryOwner) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFD97706).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Primary Owner (মালিক)", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (isCurrent) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("(You)", color = IndigoPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (user.status) {
                            UserStatus.APPROVED -> "Approved"
                            UserStatus.PENDING -> "Pending"
                            UserStatus.SUSPENDED -> "Suspended"
                            UserStatus.REJECTED -> "Rejected"
                        },
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Role: ${user.role.name} • ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(user.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isPrimaryOwner && !isCurrent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (user.status == UserStatus.PENDING) {
                            Button(
                                onClick = onApprove,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onReject,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoseDanger)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontSize = 11.sp, color = RoseDanger)
                            }
                        } else if (user.status == UserStatus.APPROVED) {
                            if (!enforceSingleAdmin) {
                                OutlinedButton(
                                    onClick = onToggleRole,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (user.role == UserRole.ADMIN) "Make Staff" else "Make Admin",
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onSuspend,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Suspend", fontSize = 11.sp, color = RoseDanger)
                            }

                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseDanger, modifier = Modifier.size(16.dp))
                            }
                        } else if (user.status == UserStatus.SUSPENDED) {
                            Button(
                                onClick = onApprove,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Reactivate", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseDanger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Staff Account?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete the account for ${user.displayName} (${user.email})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
