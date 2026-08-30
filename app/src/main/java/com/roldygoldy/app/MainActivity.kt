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
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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

// ==========================================
// 🎨 LUXURY THEME & COLOR PALETTE
// ==========================================
val WineDark = Color(0xFF1B0512)
val WineVelvet = Color(0xFF4A0A28)
val WineRich = Color(0xFF6B1139)
val GoldPure = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF7E7B4)
val GoldDeep = Color(0xFF8C6D1F)
val IvorySilky = Color(0xFFFAF6EE)
val EmeraldPrestige = Color(0xFF0F382C)
val EmeraldSoft = Color(0xFFD8EDE5)
val RubyAlert = Color(0xFF8A132C)
val GlassBorder = Color(0x33D4AF37)

val GoldMetallicGradient = Brush.linearGradient(listOf(GoldLight, GoldPure, GoldDeep, GoldLight))

// ==========================================
// 📦 DATA MODELS
// ==========================================
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val originalPrice: Double,
    val emoji: String,
    val isTrialEligible: Boolean,
    val karatInfo: String,
    val description: String
)

data class Address(
    val id: String,
    val label: String,
    val recipientName: String,
    val addressLine: String,
    val pincode: String,
    val distanceKm: Double,
    val isDefault: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val counterAmount: Double? = null
)

data class OrderHistoryItem(
    val orderId: String,
    val productName: String,
    val emoji: String,
    val amount: Double,
    val status: String,
    val date: String,
    val isTrial: Boolean
)

data class ExchangeSlip(
    val id: String,
    val itemName: String,
    val weightGrams: Double,
    val grossValue: Double,
    val netCredit: Double,
    val otp: String = "7734",
    val date: String
)

// Sample Products
val sampleProducts = listOf(
    Product("1", "Sabyasachi-inspired Kundan Choker", "Bridal Studio", 3499.0, 4499.0, "👑", true, "1-Gram Matte Gold Plated", "Handcrafted Kundan, green glass stones with adjustable golden dori. Comes in a royal trousseau box."),
    Product("2", "Korean Minimal Hoops", "Daily Wear", 349.0, 549.0, "💫", false, "18K PVD Anti-Tarnish", "Waterproof and sweatproof daily minimal hoops with hypoallergenic titanium core."),
    Product("3", "Temple Deity Choker Set", "Temple Hub", 1299.0, 1899.0, "🪔", true, "Antique Micro-Gold Plated", "Authentic South Indian temple motif with ruby-red kemp stones and hanging pearls."),
    Product("4", "Polki Heritage Maangtikka", "Bridal Studio", 2899.0, 3599.0, "💍", true, "Uncut Polki Foil Finish", "Intricate floral bridal maangtikka with semi-precious emerald drops.")
)

val sampleAddresses = listOf(
    Address("a1", "Home", "Sai Kishore Bandaru", "Flat 402, Golden Palms, Bandra West, Mumbai", "400050", 2.4, isDefault = true),
    Address("a2", "Office", "Sai Kishore Bandaru", "Level 6, Tech Park, BKC, Mumbai", "400051", 4.1, isDefault = false),
    Address("a3", "Parents", "Sai Kishore Bandaru", "B-12, Sector 9, Vashi, Navi Mumbai", "400703", 18.5, isDefault = false) // Out of 5km range
)

// ==========================================
// 🧠 VIEWMODEL & CENTRAL STATE
// ==========================================
data class AppState(
    val userFirstName: String = "Sai Kishore",
    val userLastName: String = "Bandaru",
    val userEmail: String = "saikishore@example.com",
    val userPhone: String = "+91 98765 43210",
    val addresses: List<Address> = sampleAddresses,
    val selectedAddress: Address = sampleAddresses[0],
    val products: List<Product> = sampleProducts,
    val selectedCategory: String = "All",
    val trialOnlyFilter: Boolean = false,
    val activeVtoProduct: Product = sampleProducts[0],
    val appliedExchangeSlip: ExchangeSlip? = null,
    val orderHistory: List<OrderHistoryItem> = listOf(
        OrderHistoryItem("RG-98210", "Sabyasachi-inspired Kundan Choker", "👑", 3499.0, "Trial Completed & Purchased", "28 Aug 2026", true),
        OrderHistoryItem("RG-94312", "Korean Minimal Hoops", "💫", 349.0, "Delivered", "15 Aug 2026", false)
    ),
    val exchangeHistory: List<ExchangeSlip> = listOf(
        ExchangeSlip("EX-104", "Old Broken Bangle", 100.0, 35.0, 31.5, "7734", "28 Aug 2026")
    ),
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage("1", "seller", "Namaste! I'm Rajesh from Jaipur Royal Jewels. How can I assist you with this Kundan choker?", "10:30 AM")
    ),
    val lockedNegotiatedPrice: Double? = null,
    val trialFee: Int = 69,
    val selectedSlot: String = "Tomorrow, 10 AM – 12 PM",
    val trialSecondsElapsed: Int = 0,
    val isTrialActive: Boolean = false,
    val policySheetTitle: String? = null,
    val policySheetContent: String? = null
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private var timerJob: Job? = null

    init {
        recalculateTrialDistanceAndFee()
    }

    fun setCategory(cat: String) { _state.update { it.copy(selectedCategory = cat) } }
    fun toggleTrialFilter(on: Boolean) { _state.update { it.copy(trialOnlyFilter = on) } }
    fun setVtoProduct(p: Product) { _state.update { it.copy(activeVtoProduct = p) } }

    fun selectAddress(address: Address) {
        _state.update { it.copy(selectedAddress = address) }
        recalculateTrialDistanceAndFee()
    }

    private fun recalculateTrialDistanceAndFee() {
        val dist = _state.value.selectedAddress.distanceKm
        val baseFee = when {
            dist <= 2.0 -> 49
            dist <= 3.5 -> 69
            dist <= 5.0 -> 99
            else -> 99 // Out of range flag
        }
        _state.update { it.copy(trialFee = baseFee) }
    }

    fun setSlot(slot: String) { _state.update { it.copy(selectedSlot = slot) } }

    // Exchange / Scrap calculation: 100g = ₹30-₹35 with 10% purity deduction
    fun applyExchange(itemName: String, weightGrams: Double) {
        val grossRate = 0.35 // ₹0.35 per gram
        val gross = weightGrams * grossRate
        val net = (gross * 0.90) // 10% deduction for wastage/impurities
        val slip = ExchangeSlip(
            id = "EX-${System.currentTimeMillis().toString().takeLast(4)}",
            itemName = itemName.ifBlank { "Old Jewellery Scrap" },
            weightGrams = weightGrams,
            grossValue = gross,
            netCredit = (net * 100).toInt() / 100.0,
            date = "Today"
        )
        _state.update {
            it.copy(
                appliedExchangeSlip = slip,
                exchangeHistory = listOf(slip) + it.exchangeHistory
            )
        }
    }

    fun removeExchange() {
        _state.update { it.copy(appliedExchangeSlip = null) }
    }

    // Bargaining
    fun sendOffer(offerAmt: Double) {
        val userMsg = ChatMessage(System.currentTimeMillis().toString(), "buyer", "I'd like to offer ₹${offerAmt.toInt()} for this piece.", "Just now")
        _state.update { it.copy(chatMessages = it.chatMessages + userMsg) }

        viewModelScope.launch {
            delay(1500)
            val counter = (offerAmt * 1.07).toInt().toDouble()
            val sellerMsg = ChatMessage(
                (System.currentTimeMillis() + 1).toString(),
                "seller",
                "Received your offer on the Vendor Portal! The best artisanal price I can do is ₹${counter.toInt()}.",
                "Just now",
                counterAmount = counter
            )
            _state.update { it.copy(chatMessages = it.chatMessages + sellerMsg) }
        }
    }

    fun acceptCounterOffer(amt: Double) {
        _state.update {
            it.copy(
                lockedNegotiatedPrice = amt,
                chatMessages = it.chatMessages + ChatMessage(System.currentTimeMillis().toString(), "seller", "🤝 Deal locked at ₹${amt.toInt()}! Price applied to checkout.", "Just now")
            )
        }
    }

    // Trial Timer with ₹1/min overage after 5 min grace
    fun startTrial() {
        _state.update { it.copy(isTrialActive = true, trialSecondsElapsed = 0) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // 1s = 1 simulated min for demo
                _state.update { it.copy(trialSecondsElapsed = it.trialSecondsElapsed + 1) }
            }
        }
    }

    fun stopTrial() {
        timerJob?.cancel()
        _state.update { it.copy(isTrialActive = false) }
    }

    fun showPolicy(title: String, content: String) {
        _state.update { it.copy(policySheetTitle = title, policySheetContent = content) }
    }

    fun dismissPolicy() {
        _state.update { it.copy(policySheetTitle = null, policySheetContent = null) }
    }
}

// ==========================================
// 🚀 APP ENTRY & NAVIGATION
// ==========================================
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Trial : Screen("trial", "Trial", Icons.Default.Schedule)
    data object Exchange : Screen("exchange", "Exchange", Icons.Default.Cached)
    data object Orders : Screen("orders", "Orders", Icons.Default.LocalShipping)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    data object VirtualTryOn : Screen("vto", "3D Mirror", Icons.Default.CameraAlt)
    data object SellerChat : Screen("chat", "Chat", Icons.Default.Chat)
    data object AddressBook : Screen("address_book", "Addresses", Icons.Default.Place)
    data object ProductDetail : Screen("pdp/{productId}", "Details", Icons.Default.Home) {
        fun createRoute(id: String) = "pdp/$id"
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
                AppRootNavigator()
            }
        }
    }
}

@Composable
fun AppRootNavigator(vm: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(Screen.Home, Screen.Trial, Screen.Exchange, Screen.Orders, Screen.Profile)
    val isTopLevel = currentRoute in navItems.map { it.route }

    // System Back Handler
    BackHandler(enabled = currentRoute != Screen.Home.route) {
        if (!isTopLevel) {
            navController.popBackStack()
        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val state by vm.state.collectAsState()

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WineVelvet,
                                selectedTextColor = WineVelvet,
                                indicatorColor = GoldLight.copy(alpha = 0.5f)
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
                    onOpenVto = { navController.navigate(Screen.VirtualTryOn.route) },
                    onOpenAddress = { navController.navigate(Screen.AddressBook.route) }
                )
            }
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { entry ->
                val pId = entry.arguments?.getString("productId")
                val product = vm.state.collectAsState().value.products.find { it.id == pId } ?: sampleProducts[0]
                PdpScreen(
                    product = product,
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onBookTrial = { navController.navigate(Screen.Trial.route) },
                    onOpenChat = { navController.navigate(Screen.SellerChat.route) },
                    onOpenVto = {
                        vm.setVtoProduct(product)
                        navController.navigate(Screen.VirtualTryOn.route)
                    },
                    onOpenExchange = { navController.navigate(Screen.Exchange.route) },
                    onDirectBuy = { navController.navigate(Screen.Orders.route) }
                )
            }
            composable(Screen.VirtualTryOn.route) {
                InteractiveLenskartVtoScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onBookTrial = { navController.navigate(Screen.Trial.route) },
                    onBuyNow = { navController.navigate(Screen.Orders.route) }
                )
            }
            composable(Screen.SellerChat.route) {
                SellerChatScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Screen.Orders.route) }
                )
            }
            composable(Screen.Trial.route) {
                TrialBookingScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onOpenAddress = { navController.navigate(Screen.AddressBook.route) },
                    onOpenExchange = { navController.navigate(Screen.Exchange.route) },
                    onConfirm = { navController.navigate(Screen.Orders.route) }
                )
            }
            composable(Screen.Exchange.route) {
                ExchangeScrapScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onAppliedGoToCheckout = { navController.navigate(Screen.Trial.route) }
                )
            }
            composable(Screen.Orders.route) {
                OrderTrackingAndHistoryScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onContactSupport = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileHubScreen(
                    vm = vm,
                    onOpenAddressBook = { navController.navigate(Screen.AddressBook.route) },
                    onOpenOrders = { navController.navigate(Screen.Orders.route) },
                    onOpenChats = { navController.navigate(Screen.SellerChat.route) },
                    onOpenExchangeHistory = { navController.navigate(Screen.Exchange.route) }
                )
            }
            composable(Screen.AddressBook.route) {
                AddressBookScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Policy Bottom Sheet
    if (state.policySheetTitle != null) {
        ModalBottomSheet(
            onDismissRequest = { vm.dismissPolicy() },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(state.policySheetTitle ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WineDark)
                Spacer(modifier = Modifier.height(12.dp))
                Text(state.policySheetContent ?: "", fontSize = 13.sp, lineHeight = 20.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { vm.dismissPolicy() },
                    colors = ButtonDefaults.buttonColors(containerColor = WineVelvet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I Understand", color = IvorySilky)
                }
            }
        }
    }
}

// ==========================================
// 🏠 1. HOME & CATALOG SCREEN
// ==========================================
@Composable
fun HomeScreen(
    vm: MainViewModel,
    onProductClick: (String) -> Unit,
    onOpenVto: () -> Unit,
    onOpenAddress: () -> Unit
) {
    val state by vm.state.collectAsState()
    val categories = listOf("All", "Daily Wear", "Bridal Studio", "Temple Hub")

    Column(modifier = Modifier.fillMaxSize().background(IvorySilky)) {
        // Luxury Header
        Surface(color = WineVelvet, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("RoldyGoldy", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IvorySilky)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenAddress() }
                        ) {
                            Text("📍 ${state.selectedAddress.label}: ${state.selectedAddress.pincode} (${state.selectedAddress.distanceKm} km)", color = GoldLight, fontSize = 11.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                        }
                    }
                    Button(
                        onClick = onOpenVto,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("✨ 3D Mirror", color = WineDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Categories Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

        // Trial Only Filter Toggle
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldPrestige.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Show Trial @Home SKU's only", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                    Text("Eligible for doorstep concierge tryout", fontSize = 10.sp, color = Color.Gray)
                }
                Switch(
                    checked = state.trialOnlyFilter,
                    onCheckedChange = { vm.toggleTrialFilter(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = EmeraldPrestige, checkedThumbColor = IvorySilky)
                )
            }
        }

        // Product Catalog Grid
        val filtered = state.products.filter {
            (state.selectedCategory == "All" || it.category == state.selectedCategory) &&
                    (!state.trialOnlyFilter || it.isTrialEligible)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.clickable { onProductClick(item.id) }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(GoldLight.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.emoji, fontSize = 48.sp)
                            if (item.isTrialEligible) {
                                Surface(
                                    color = EmeraldPrestige,
                                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                    modifier = Modifier.align(Alignment.TopStart).padding(top = 8.dp)
                                ) {
                                    Text("Trial @Home", color = EmeraldSoft, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, color = WineDark)
                            Text(item.karatInfo, fontSize = 10.sp, color = GoldDeep, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                                Text("₹${item.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = WineRich)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("₹${item.originalPrice.toInt()}", fontSize = 11.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🔍 2. PRODUCT DETAILS (PDP) SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdpScreen(
    product: Product,
    vm: MainViewModel,
    onBack: () -> Unit,
    onBookTrial: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenVto: () -> Unit,
    onOpenExchange: () -> Unit,
    onDirectBuy: () -> Unit
) {
    val state by vm.state.collectAsState()
    val negotiated = state.lockedNegotiatedPrice
    val basePrice = negotiated ?: product.price
    val exchangeCredit = state.appliedExchangeSlip?.netCredit ?: 0.0
    val finalPayable = max(0.0, basePrice - exchangeCredit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, fontSize = 16.sp, maxLines = 1, color = WineDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(GoldLight.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 90.sp)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(product.category.uppercase(), color = GoldDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WineDark)
                Text(product.karatInfo, fontSize = 12.sp, color = EmeraldPrestige, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))

                // Price Row
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 10.dp)) {
                    Text("₹${finalPayable.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = WineRich)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("₹${product.originalPrice.toInt()}", fontSize = 14.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                    if (negotiated != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = EmeraldSoft, shape = RoundedCornerShape(4.dp)) {
                            Text("Bargain Price Applied", color = EmeraldPrestige, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp, 2.dp))
                        }
                    }
                }

                // Applied Exchange Credit Strip
                if (state.appliedExchangeSlip != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldSoft),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("♻️ Exchange Credit: -₹${state.appliedExchangeSlip?.netCredit?.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrestige)
                                Text("${state.appliedExchangeSlip?.weightGrams?.toInt()}g old jewellery deducted", fontSize = 10.sp, color = EmeraldPrestige)
                            }
                            TextButton(onClick = { vm.removeExchange() }) {
                                Text("Remove", color = RubyAlert, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Text(product.description, fontSize = 13.sp, lineHeight = 19.sp, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(16.dp))

                // 3D Mirror Try On
                Button(
                    onClick = onOpenVto,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✨ Lenskart-Style 3D Virtual Try-On", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Doorstep Trial
                if (product.isTrialEligible) {
                    Button(
                        onClick = onBookTrial,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WineVelvet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("👑 Book Trial @Home (₹${state.trialFee})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bargain Chat
                OutlinedButton(
                    onClick = onOpenChat,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WineVelvet)
                ) {
                    Text("💬 Make an Offer / Bargain with Jeweller", color = WineVelvet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exchange trade-in
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenExchange() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♻️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Exchange Old Jewellery with this purchase", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                                Text("₹0.30–₹0.35/gram deducted directly from bill", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Text("➔", color = GoldDeep, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDirectBuy,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrestige),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Direct Buy (Pay ₹${finalPayable.toInt()})", color = IvorySilky, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 🪞 3. INTERACTIVE 3D VIRTUAL TRY-ON (Lenskart-style)
// ==========================================
@Composable
fun InteractiveLenskartVtoScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onBookTrial: () -> Unit,
    onBuyNow: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val state by vm.state.collectAsState()

    var hasCamera by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }

    LaunchedEffect(Unit) { if (!hasCamera) launcher.launch(Manifest.permission.CAMERA) }

    // Interactive Drag & Scale Anchors
    var jewelryOffset by remember { mutableStateOf(Offset(0f, 60f)) }
    var jewelryScale by remember { mutableFloatStateOf(1.0f) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // CameraX Live Feed
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    view
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.85f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
            }

            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GoldPure)
            ) {
                Text(
                    text = "🪞 Pinch to scale · Drag to fit neckline",
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Augmented Drag/Scale Jewelry Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        jewelryScale = (jewelryScale * zoom).coerceIn(0.6f, 2.2f)
                        jewelryOffset = Offset(jewelryOffset.x + pan.x, jewelryOffset.y + pan.y)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(x = jewelryOffset.x.dp, y = jewelryOffset.y.dp)
            ) {
                Text(
                    text = state.activeVtoProduct.emoji,
                    fontSize = (90 * jewelryScale).sp
                )
            }
        }

        // Bottom Selector & Dual Try-On Funnel
        Surface(
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select jewellery piece to try on:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    items(state.products) { p ->
                        val isSelected = state.activeVtoProduct.id == p.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GoldLight else IvorySilky,
                            border = BorderStroke(1.5.dp, if (isSelected) GoldPure else Color.Transparent),
                            modifier = Modifier.size(64.dp).clickable { vm.setVtoProduct(p) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(p.emoji, fontSize = 24.sp)
                                Text(p.name.take(6) + "…", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WineDark)
                            }
                        }
                    }
                }

                // Dual Funnel CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBookTrial,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, WineVelvet)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Unsure?", fontSize = 9.sp, color = Color.Gray)
                            Text("Book Trial @Home", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WineVelvet)
                        }
                    }

                    Button(
                        onClick = onBuyNow,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WineVelvet)
                    ) {
                        Text("Buy (₹${state.activeVtoProduct.price.toInt()})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 🛵 4. TRIAL @HOME (15-20 MIN + ₹1/MIN OVERAGE & GPS DISTANCE)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialBookingScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onOpenAddress: () -> Unit,
    onOpenExchange: () -> Unit,
    onConfirm: () -> Unit
) {
    val state by vm.state.collectAsState()
    val dist = state.selectedAddress.distanceKm
    val isOutOfRange = dist > 5.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trial @Home Booking", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvorySilky)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Delivery Address GPS Distance Strip
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DELIVERY LOCATION (AUTO-GPS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                        TextButton(onClick = onOpenAddress) {
                            Text("Change Address", fontSize = 11.sp, color = WineVelvet)
                        }
                    }
                    Text("${state.selectedAddress.label} · ${state.selectedAddress.recipientName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WineDark)
                    Text(state.selectedAddress.addressLine, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = if (isOutOfRange) RubyAlert.copy(alpha = 0.1f) else EmeraldSoft,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isOutOfRange) "⚠ Rider Distance: $dist km (Exceeds 5km Trial Limit)" else "📍 Rider Distance: $dist km from nearby boutique hub",
                            color = if (isOutOfRange) RubyAlert else EmeraldPrestige,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing & Overage Notice Box
            Card(
                colors = CardDefaults.cardColors(containerColor = WineVelvet),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TRIAL BOOKING FEE", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${state.trialFee}", color = GoldLight, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(color = GoldLight, shape = RoundedCornerShape(8.dp)) {
                            Text("15–20 Min Tryout", color = WineDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp, 4.dp))
                        }
                    }
                    Divider(color = WineRich, modifier = Modifier.padding(vertical = 10.dp))
                    Text(
                        text = "• First 15–20 mins covered in this booking fee.\n• 5-minute grace period included.\n• ₹1/minute overage applies after grace period expires.\n• Fee waived against purchase if you keep an item.",
                        color = IvorySilky,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Slots
            Text("Select Tryout Time Slot:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
            val slots = listOf("Today, 4 PM – 6 PM", "Tomorrow, 10 AM – 12 PM", "Tomorrow, 4 PM – 6 PM")
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slots.forEach { s ->
                    val isSelected = state.selectedSlot == s
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) WineVelvet else Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSelected) WineVelvet else GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { vm.setSlot(s) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s, fontSize = 10.sp, color = if (isSelected) IvorySilky else WineDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Old Jewellery Exchange Trigger
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth().clickable { onOpenExchange() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("♻️ Have old jewellery to hand over at trial?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                        Text(
                            text = if (state.appliedExchangeSlip != null) "✓ ₹${state.appliedExchangeSlip?.netCredit?.toInt()} trade-in credit active" else "Add old items for instant deduction during pickup",
                            fontSize = 11.sp,
                            color = if (state.appliedExchangeSlip != null) EmeraldPrestige else Color.Gray
                        )
                    }
                    Text("➔", color = GoldDeep, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isOutOfRange) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.LightGray)
                ) {
                    Text("Out of 5km Range (Choose PAN-India Shipping)", color = Color.DarkGray, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WineVelvet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Trial Booking (Pay ₹${state.trialFee})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ==========================================
// ♻️ 5. OLD JEWELLERY EXCHANGE & VALUATION SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScrapScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onAppliedGoToCheckout: () -> Unit
) {
    val state by vm.state.collectAsState()
    var itemName by remember { mutableStateOf("") }
    var gramsInput by remember { mutableStateOf("100") }
    var photoCaptured by remember { mutableStateOf(false) }

    val grams = gramsInput.toDoubleOrNull() ?: 0.0
    val gross = grams * 0.35
    val netCredit = (gross * 0.90) // 10% deduction

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Old Jewellery Exchange", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvorySilky)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Trade in old rolled-gold, silver-plated, or broken jewellery for instant purchase discounts.", fontSize = 12.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(12.dp))

            // Policy / Rules Card
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSoft.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📋 EXCHANGE RULES & POLICY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldPrestige)
                    Text("• ₹0.30–₹0.35 exchange credit per gram.\n• 10% weightage deduction applied for wastage/impurities.\n• Strictly NO plastic or synthetic jewellery accepted.\n• Live camera capture required at pickup for verification.", fontSize = 11.sp, lineHeight = 16.sp, color = EmeraldPrestige)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Form
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Description (e.g. Old bangles, broken chain)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = gramsInput,
                onValueChange = { gramsInput = it },
                label = { Text("Approx. Weight (in Grams)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live Camera Photo Capture
            Button(
                onClick = { photoCaptured = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (photoCaptured) EmeraldPrestige else WineDark),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (photoCaptured) "✓ Live Camera Photo Attached" else "📸 Take Live Photo (No Gallery Uploads)", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Valuation Box
            Card(
                colors = CardDefaults.cardColors(containerColor = WineVelvet),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ESTIMATED EXCHANGE CREDIT", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("₹${(netCredit * 100).toInt() / 100.0}", color = GoldLight, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Net of 10% purity deduction (₹0.35/gram slab)", color = IvorySilky, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    vm.applyExchange(itemName, grams)
                    onAppliedGoToCheckout()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply to Cart & Deduct from Bill", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ==========================================
// 💬 6. SELLER BARGAINING CHAT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerChatScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val state by vm.state.collectAsState()
    var msgText by remember { mutableStateOf("") }
    var offerInput by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jaipur Royal Jewels", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WineDark)
                        Text("Online on Vendor Portal", fontSize = 10.sp, color = EmeraldPrestige)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky)) {
            // Chat Message Stream
            LazyColumn(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.chatMessages) { msg ->
                    val isBuyer = msg.senderId == "buyer"
                    Column(
                        horizontalAlignment = if (isBuyer) Alignment.End else Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isBuyer) WineVelvet else Color.White,
                            border = if (!isBuyer) BorderStroke(1.dp, GlassBorder) else null,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(msg.text, color = if (isBuyer) IvorySilky else WineDark, fontSize = 13.sp)
                                if (msg.counterAmount != null && state.lockedNegotiatedPrice == null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { vm.acceptCounterOffer(msg.counterAmount) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrestige),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Accept Counter ₹${msg.counterAmount.toInt()}", color = EmeraldSoft, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Strip
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Bargain", color = WineDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = msgText,
                        onValueChange = { msgText = it },
                        placeholder = { Text("Ask seller…", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (msgText.isNotBlank()) {
                                vm.sendOffer(msgText.toDoubleOrNull() ?: 3000.0)
                                msgText = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WineVelvet)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Send Custom Offer to Jeweller") },
            text = {
                OutlinedTextField(
                    value = offerInput,
                    onValueChange = { offerInput = it },
                    label = { Text("Your Offer Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = offerInput.toDoubleOrNull()
                        if (amt != null) vm.sendOffer(amt)
                        showDialog = false
                    }
                ) { Text("Send to Vendor App") }
            }
        )
    }
}

// ==========================================
// 📦 7. ORDERS, TRACKING & TRIAL TIMER
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingAndHistoryScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onContactSupport: () -> Unit
) {
    val state by vm.state.collectAsState()
    val elapsed = state.trialSecondsElapsed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders & Live Concierge", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvorySilky)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Live Concierge Status
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1ECE4)),
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛵 Rider 'Vikram' is en route (OTP: 4812)", fontWeight = FontWeight.Bold, color = WineDark, fontSize = 13.sp)
                        Text("Arriving in 14 mins · Sealed Trial Kit", color = EmeraldPrestige, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Doorstep Tryout Timer & Overage Hub
            Card(
                colors = CardDefaults.cardColors(containerColor = WineDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!state.isTrialActive) {
                        Text("Start Doorstep Tryout Timer", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Initial 15–20 mins covered · ₹1/min overage after grace", color = GoldLight, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                        Button(onClick = { vm.startTrial() }, colors = ButtonDefaults.buttonColors(containerColor = GoldPure)) {
                            Text("Start Timer on Box Unseal", color = WineDark, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val status = when {
                            elapsed <= 20 -> "Free Window Active (${20 - elapsed}:00 left)"
                            elapsed <= 25 -> "Grace Period (${25 - elapsed}:00 left)"
                            else -> "Overage Charge: ₹${elapsed - 25} (₹1/min)"
                        }
                        Text(status, color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { vm.stopTrial() }, colors = ButtonDefaults.buttonColors(containerColor = IvorySilky)) {
                            Text("End Tryout & Finalize Purchase", color = WineDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Past Orders & Trial Reports", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WineDark)

            state.orderHistory.forEach { o ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(o.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(o.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                            Text("${o.status} · ${o.date}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("₹${o.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WineRich)
                    }
                }
            }
        }
    }
}

// ==========================================
// 👤 8. PROFILE, ADDRESS BOOK & POLICIES HUB
// ==========================================
@Composable
fun ProfileHubScreen(
    vm: MainViewModel,
    onOpenAddressBook: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenExchangeHistory: () -> Unit
) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IvorySilky)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Surface(color = WineVelvet, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(54.dp).clip(CircleShape).background(GoldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SK", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WineDark)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("${state.userFirstName} ${state.userLastName}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IvorySilky)
                        Text(state.userPhone, fontSize = 12.sp, color = GoldLight)
                        Text(state.userEmail, fontSize = 11.sp, color = IvorySilky.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("ACCOUNT & HUB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Column {
                    ProfileMenuRow("📍 Address Book", "Manage delivery and Trial @Home addresses") { onOpenAddressBook() }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("📦 Order & Trial History", "View invoices, tryout slips & tracking") { onOpenOrders() }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("💬 Seller Chat Threads", "Active bargains & vendor replies") { onOpenChats() }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("♻️ Exchange & Valuation Slips", "Track scrap trade-in credit history") { onOpenExchangeHistory() }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("POLICIES & LEGAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Column {
                    ProfileMenuRow("📜 Terms & Conditions", "Platform rules and usage policies") {
                        vm.showPolicy("Terms & Conditions", "1. Trial @Home is restricted to a 5km radius from our partner boutiques.\n2. Initial trial booking fee covers 15-20 minutes plus a 5-minute grace period.\n3. ₹1 per minute applies for extra tryout time.")
                    }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("🔒 Privacy Policy", "How we protect and secure your data") {
                        vm.showPolicy("Privacy Policy", "Your personal information, address geocodes, and camera feeds during Virtual Try-On are processed securely on-device and never stored on third-party servers.")
                    }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("🔄 Refund & Cancellation Policy", "Tryout fee waivers & product returns") {
                        vm.showPolicy("Refund Policy", "The trial fee is non-refundable if all items are rejected at doorstep. If any item is purchased, the full trial fee is adjusted against your bill.")
                    }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("♻️ Exchange & Trade-In Rules", "10% deduction & metal valuation slabs") {
                        vm.showPolicy("Exchange Policy", "Old jewellery is valued at ₹0.30–₹0.35/g net of a 10% purity check deduction. Strictly no plastic/synthetic items are accepted.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("HELP & SUPPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Column {
                    ProfileMenuRow("💬 WhatsApp Support", "Live chat with customer concierge") {
                        vm.showPolicy("Customer Care", "WhatsApp Support: +91 98765 00000\nEmail: care@roldygoldy.com\nTimings: 9 AM - 9 PM IST")
                    }
                    Divider(color = Color(0xFFF1ECE4))
                    ProfileMenuRow("🚪 Logout", "Sign out of your RoldyGoldy account") {
                        vm.showPolicy("Sign Out", "You have successfully signed out of the prototype demo.")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WineDark)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
        }
        Text("➔", color = GoldDeep, fontSize = 12.sp)
    }
}

// ==========================================
// 📍 9. ADDRESS BOOK SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBookScreen(vm: MainViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Addresses", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvorySilky)
                .padding(16.dp)
        ) {
            Text("Trial @Home is serviceable within 5 km of partner boutiques.", fontSize = 12.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(12.dp))

            state.addresses.forEach { addr ->
                val isSelected = state.selectedAddress.id == addr.id
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { vm.selectAddress(addr) },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) GoldLight.copy(alpha = 0.25f) else Color.White),
                    border = BorderStroke(1.5.dp, if (isSelected) GoldPure else GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${addr.label} · ${addr.recipientName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WineDark)
                            if (isSelected) {
                                Surface(color = WineVelvet, shape = RoundedCornerShape(4.dp)) {
                                    Text("Active", color = IvorySilky, fontSize = 10.sp, modifier = Modifier.padding(4.dp, 2.dp))
                                }
                            }
                        }
                        Text(addr.addressLine, fontSize = 12.sp, color = Color.DarkGray)
                        Text(
                            text = if (addr.distanceKm > 5.0) "Distance: ${addr.distanceKm} km (Out of 5km Trial Range)" else "Distance: ${addr.distanceKm} km (Eligible for Trial @Home)",
                            color = if (addr.distanceKm > 5.0) RubyAlert else EmeraldPrestige,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
