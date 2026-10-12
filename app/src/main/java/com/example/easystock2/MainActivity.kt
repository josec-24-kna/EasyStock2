package com.example.easystock2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.easystock2.auth.presentation.AuthScreen
import com.example.easystock2.auth.presentation.AuthViewModel
import com.example.easystock2.auth.presentation.AuthViewModelFactory
import com.example.easystock2.core.database.AppDatabase
import com.example.easystock2.inventory.presentation.InventoryScreen
import com.example.easystock2.inventory.presentation.InventoryViewModel
import com.example.easystock2.inventory.presentation.InventoryViewModelFactory
import com.example.easystock2.inventory.presentation.ScannerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {

                        // --- RUTA 1: LOGIN ---
                        composable("login") {
                            val authViewModel: AuthViewModel = viewModel(
                                factory = AuthViewModelFactory(database.userDao())
                            )
                            val loggedInUserId by authViewModel.loggedInUserId.collectAsState()

                            // Escuchamos el estado reactivo del ID de usuario
                            LaunchedEffect(loggedInUserId) {
                                loggedInUserId?.let { id ->
                                    // Navegamos al inventario borrando el login del historial (botón atrás)
                                    navController.navigate("inventory/$id") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }

                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {} // Puedes eliminar este parámetro de tu AuthScreen si lo deseas
                            )
                        }

                        // --- RUTA 2: INVENTARIO ---
                        composable(
                            route = "inventory/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable

                            val inventoryViewModel: InventoryViewModel = viewModel(
                                factory = InventoryViewModelFactory(database.inventoryDao(), userId)
                            )

                            InventoryScreen(
                                viewModel = inventoryViewModel,
                                onNavigateToScanner = { navController.navigate("scanner/$userId") },
                                onNavigateToAi = { navController.navigate("ai/$userId") } // <- NUEVO
                            )
                        }

                        // --- RUTA 3: ESCÁNER QR ---
                        composable(
                            route = "scanner/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
                            val context = androidx.compose.ui.platform.LocalContext.current

                            val inventoryViewModel: InventoryViewModel = viewModel(
                                factory = InventoryViewModelFactory(database.inventoryDao(), userId)
                            )

                            ScannerScreen(
                                onQrScanned = { qrData ->
                                    inventoryViewModel.processScannedQr(
                                        qrData = qrData,
                                        onSuccess = {
                                            navController.popBackStack()
                                        },
                                        onError = { errorMessage ->
                                            android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                        // --- RUTA 4: IA GEMINI ---
                        composable(
                            route = "ai/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable

                            val aiViewModel: com.example.easystock2.ai.presentation.AiViewModel = viewModel(
                                factory = com.example.easystock2.ai.presentation.AiViewModelFactory(database.inventoryDao(), userId)
                            )

                            com.example.easystock2.ai.presentation.RecommendationsScreen(viewModel = aiViewModel)
                        }
                    }
                }
            }
        }
    }
}
