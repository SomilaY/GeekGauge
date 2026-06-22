package com.somila.geekgauge.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.somila.geekgauge.domain.enums.UserRole
import com.somila.geekgauge.navigation.navbar.BottomNavItem
import com.somila.geekgauge.ui.theme.accentColor
import com.somila.geekgauge.ui.theme.backgroundColor
import com.somila.geekgauge.ui.theme.primaryColor

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.TRAINER) }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                when (state.user.role) {
                    UserRole.TRAINER -> navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                    UserRole.GEEK -> navController.navigate("geekDashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )

        Box(
            modifier = Modifier
                .size(420.dp)
                .offset(x = (-120).dp, y = (-120).dp)
                .clip(CircleShape)
                .background(primaryColor)
        )

        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 120.dp)
                .clip(CircleShape)
                .background(primaryColor)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isSignUp) "Fill in your details to get started"
                    else "Enter your details below",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )

                Spacer(Modifier.height(16.dp))

                // Name fields — sign up only
                AnimatedVisibility(visible = isSignUp) {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("First Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = loginState !is LoginState.Loading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    cursorColor = primaryColor
                                )
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Last Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = loginState !is LoginState.Loading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    cursorColor = primaryColor
                                )
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Role selector
                        Text(
                            text = "I am a...",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RoleChip(
                                label = "Trainer",
                                selected = selectedRole == UserRole.TRAINER,
                                onClick = { selectedRole = UserRole.TRAINER },
                                modifier = Modifier.weight(1f)
                            )
                            RoleChip(
                                label = "Geek",
                                selected = selectedRole == UserRole.GEEK,
                                onClick = { selectedRole = UserRole.GEEK },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = loginState !is LoginState.Loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = loginState !is LoginState.Loading,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                if (passwordVisible) "Hide" else "Show",
                                color = primaryColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    )
                )

                AnimatedVisibility(visible = loginState is LoginState.Error) {
                    Text(
                        text = (loginState as? LoginState.Error)?.message ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.register(
                                firstName, lastName, email, password, selectedRole
                            )
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = loginState !is LoginState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    )
                ) {
                    if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isSignUp) "Create Account" else "Login")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Toggle between login and sign up
                TextButton(
                    onClick = {
                        isSignUp = !isSignUp
                        viewModel.resetState()
                        firstName = ""
                        lastName = ""
                        email = ""
                        password = ""
                    }
                ) {
                    Text(
                        text = if (isSignUp)
                            "Already have an account? Log in"
                        else
                            "Don't have an account? Sign up",
                        color = primaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) primaryColor else primaryColor.copy(alpha = 0.3f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected)
                primaryColor.copy(alpha = 0.08f)
            else
                Color.Transparent
        )
    ) {
        Text(
            text = label,
            color = primaryColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}