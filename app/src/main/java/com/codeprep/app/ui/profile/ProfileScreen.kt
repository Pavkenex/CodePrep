package com.codeprep.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.navigation.SessionBootstrapViewModel
import com.codeprep.app.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: SessionBootstrapViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val progress by viewModel.currentUserProgress.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Charcoal,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Avatar with Glowing ElectricCyan Border
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow effect (simulated with shadow/border)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.1f))
                    .border(4.dp, ElectricCyan, CircleShape)
                    .shadow(elevation = 10.dp, shape = CircleShape, spotColor = ElectricCyan)
            )

            // Avatar Image Placeholder
            Box(
                modifier = Modifier
                    .size(112.dp) // Slightly smaller to show border
                    .clip(CircleShape)
                    .background(Charcoal), // Dark background for avatar placeholder
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = progress?.nickname?.take(1)?.uppercase() ?: "U",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                )
            }

            // Edit Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { /* TODO: Edit Profile */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Avatar",
                    tint = Charcoal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Username
        Text(
            text = progress?.nickname ?: "User",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. User Stats: XP, Level, League
        // Minimalist, high contrast (Cyan text on Charcoal cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeonStatCard(
                label = "XP",
                value = "${progress?.xp ?: 0}",
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                label = "Level",
                value = "${((progress?.xp ?: 0) / 100) + 1}", // Simple level calculation
                modifier = Modifier.weight(1f)
            )
            NeonStatCard(
                label = "League",
                value = "Bronze", // Placeholder
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Badges Placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
             Text(
                text = "Badges",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            )
        }
       
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                BadgePlaceholder(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(48.dp))

        // 4. Logout: System Shutdown
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Charcoal,
                contentColor = CardinalRed
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SYSTEM SHUTDOWN")
        }
    }
}

@Composable
fun NeonStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Charcoal)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        )
    }
}

@Composable
fun BadgePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(LockedGrey.copy(alpha = 0.5f))
            .border(1.dp, LockedGrey, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked Badge",
            tint = LockedGreyDark,
            modifier = Modifier.size(24.dp)
        )
    }
}
