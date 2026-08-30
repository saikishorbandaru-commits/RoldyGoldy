package com.roldygoldy.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

val WineDark = Color(0xFF1E0511)
val WineVelvet = Color(0xFF420822)
val WineRich = Color(0xFF6B1139)
val GoldPure = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF9E8B2)
val GoldDeep = Color(0xFF8C6D1F)
val IvorySilky = Color(0xFFFAF7F2)
val EmeraldPrestige = Color(0xFF0F382C)
val EmeraldSoft = Color(0xFFD8EDE5)

data class Product(val id: String, val name: String, val category: String, val price: Double, val originalPrice: Double, val emoji: String, val isTrialEligible: Boolean, val description: String)
data class ChatMessage(val id: String, val senderId: String, val text: String, val timestamp: String, val counterAmount: Double? = null)
data class CartItem(val product: Product, var quantity: Int = 1)
data class ExchangeQuote(val weightGrams: Double, val estimatedCredit: Double, val otp: String = "7734")

val sampleProducts = listOf(
    Product("1", "Sabyasachi-inspired Kundan Choker", "Bridal Studio", 3499.0, 4299.0, "👑", true, "1-Gram matte gold finish · Kundan stone work"),
    Product("2", "Korean Minimal Hoops", "Daily Wear", 349.0, 499.0, "💫", false, "18k PVD gold coated · Daily waterproof wear"),
    Product("3", "Temple Jewellery Set", "Temple Hub", 1299.0, 1799.0, "🪔", true, "Antique micro-gold plating · Kemp stones"),
    Product("4", "Polki Bridal Maangtikka", "Bridal Studio", 2899.0, 3499.0, "💍", false, "Handcrafted uncut polki foil work")
)

data class UiState(
    val products: List<Product> = sampleProducts,
    val selectedCategory: String = "All",
    val cart: List<CartItem> = listOf(CartItem(sampleProducts[0])),
    val selectedDistanceKm: Int = 2,
    val trialFee: Int = 79,
    val selectedSlot: String = "Tomorrow, 10 AM–12 PM",
    val exchangeQuote: ExchangeQuote? = null,
    val trialSecondsElapsed: Int = 0,
    val isTrialActive: Boolean = false,
    val activeVtoProduct: Product = sampleProducts[0],
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage("1", "seller", "Namaste! I'm Rajesh from Jaipur Royal Jewels. How can I assist you with this piece?", "10:30 AM")
    ),
    val lockedNegotiatedPrice: Double? = null,
    val sellerNotificationEvent: String? = null
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private var timerJob: Job? = null

    init { recalculateTrialFee() }

    fun setCategory(category: String) { _uiState.update { it.copy(selectedCategory = category) } }
    fun setVtoProduct(product: Product) { _uiState.update { it.copy(activeVtoProduct = product) } }
    fun setDistance(km: Int) { _uiState.update { it.copy(selectedDistanceKm = km) }; recalculateTrialFee() }

    private fun recalculateTrialFee() {
        val state = _uiState.value
        val totalCart = state.cart.sumOf { it.product.price * it.quantity }
        var fee = 49 + (state.selectedDistanceKm - 1) * 10
        if (totalCart > 2000) fee += 10
        if (totalCart > 3500) fee += 10
        _uiState.update { it.copy(trialFee = min(99, max(49, fee))) }
    }

    fun sendBuyerMessage(text: String, offerAmount: Double? = null) {
        val newMsg = ChatMessage(System.currentTimeMillis().toString(), "buyer", text, "Just now")
        _uiState.update { it.copy(chatMessages = it.chatMessages + newMsg) }

        if (offerAmount != null) {
            _uiState.update { it.copy(sellerNotificationEvent = "🔔 [Seller App Alert] Offer received for ₹${offerAmount.toInt()} on '${_uiState.value.activeVtoProduct.name}'") }
            viewModelScope.launch {
                delay(2000)
                val counter = (offerAmount * 1.07).toInt().toDouble()
                val sellerReply = ChatMessage(
                    (System.currentTimeMillis() + 1).toString(),
                    "seller",
                    "Received your offer of ₹${offerAmount.toInt()}! The best discounted price is ₹${counter.toInt()}.",
                    "Just now",
                    counterAmount = counter
                )
                _uiState.update { it.copy(chatMessages = it.chatMessages + sellerReply) }
            }
        }
    }

    fun acceptSellerOffer(amount: Double) {
        _uiState.update {
            it.copy(
                lockedNegotiatedPrice = amount,
                chatMessages = it.chatMessages + ChatMessage(System.currentTimeMillis().toString(), "seller", "🤝 Deal Locked at ₹${amount.toInt()}! Price updated at checkout.", "Just now")
            )
        }
    }

    fun clearNotificationBanner() { _uiState.update { it.copy(sellerNotificationEvent = null) } }

    fun calculateScrapQuote(grams: Double) {
        val net = ((grams * 0.5) * 0.9).toInt().toDouble()
        _uiState.update { it.copy(exchangeQuote = ExchangeQuote(grams, net)) }
    }

    fun startTrial() {
        _uiState.update { it.copy(isTrialActive = true, trialSecondsElapsed = 0) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(trialSecondsElapsed = it.trialSecondsElapsed + 1) }
            }
        }
    }

    fun stopTrial() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTrialActive = false) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = WineVelvet,
                    background = IvorySilky,
                    surface = Color.White
                )
            ) {
                AppNavigator()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Trial : Screen("trial", "Trial", Icons.Default.ShoppingCart)
    data object Exchange : Screen("exchange", "Exchange", Icons.Default.Refresh)
    data object Orders : Screen("orders", "Orders", Icons.Default.Person)
    data object VirtualTryOn : Screen("vto", "3D Mirror", Icons.Default.Star)
    data object SellerChat : Screen("chat", "Chat", Icons.Default.Email)
    data object ProductDetail : Screen("pdp/{productId}", "Details", Icons.Default.Home) {
        fun createRoute(id: String) = "pdp/$id"
    }
}

@Composable
fun AppNavigator(vm: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(Screen.Home, Screen.Trial, Screen.Exchange, Screen.Orders)
    val isTopLevel = currentRoute in navItems.map { it.route }

    BackHandler(enabled = currentRoute != null && currentRoute != Screen.Home.route) {
        if (currentRoute?.startsWith("pdp") == true || currentRoute == Screen.VirtualTryOn.route || currentRoute == Screen.SellerChat.route) {
            if (!navController.popBackStack()) navController.navigate(Screen.Home.route)
        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(containerColor = Color.White) {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WineVelvet,
                                selectedTextColor = WineVelvet,
                                indicatorColor = GoldLight
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    vm = vm,
                    onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                    onOpenVto = { navController.navigate(Screen.VirtualTryOn.route) }
                )
            }
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { entry ->
                val pId = entry.arguments?.getString("productId")
                val product = vm.uiState.collectAsState().value.products.find { it.id == pId } ?: sampleProducts[0]
                PdpScreen(
                    product = product,
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onBookTrial = { navController.navigate(Screen.Trial.route) },
                    onOpenChat = { navController.navigate(Screen.SellerChat.route) },
                    onOpenVto = {
                        vm.setVtoProduct(product)
                        navController.navigate(Screen.VirtualTryOn.route)
                    }
                )
            }
            composable(Screen.VirtualTryOn.route) {
                VtoScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onBookTrial = { navController.navigate(Screen.Trial.route) },
                    onBuyNow = { navController.navigate(Screen.Orders.route) }
                )
            }
            composable(Screen.SellerChat.route) {
                ChatScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Screen.Orders.route) }
                )
            }
            composable(Screen.Trial.route) {
                TrialScreen(
                    vm = vm,
                    onProceed = { navController.navigate(Screen.Orders.route) },
                    onOpenExchange = { navController.navigate(Screen.Exchange.route) }
                )
            }
            composable(Screen.Exchange.route) { ExchangeScreen(vm = vm) }
            composable(Screen.Orders.route) { TrackingScreen(vm = vm) }
        }
    }
}

@Composable
fun HomeScreen(vm: MainViewModel, onProductClick: (String) -> Unit, onOpenVto: () -> Unit) {
    val state by vm.uiState.collectAsState()
    val categories = listOf("All", "Daily Wear", "Bridal Studio", "Temple Hub")

    Column(modifier = Modifier.fillMaxSize().background(IvorySilky)) {
        Surface(color = WineVelvet, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RoldyGoldy", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvorySilky)
                Button(
                    onClick = onOpenVto,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("✨ 3D Mirror Try-On", color = WineDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = state.selectedCategory == cat,
                    onClick = { vm.setCategory(cat) },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.products.filter { state.selectedCategory == "All" || it.category == state.selectedCategory }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.clickable { onProductClick(item.id) }
                ) {
                    Column {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(110.dp).background(GoldLight.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.emoji, fontSize = 40.sp)
                        }
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, color = WineDark)
                            Text("₹${item.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = WineRich)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdpScreen(
    product: Product,
    vm: MainViewModel,
    onBack: () -> Unit,
    onBookTrial: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenVto: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val displayPrice = state.lockedNegotiatedPrice ?: product.price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, fontSize = 16.sp, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvorySilky)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(260.dp).background(GoldLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 80.sp)
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(product.category.uppercase(), color = GoldDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = WineDark)

                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("₹${displayPrice.toInt()}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = WineRich)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("₹${product.originalPrice.toInt()}", fontSize = 14.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                }

                Text(product.description, fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenVto,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✨ Try Virtually (Live AR Mirror)", color = WineDark, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onBookTrial,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WineVelvet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Book Trial @Home (₹49–₹99)", color = IvorySilky, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenChat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💬 Chat & Bargain with Jeweller", color = WineDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VtoScreen(vm: MainViewModel, onBack: () -> Unit, onBookTrial: () -> Unit, onBuyNow: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val state by vm.uiState.collectAsState()

    var hasCamera by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }

    LaunchedEffect(Unit) { if (!hasCamera) launcher.launch(Manifest.permission.CAMERA) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCamera && lifecycleOwner != null) {
            AndroidView(
                factory = { ctx ->
                    val view = PreviewView(ctx)
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        try {
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    view
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
        }

        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                Text("✨ 3D Overlay: ${state.activeVtoProduct.name}", color = GoldLight, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Text(state.activeVtoProduct.emoji, fontSize = 110.sp, modifier = Modifier.padding(top = 10.dp))
        }

        Surface(
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(state.products) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.activeVtoProduct.id == item.id) GoldLight else IvorySilky,
