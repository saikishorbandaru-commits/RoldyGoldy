@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun LuxuryBrandLogo(size: Int = 42, showText: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(size.dp).clip(CircleShape).background(Brush.radialGradient(listOf(GoldLight, GoldPure, GoldDeep), radius = 80f)).border(2.dp, GoldLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("RG", color = WineDark, fontWeight = FontWeight.ExtraBold, fontSize = (size * 0.42).sp)
        }
        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("RoldyGoldy", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = IvorySilky)
                Text("FINE JEWELLERY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = GoldLight)
            }
        }
    }
}

data class Product(val id: String, val name: String, val category: String, val price: Double, val originalPrice: Double, val emoji: String, val isTrialEligible: Boolean, val karatInfo: String, val description: String)
data class CartItem(val product: Product, var quantity: Int = 1, var customPrice: Double? = null)
data class Address(val id: String, val label: String, val recipientName: String, val addressLine: String, val pincode: String, val distanceKm: Double, val isDefault: Boolean = false)
data class ChatMessage(val id: String, val senderId: String, val text: String, val timestamp: String, val isOffer: Boolean = false, val counterAmount: Double? = null)
data class OrderHistoryItem(val orderId: String, val productName: String, val emoji: String, val amount: Double, val status: String, val date: String)
data class ExchangeSlip(val id: String, val itemName: String, val weightGrams: Double, val grossValue: Double, val netCredit: Double, val otp: String = "7734")
data class OnboardingSlide(val title: String, val subtitle: String, val emoji: String, val badge: String)

val onboardingSlides = listOf(
    OnboardingSlide("3D Virtual Try-On", "Live AR camera try-on for necklaces, hoops & maangtikkas.", "🪞", "Virtual Mirror"),
    OnboardingSlide("Trial @Home Concierge", "Try real pieces at doorstep for 15–20 mins.", "👑", "Doorstep Tryout"),
    OnboardingSlide("Old Jewellery Exchange", "Get ₹0.30–₹0.35/g credit deducted directly from your cart.", "♻️", "Trade-In Discount"),
    OnboardingSlide("Live Jeweller Bargaining", "Chat with artisans and lock discounted deals in real time.", "💬", "P2P Negotiation")
)

val sampleProducts = listOf(
    Product("1", "Sabyasachi-inspired Kundan Choker", "Bridal Studio", 3499.0, 4499.0, "👑", true, "1-Gram Matte Gold", "Handcrafted Kundan with green glass stones."),
    Product("2", "Korean Minimal Hoops", "Daily Wear", 349.0, 549.0, "💫", false, "18K PVD Anti-Tarnish", "Waterproof daily hoops with hypoallergenic core."),
    Product("3", "Temple Deity Choker Set", "Temple Hub", 1299.0, 1899.0, "🪔", true, "Antique Micro-Gold", "Authentic South Indian motif with kemp stones."),
    Product("4", "Polki Heritage Maangtikka", "Bridal Studio", 2899.0, 3599.0, "💍", true, "Uncut Polki Foil", "Floral bridal maangtikka with emerald drops.")
)

val sampleAddresses = listOf(
    Address("a1", "Home", "Sai Kishore Bandaru", "Flat 402, Golden Palms, Bandra West, Mumbai", "400050", 2.4, isDefault = true),
    Address("a2", "Office", "Sai Kishore Bandaru", "Level 6, Tech Park, BKC, Mumbai", "400051", 4.1, isDefault = false),
    Address("a3", "Parents", "Sai Kishore Bandaru", "B-12, Sector 9, Vashi, Navi Mumbai", "400703", 18.5, isDefault = false)
)

data class AppState(
    val isFirstTimeUser: Boolean = true,
    val isLoggedIn: Boolean = false,
    val userFirstName: String = "Sai Kishore",
    val userLastName: String = "Bandaru",
    val userPhone: String = "+91 98765 43210",
    val addresses: List<Address> = sampleAddresses,
    val selectedAddress: Address = sampleAddresses[0],
    val products: List<Product> = sampleProducts,
    val selectedCategory: String = "All",
    val trialOnlyFilter: Boolean = false,
    val cart: List<CartItem> = listOf(CartItem(sampleProducts[1], 1)),
    val activeVtoProduct: Product = sampleProducts[0],
    val appliedExchangeSlip: ExchangeSlip? = null,
    val negotiatedPrices: Map<String, Double> = emptyMap(),
    val orderHistory: List<OrderHistoryItem> = listOf(
        OrderHistoryItem("RG-98210", "Sabyasachi-inspired Kundan Choker", "👑", 3499.0, "Trial Purchased", "28 Aug 2026"),
        OrderHistoryItem("RG-94312", "Korean Minimal Hoops", "💫", 349.0, "Delivered", "15 Aug 2026")
    ),
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage("1", "seller", "Namaste! How can I assist you with this jewellery piece?", "10:30 AM")
    ),
    val trialFee: Int = 69,
    val selectedSlot: String = "Tomorrow, 10 AM – 12 PM",
    val trialSecondsElapsed: Int = 0,
    val isTrialActive: Boolean = false,
    val selectedPaymentMethod: String = "UPI (Google Pay / PhonePe)"
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private var timerJob: Job? = null

    init { recalculateDistanceFee() }

    fun completeOnboarding() { _state.update { it.copy(isFirstTimeUser = false) } }
    fun login(phone: String, name: String) { _state.update { it.copy(isLoggedIn = true, userPhone = phone, userFirstName = name) } }
    fun logout() { _state.update { it.copy(isLoggedIn = false) } }
    fun setCategory(cat: String) { _state.update { it.copy(selectedCategory = cat) } }
    fun toggleTrialFilter(on: Boolean) { _state.update { it.copy(trialOnlyFilter = on) } }
    fun setVtoProduct(p: Product) { _state.update { it.copy(activeVtoProduct = p) } }

    fun selectAddress(address: Address) {
        _state.update { it.copy(selectedAddress = address) }
        recalculateDistanceFee()
    }

    fun autoDetectGps() {
        val detected = Address("gps", "Current Location (Live GPS)", _state.value.userFirstName, "Bandra West, Hill Road, Mumbai", "400050", 1.8, isDefault = true)
        _state.update { it.copy(addresses = listOf(detected) + it.addresses, selectedAddress = detected) }
        recalculateDistanceFee()
    }

    private fun recalculateDistanceFee() {
        val dist = _state.value.selectedAddress.distanceKm
        val fee = when { dist <= 2.0 -> 49; dist <= 3.5 -> 69; else -> 99 }
        _state.update { it.copy(trialFee = fee) }
    }

    fun setSlot(slot: String) { _state.update { it.copy(selectedSlot = slot) } }

    fun addToCart(product: Product, customPrice: Double? = null) {
        val current = _state.value.cart.toMutableList()
        val existing = current.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity += 1
            if (customPrice != null) existing.customPrice = customPrice
        } else {
            current.add(CartItem(product, 1, customPrice))
        }
        _state.update { it.copy(cart = current) }
    }

    fun updateCartQty(productId: String, delta: Int) {
        val current = _state.value.cart.toMutableList()
        val item = current.find { it.product.id == productId } ?: return
        item.quantity += delta
        if (item.quantity <= 0) current.remove(item)
        _state.update { it.copy(cart = current) }
    }

    fun applyExchange(itemName: String, weightGrams: Double) {
        val net = (weightGrams * 0.35) * 0.90
        val slip = ExchangeSlip("EX-101", itemName.ifBlank { "Old Scrap" }, weightGrams, weightGrams * 0.35, (net * 100).toInt() / 100.0)
        _state.update { it.copy(appliedExchangeSlip = slip) }
    }

    fun removeExchange() { _state.update { it.copy(appliedExchangeSlip = null) } }

    fun sendTextMessage(text: String) {
        val msg = ChatMessage(System.currentTimeMillis().toString(), "buyer", text, "Just now")
        _state.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    fun sendBargainOffer(productId: String, offerAmt: Double) {
        val userMsg = ChatMessage(System.currentTimeMillis().toString(), "buyer", "Offer: ₹${offerAmt.toInt()}", "Just now", true)
        _state.update { it.copy(chatMessages = it.chatMessages + userMsg) }
        viewModelScope.launch {
            delay(1200)
            val counter = (offerAmt * 1.07).toInt().toDouble()
            val sellerMsg = ChatMessage((System.currentTimeMillis() + 1).toString(), "seller", "Vendor Counter: ₹${counter.toInt()}", "Just now", true, counter)
            _state.update { it.copy(chatMessages = it.chatMessages + sellerMsg) }
        }
    }

    fun acceptCounterOffer(productId: String, amt: Double) {
        val map = _state.value.negotiatedPrices.toMutableMap()
        map[productId] = amt
        _state.update { it.copy(negotiatedPrices = map) }
    }

    fun setPaymentMethod(method: String) { _state.update { it.copy(selectedPaymentMethod = method) } }

    fun calculateCartPayable(): Double {
        val s = _state.value
        val itemsTotal = s.cart.sumOf { (s.negotiatedPrices[it.product.id] ?: it.customPrice ?: it.product.price) * it.quantity }
        val exchangeDisc = s.appliedExchangeSlip?.netCredit ?: 0.0
        return max(0.0, itemsTotal - exchangeDisc)
    }

    fun placeOrder(isTrial: Boolean = false): String {
        val orderId = "RG-${(10000..99999).random()}"
        val firstItem = _state.value.cart.firstOrNull()?.product ?: sampleProducts[0]
        val total = calculateCartPayable()
        val newOrder = OrderHistoryItem(orderId, if (isTrial) "Trial: ${firstItem.name}" else firstItem.name, firstItem.emoji, if (isTrial) _state.value.trialFee.toDouble() else total, "Confirmed", "Today")
        _state.update { it.copy(orderHistory = listOf(newOrder) + it.orderHistory, cart = emptyList(), appliedExchangeSlip = null) }
        return orderId
    }

    fun startTrial() {
        _state.update { it.copy(isTrialActive = true, trialSecondsElapsed = 0) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(trialSecondsElapsed = it.trialSecondsElapsed + 1) }
            }
        }
    }

    fun stopTrial() {
        timerJob?.cancel()
        _state.update { it.copy(isTrialActive = false) }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Onboarding : Screen("onboarding", "Welcome", Icons.Default.Star)
    data object Auth : Screen("auth", "Login", Icons.Default.Lock)
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    data object Checkout : Screen("checkout", "Checkout", Icons.Default.CreditCard)
    data object Trial : Screen("trial", "Trial", Icons.Default.Schedule)
    data object Exchange : Screen("exchange", "Exchange", Icons.Default.Cached)
    data object Orders : Screen("orders", "Orders", Icons.Default.LocalShipping)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    data object VirtualTryOn : Screen("vto", "3D Mirror", Icons.Default.CameraAlt)
    data object SellerChat : Screen("chat/{productId}", "Chat", Icons.Default.Chat) { fun createRoute(pId: String) = "chat/$pId" }
    data object AddressBook : Screen("address_book", "Addresses", Icons.Default.Place)
    data object ProductDetail : Screen("pdp/{productId}", "Details", Icons.Default.Home) { fun createRoute(id: String) = "pdp/$id" }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = WineVelvet, background = IvorySilky, surface = Color.White)) {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator(vm: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val state by vm.state.collectAsState()

    val navItems = listOf(Screen.Home, Screen.Cart, Screen.Trial, Screen.Exchange, Screen.Profile)
    val isTopLevel = currentRoute in navItems.map { it.route }

    BackHandler(enabled = currentRoute != Screen.Home.route && currentRoute != Screen.Auth.route && currentRoute != Screen.Onboarding.route) {
        if (!isTopLevel) navController.popBackStack()
        else navController.navigate(Screen.Home.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
    }

    Scaffold(
        bottomBar = {
            if (isTopLevel && state.isLoggedIn) {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                BadgedBox(badge = { if (screen == Screen.Cart && state.cart.isNotEmpty()) Badge { Text("${state.cart.sumOf { it.quantity }}") } }) {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = WineVelvet, selectedTextColor = WineVelvet, indicatorColor = GoldLight.copy(alpha = 0.5f)),
                            onClick = { if (currentRoute != screen.route) navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val startScreen = when { state.isFirstTimeUser -> Screen.Onboarding.route; !state.isLoggedIn -> Screen.Auth.route; else -> Screen.Home.route }

        NavHost(navController = navController, startDestination = startScreen, modifier = Modifier.padding(padding)) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen { vm.completeOnboarding(); navController.navigate(Screen.Auth.route) }
            }
            composable(Screen.Auth.route) {
                AuthScreen(vm) { navController.navigate(Screen.Home.route) }
            }
            composable(Screen.Home.route) {
                HomeScreen(vm, { navController.navigate(Screen.ProductDetail.createRoute(it)) }, { navController.navigate(Screen.VirtualTryOn.route) }, { navController.navigate(Screen.AddressBook.route) }, { navController.navigate(Screen.Cart.route) })
            }
            composable(Screen.ProductDetail.route, arguments = listOf(navArgument("productId") { type = NavType.StringType })) { entry ->
                val pId = entry.arguments?.getString("productId") ?: "1"
                val product = vm.state.collectAsState().value.products.find { it.id == pId } ?: sampleProducts[0]
                PdpScreen(product, vm, { navController.popBackStack() }, { navController.navigate(Screen.Trial.route) }, { navController.navigate(Screen.SellerChat.createRoute(product.id)) }, { vm.setVtoProduct(product); navController.navigate(Screen.VirtualTryOn.route) }, { navController.navigate(Screen.Exchange.route) }, { navController.navigate(Screen.Cart.route) })
            }
            composable(Screen.Cart.route) {
                CartScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.Exchange.route) }, { navController.navigate(Screen.Checkout.route) }, { navController.navigate(Screen.Home.route) })
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.Orders.route) })
            }
            composable(Screen.VirtualTryOn.route) {
                VtoScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.Trial.route) }, { navController.navigate(Screen.Cart.route) })
            }
            composable(Screen.SellerChat.route, arguments = listOf(navArgument("productId") { type = NavType.StringType })) { entry ->
                val pId = entry.arguments?.getString("productId") ?: "1"
                val product = vm.state.collectAsState().value.products.find { it.id == pId } ?: sampleProducts[0]
                SellerChatScreen(product, vm, { navController.popBackStack() }, { navController.navigate(Screen.Cart.route) })
            }
            composable(Screen.Trial.route) {
                TrialScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.AddressBook.route) }, { navController.navigate(Screen.Exchange.route) }, { vm.placeOrder(true); navController.navigate(Screen.Orders.route) })
            }
            composable(Screen.Exchange.route) {
                ExchangeScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.Cart.route) })
            }
            composable(Screen.Orders.route) {
                OrdersScreen(vm, { navController.popBackStack() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(vm, { navController.navigate(Screen.AddressBook.route) }, { navController.navigate(Screen.Orders.route) }, { navController.navigate(Screen.Auth.route) })
            }
            composable(Screen.AddressBook.route) {
                AddressBookScreen(vm, { navController.popBackStack() })
            }
        }
    }
}

// ==========================================
// 📱 SCREEN IMPLEMENTATIONS
// ==========================================

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(WineVelvet).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            LuxuryBrandLogo(size = 38, showText = true)
            TextButton(onClick = onGetStarted) { Text("Skip", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val slide = onboardingSlides[page]
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(color = GoldPure.copy(alpha = 0.15f), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.5.dp, GoldPure), modifier = Modifier.size(140.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(slide.emoji, fontSize = 68.sp) }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Surface(color = GoldPure, shape = RoundedCornerShape(12.dp)) {
                    Text(slide.badge.uppercase(), color = WineDark, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(10.dp, 3.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(slide.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvorySilky, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(slide.subtitle, fontSize = 12.sp, color = GoldLight.copy(alpha = 0.85f), textAlign = TextAlign.Center, lineHeight = 18.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 12.dp)) {
            repeat(onboardingSlides.size) { i ->
                val active = pagerState.currentPage == i
                Box(modifier = Modifier.height(6.dp).width(if (active) 22.dp else 6.dp).clip(RoundedCornerShape(3.dp)).background(if (active) GoldPure else Color.White.copy(0.3f)))
            }
        }
        Button(
            onClick = { if (pagerState.currentPage < onboardingSlides.size - 1) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } else onGetStarted() },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPure),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(if (pagerState.currentPage == onboardingSlides.size - 1) "Enter Boutique (Login / Signup)" else "Continue", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun AuthScreen(vm: MainViewModel, onAuthSuccess: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var name by remember { mutableStateOf("Sai Kishore Bandaru") }
    var pass by remember { mutableStateOf("pass123") }
    var showForgot by remember { mutableStateOf(false) }
    var forgotPhone by remember { mutableStateOf("+91 98765 43210") }

    Column(modifier = Modifier.fillMaxSize().background(IvorySilky).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        LuxuryBrandLogo(size = 56, showText = true)
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEADBCE), RoundedCornerShape(10.dp)).padding(3.dp)) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (!isRegister) WineVelvet else Color.Transparent).clickable { isRegister = false }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("Login", color = if (!isRegister) IvorySilky else WineDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isRegister) WineVelvet else Color.Transparent).clickable { isRegister = true }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("Sign Up", color = if (isRegister) IvorySilky else WineDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isRegister) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true)

        if (!isRegister) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showForgot = true }) { Text("Forgot Password?", color = WineRich, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        } else Spacer(modifier = Modifier.height(14.dp))

        Button(onClick = { vm.login(phone, name); onAuthSuccess() }, colors = ButtonDefaults.buttonColors(containerColor = WineVelvet), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text(if (isRegister) "Register & Enter" else "Login to Boutique", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }

    if (showForgot) {
        AlertDialog(
            onDismissRequest = { showForgot = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = WineDark) },
            text = {
                Column {
                    Text("Enter mobile number to receive OTP reset link:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = forgotPhone, onValueChange = { forgotPhone = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = { showForgot = false; vm.login(forgotPhone, "Sai Kishore"); onAuthSuccess() }, colors = ButtonDefaults.buttonColors(containerColor = WineVelvet)) {
                    Text("Send OTP & Login")
                }
            },
            dismissButton = { TextButton(onClick = { showForgot = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }
}

@Composable
fun HomeScreen(vm: MainViewModel, onProductClick: (String) -> Unit, onOpenVto: () -> Unit, onOpenAddress: () -> Unit, onOpenCart: () -> Unit) {
    val state by vm.state.collectAsState()
    val categories = listOf("All", "Daily Wear", "Bridal Studio", "Temple Hub")

    Column(modifier = Modifier.fillMaxSize().background(IvorySilky)) {
        Surface(color = WineVelvet, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    LuxuryBrandLogo(size = 38, showText = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onOpenCart) {
                            BadgedBox(badge = { if (state.cart.isNotEmpty()) Badge { Text("${state.cart.sumOf { it.quantity }}") } }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = IvorySilky)
                            }
                        }
                        Button(onClick = onOpenVto, colors = ButtonDefaults.buttonColors(containerColor = GoldPure), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("✨ 3D Mirror", color = WineDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp).clickable { onOpenAddress() }) {
                    Text("📍 ${state.selectedAddress.label}: ${state.selectedAddress.pincode} (${state.selectedAddress.distanceKm} km)", color = GoldLight, fontSize = 11.sp)
                    Icon(Icons.Default.Place, contentDescription = null, tint = GoldLight, modifier = Modifier.size(13.dp))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { cat ->
                FilterChip(selected = state.selectedCategory == cat, onClick = { vm.setCategory(cat) }, label = { Text(cat, fontSize = 11.sp) })
            }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = EmeraldPrestige.copy(alpha = 0.08f))) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Trial @Home SKU's only", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WineDark)
                Switch(checked = state.trialOnlyFilter, onCheckedChange = { vm.toggleTrialFilter(it) }, colors = SwitchDefaults.colors(checkedTrackColor = EmeraldPrestige, checkedThumbColor = IvorySilky))
            }
        }
        val filtered = state.products.filter { (state.selectedCategory == "All" || it.category == state.selectedCategory) && (!state.trialOnlyFilter || it.isTrialEligible) }
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(filtered) { item ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.clickable { onProductClick(item.id) }) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(GoldLight.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                            Text(item.emoji, fontSize = 42.sp)
                            if (item.isTrialEligible) {
                                Surface(color = EmeraldPrestige, shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp), modifier = Modifier.align(Alignment.TopStart).padding(top = 6.dp)) {
                                    Text("Trial @Home", color = EmeraldSoft, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp, 2.dp))
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, color = WineDark)
                            Text(item.karatInfo, fontSize = 9.sp, color = GoldDeep, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                                Text("₹${item.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = WineRich)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("₹${item.originalPrice.toInt()}", fontSize = 10.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdpScreen(product: Product, vm: MainViewModel, onBack: () -> Unit, onBookTrial: () -> Unit, onOpenChat: () -> Unit, onOpenVto: () -> Unit, onOpenExchange: () -> Unit, onGoToCart: () -> Unit) {
    val state by vm.state.collectAsState()
    val negotiatedPrice = state.negotiatedPrices[product.id]
    val activePrice = negotiatedPrice ?: product.price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, fontSize = 15.sp, maxLines = 1, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(GoldLight.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                Text(product.emoji, fontSize = 80.sp)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.category.uppercase(), color = GoldDeep, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = WineDark)
                Text(product.karatInfo, fontSize = 11.sp, color = EmeraldPrestige, fontWeight = FontWeight.SemiBold)

                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("₹${activePrice.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = WineRich)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("₹${product.originalPrice.toInt()}", fontSize = 13.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                }

                Text(product.description, fontSize = 12.sp, lineHeight = 18.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(14.dp))

                Button(onClick = onOpenVto, modifier = Modifier.fillMaxWidth().height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldPure), shape = RoundedCornerShape(10.dp)) {
                    Text("✨ 3D Mirror Virtual Try-On", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (product.isTrialEligible) {
                    Button(onClick = onBookTrial, modifier = Modifier.fillMaxWidth().height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = WineVelvet), shape = RoundedCornerShape(10.dp)) {
                        Text("👑 Book Trial @Home (₹${state.trialFee})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(onClick = onOpenChat, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, WineVelvet)) {
                    Text("💬 Chat & Bargain with Jeweller", color = WineVelvet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth().clickable { onOpenExchange() }, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, GlassBorder)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("♻️ Exchange Old Jewellery at Purchase (₹0.30–₹0.35/g)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WineDark)
                        Text("➔", color = GoldDeep)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.addToCart(product, negotiatedPrice); onGoToCart() }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.5.dp, WineVelvet)) {
                        Text("Add to Cart", color = WineVelvet, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { vm.addToCart(product, negotiatedPrice); onGoToCart() }, modifier = Modifier.weight(1f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrestige), shape = RoundedCornerShape(10.dp)) {
                        Text("Direct Buy", color = IvorySilky, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(vm: MainViewModel, onBack: () -> Unit, onOpenExchange: () -> Unit, onProceedToCheckout: () -> Unit, onContinueShopping: () -> Unit) {
    val state by vm.state.collectAsState()
    val payable = vm.calculateCartPayable()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart (${state.cart.size} items)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        if (state.cart.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🛍️", fontSize = 50.sp)
                Text("Your Cart is Empty", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onContinueShopping, colors = ButtonDefaults.buttonColors(containerColor = WineVelvet)) { Text("Explore Catalog", color = IvorySilky) }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).verticalScroll(rememberScrollState()).padding(14.dp)) {
                state.cart.forEach { item ->
                    val activePrice = state.negotiatedPrices[item.product.id] ?: item.customPrice ?: item.product.price
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.product.emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                                Text("₹${activePrice.toInt()} each", fontSize = 11.sp, color = WineRich, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { vm.updateCartQty(item.product.id, -1) }) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-", tint = Color.Gray) }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                IconButton(onClick = { vm.updateCartQty(item.product.id, 1) }) { Icon(Icons.Default.AddCircleOutline, contentDescription = "+", tint = WineVelvet) }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Card(colors = CardDefaults.cardColors(containerColor = if (state.appliedExchangeSlip != null) EmeraldSoft else Color.White), border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth().clickable { onOpenExchange() }) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (state.appliedExchangeSlip != null) "✓ Trade-In Discount: -₹${state.appliedExchangeSlip?.netCredit?.toInt()}" else "♻️ Apply Old Jewellery Trade-In Discount", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (state.appliedExchangeSlip != null) EmeraldPrestige else WineDark)
                        if (state.appliedExchangeSlip != null) {
                            TextButton(onClick = { vm.removeExchange() }) { Text("Remove", color = RubyAlert, fontSize = 10.sp) }
                        } else Text("➔", color = GoldDeep)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("BILL SUMMARY", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = GoldDeep)
                        val subtotal = state.cart.sumOf { (state.negotiatedPrices[it.product.id] ?: it.customPrice ?: it.product.price) * it.quantity }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 11.sp); Text("₹${subtotal.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        if (state.appliedExchangeSlip != null) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Trade-In Credit", fontSize = 11.sp, color = EmeraldPrestige); Text("-₹${state.appliedExchangeSlip?.netCredit?.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrestige)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = WineDark); Text("₹${payable.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WineRich)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onProceedToCheckout, colors = ButtonDefaults.buttonColors(containerColor = WineVelvet), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
                    Text("Proceed to Checkout (₹${payable.toInt()})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CheckoutScreen(vm: MainViewModel, onBack: () -> Unit, onOrderPlaced: () -> Unit) {
    val state by vm.state.collectAsState()
    val payable = vm.calculateCartPayable()
    var orderPlacedDialog by remember { mutableStateOf(false) }
    val paymentOptions = listOf("UPI (Google Pay / PhonePe / Paytm)", "Credit / Debit Card", "Net Banking", "Cash on Delivery")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment & Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).verticalScroll(rememberScrollState()).padding(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DELIVERING TO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = GoldDeep)
                    Text("${state.selectedAddress.label} · ${state.selectedAddress.recipientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                    Text(state.selectedAddress.addressLine, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Select Payment Option:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
            Spacer(modifier = Modifier.height(4.dp))
            paymentOptions.forEach { opt ->
                val isSelected = state.selectedPaymentMethod == opt
                Card(colors = CardDefaults.cardColors(containerColor = if (isSelected) GoldLight.copy(alpha = 0.3f) else Color.White), border = BorderStroke(1.dp, if (isSelected) GoldPure else GlassBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { vm.setPaymentMethod(opt) }) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSelected, onClick = { vm.setPaymentMethod(opt) }, colors = RadioButtonDefaults.colors(selectedColor = WineVelvet))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(opt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WineDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = WineVelvet), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FINAL PAYABLE", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("₹${payable.toInt()}", color = GoldLight, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { vm.placeOrder(false); orderPlacedDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrestige), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
                Text("Pay ₹${payable.toInt()} & Place Order", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (orderPlacedDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("🎉 Order Placed Successfully!", fontWeight = FontWeight.Bold, color = WineDark) },
            text = { Text("Your jewellery invoice is confirmed. Insured delivery dispatched shortly.", fontSize = 12.sp) },
            confirmButton = { Button(onClick = { orderPlacedDialog = false; onOrderPlaced() }, colors = ButtonDefaults.buttonColors(containerColor = WineVelvet)) { Text("Track Order") } }
        )
    }
}

@Composable
fun VtoScreen(vm: MainViewModel, onBack: () -> Unit, onBookTrial: () -> Unit, onBuyNow: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val state by vm.state.collectAsState()
    var hasCamera by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }

    LaunchedEffect(Unit) { if (!hasCamera) launcher.launch(Manifest.permission.CAMERA) }

    var jewelryOffset by remember { mutableStateOf(Offset(0f, 60f)) }
    var jewelryScale by remember { mutableFloatStateOf(1.0f) }

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
                        } catch (e: Exception) { e.printStackTrace() }
                    }, ContextCompat.getMainExecutor(ctx))
                    view
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp).align(Alignment.TopStart), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White.copy(0.85f))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark)
            }
            Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, GoldPure)) {
                Text("🪞 Pinch to scale · Drag to fit", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp, 4.dp))
            }
        }
        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> jewelryScale = (jewelryScale * zoom).coerceIn(0.6f, 2.2f); jewelryOffset = Offset(jewelryOffset.x + pan.x, jewelryOffset.y + pan.y) } }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(x = jewelryOffset.x.dp, y = jewelryOffset.y.dp)) {
                Text(state.activeVtoProduct.emoji, fontSize = (85 * jewelryScale).sp)
            }
        }
        Surface(color = Color.White.copy(0.95f), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Select piece to try on:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    items(state.products) { p ->
                        val isSelected = state.activeVtoProduct.id == p.id
                        Surface(shape = RoundedCornerShape(10.dp), color = if (isSelected) GoldLight else IvorySilky, border = BorderStroke(1.dp, if (isSelected) GoldPure else Color.Transparent), modifier = Modifier.size(56.dp).clickable { vm.setVtoProduct(p) }) {
                            Box(contentAlignment = Alignment.Center) { Text(p.emoji, fontSize = 22.sp) }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBookTrial, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.5.dp, WineVelvet)) {
                        Text("Book Trial @Home", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WineVelvet)
                    }
                    Button(onClick = { vm.addToCart(state.activeVtoProduct); onBuyNow() }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = WineVelvet)) {
                        Text("Buy (₹${state.activeVtoProduct.price.toInt()})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerChatScreen(product: Product, vm: MainViewModel, onBack: () -> Unit, onCheckout: () -> Unit) {
    val state by vm.state.collectAsState()
    var msgText by remember { mutableStateOf("") }
    var offerInput by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jaipur Jewels (${product.name.take(12)}…)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky)) {
            LazyColumn(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.chatMessages) { msg ->
                    val isBuyer = msg.senderId == "buyer"
                    Column(horizontalAlignment = if (isBuyer) Alignment.End else Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(12.dp), color = if (isBuyer) WineVelvet else Color.White, border = if (!isBuyer) BorderStroke(1.dp, GlassBorder) else null, modifier = Modifier.widthIn(max = 260.dp)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(msg.text, color = if (isBuyer) IvorySilky else WineDark, fontSize = 12.sp)
                                if (msg.counterAmount != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(onClick = { vm.acceptCounterOffer(product.id, msg.counterAmount); vm.addToCart(product, msg.counterAmount); onCheckout() }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrestige), shape = RoundedCornerShape(6.dp)) {
                                        Text("Accept ₹${msg.counterAmount.toInt()}", color = EmeraldSoft, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = GoldPure), shape = RoundedCornerShape(16.dp)) {
                        Text("Bargain", color = WineDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(value = msgText, onValueChange = { msgText = it }, placeholder = { Text("Message…", fontSize = 11.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = { if (msgText.isNotBlank()) { vm.sendTextMessage(msgText); msgText = "" } }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WineVelvet)
                    }
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Offer to Jeweller") },
            text = { OutlinedTextField(value = offerInput, onValueChange = { offerInput = it }, label = { Text("Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
            confirmButton = { Button(onClick = { val amt = offerInput.toDoubleOrNull(); if (amt != null) vm.sendBargainOffer(product.id, amt); showDialog = false }) { Text("Send") } }
        )
    }
}

@Composable
fun TrialScreen(vm: MainViewModel, onBack: () -> Unit, onOpenAddress: () -> Unit, onOpenExchange: () -> Unit, onConfirm: () -> Unit) {
    val state by vm.state.collectAsState()
    val dist = state.selectedAddress.distanceKm
    val isOutOfRange = dist > 5.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trial @Home Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).verticalScroll(rememberScrollState()).padding(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("DELIVERY LOCATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                        TextButton(onClick = onOpenAddress) { Text("Change", fontSize = 10.sp, color = WineVelvet) }
                    }
                    Text("${state.selectedAddress.label} · ${state.selectedAddress.recipientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                    Text(state.selectedAddress.addressLine, fontSize = 11.sp, color = Color.Gray)
                    Surface(color = if (isOutOfRange) RubyAlert.copy(0.1f) else EmeraldSoft, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Text(if (isOutOfRange) "⚠ $dist km (Exceeds 5km Trial Limit)" else "📍 Rider Distance: $dist km", color = if (isOutOfRange) RubyAlert else EmeraldPrestige, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp, 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = WineVelvet), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("TRIAL FEE", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("₹${state.trialFee}", color = GoldLight, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(color = GoldLight, shape = RoundedCornerShape(6.dp)) {
                            Text("15–20 Mins", color = WineDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp, 3.dp))
                        }
                    }
                    HorizontalDivider(color = WineRich, modifier = Modifier.padding(vertical = 6.dp))
                    Text("• 15–20 mins covered · 5 min grace · ₹1/min overage.\n• Fee waived against bill if you keep an item.", color = IvorySilky, fontSize = 10.sp, lineHeight = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Select Slot:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WineDark)
            val slots = listOf("Today, 4–6 PM", "Tomorrow, 10–12 PM", "Tomorrow, 4–6 PM")
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                slots.forEach { s ->
                    val isSelected = state.selectedSlot == s
                    Box(modifier = Modifier.weight(1f).background(if (isSelected) WineVelvet else Color.White, RoundedCornerShape(8.dp)).border(1.dp, if (isSelected) WineVelvet else GlassBorder, RoundedCornerShape(8.dp)).clickable { vm.setSlot(s) }.padding(6.dp), contentAlignment = Alignment.Center) {
                        Text(s, fontSize = 9.sp, color = if (isSelected) IvorySilky else WineDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isOutOfRange) {
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(44.dp)) { Text("Out of 5km Trial Range", fontSize = 11.sp) }
            } else {
                Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = WineVelvet), shape = RoundedCornerShape(10.dp)) {
                    Text("Confirm Trial Booking (Pay ₹${state.trialFee})", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ExchangeScreen(vm: MainViewModel, onBack: () -> Unit, onAppliedGoToCart: () -> Unit) {
    var itemName by remember { mutableStateOf("") }
    var gramsInput by remember { mutableStateOf("100") }
    var photoCaptured by remember { mutableStateOf(false) }

    val grams = gramsInput.toDoubleOrNull() ?: 0.0
    val netCredit = (grams * 0.35) * 0.90

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jewellery Exchange", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).verticalScroll(rememberScrollState()).padding(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = EmeraldSoft), shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("• ₹0.30–₹0.35/g trade-in credit.\n• 10% purity deduction.\n• No plastic/synthetic items.", fontSize = 10.sp, lineHeight = 15.sp, color = EmeraldPrestige)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Description") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = gramsInput, onValueChange = { gramsInput = it }, label = { Text("Weight (Grams)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { photoCaptured = true }, colors = ButtonDefaults.buttonColors(containerColor = if (photoCaptured) EmeraldPrestige else WineDark), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text(if (photoCaptured) "✓ Live Photo Attached" else "📸 Take Live Photo", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = WineVelvet), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ESTIMATED CREDIT", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("₹${(netCredit * 100).toInt() / 100.0}", color = GoldLight, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = { vm.applyExchange(itemName, grams); onAppliedGoToCart() }, modifier = Modifier.fillMaxWidth().height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldPure), shape = RoundedCornerShape(10.dp)) {
                Text("Apply to Cart & Deduct from Bill", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OrdersScreen(vm: MainViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val elapsed = state.trialSecondsElapsed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders & Live Concierge", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).verticalScroll(rememberScrollState()).padding(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1ECE4)), modifier = Modifier.fillMaxWidth().height(90.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("🛵 Rider 'Vikram' is en route (OTP: 4812)", fontWeight = FontWeight.Bold, color = WineDark, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = WineDark), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!state.isTrialActive) {
                        Text("Start Tryout Timer", color = IvorySilky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(onClick = { vm.startTrial() }, colors = ButtonDefaults.buttonColors(containerColor = GoldPure), modifier = Modifier.padding(top = 6.dp)) {
                            Text("Start Timer on Unseal", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        val status = when { elapsed <= 20 -> "Trial Active (${20 - elapsed}:00 left)"; elapsed <= 25 -> "Grace Period (${25 - elapsed}:00 left)"; else -> "Overage: ₹${elapsed - 25} (₹1/min)" }
                        Text(status, color = GoldLight, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { vm.stopTrial() }, colors = ButtonDefaults.buttonColors(containerColor = IvorySilky)) { Text("End Tryout", color = WineDark, fontSize = 11.sp) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Past Orders", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WineDark)
            state.orderHistory.forEach { o ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(o.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(o.productName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WineDark)
                            Text("${o.status} · ${o.date}", fontSize = 9.sp, color = Color.Gray)
                        }
                        Text("₹${o.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineRich)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(vm: MainViewModel, onOpenAddressBook: () -> Unit, onOpenOrders: () -> Unit, onLogout: () -> Unit) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(IvorySilky).verticalScroll(rememberScrollState())) {
        Surface(color = WineVelvet, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                        Text("SK", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WineDark)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("${state.userFirstName} ${state.userLastName}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IvorySilky)
                        Text(state.userPhone, fontSize = 11.sp, color = GoldLight)
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text("ACCOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOpenAddressBook() }.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("📍 Saved Addresses", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark); Text("➔", color = GoldDeep)
                    }
                    HorizontalDivider(color = Color(0xFFF1ECE4))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOpenOrders() }.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("📦 Order & Trial History", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark); Text("➔", color = GoldDeep)
                    }
                    HorizontalDivider(color = Color(0xFFF1ECE4))
                    Row(modifier = Modifier.fillMaxWidth().clickable { vm.logout(); onLogout() }.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🚪 Logout", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RubyAlert); Text("➔", color = RubyAlert)
                    }
                }
            }
        }
    }
}

@Composable
fun AddressBookScreen(vm: MainViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Addresses", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WineDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WineDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IvorySilky)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(IvorySilky).padding(14.dp)) {
            Button(onClick = { vm.autoDetectGps() }, colors = ButtonDefaults.buttonColors(containerColor = GoldPure), modifier = Modifier.fillMaxWidth()) {
                Text("📍 Auto-Detect Live GPS Location", color = WineDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            state.addresses.forEach { addr ->
                val isSelected = state.selectedAddress.id == addr.id
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { vm.selectAddress(addr) },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) GoldLight.copy(alpha = 0.25f) else Color.White),
                    border = BorderStroke(1.dp, if (isSelected) GoldPure else GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${addr.label} · ${addr.recipientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WineDark)
                            if (isSelected) Surface(color = WineVelvet, shape = RoundedCornerShape(4.dp)) { Text("Active", color = IvorySilky, fontSize = 9.sp, modifier = Modifier.padding(3.dp, 1.dp)) }
                        }
                        Text(addr.addressLine, fontSize = 11.sp, color = Color.DarkGray)
                        Text(if (addr.distanceKm > 5.0) "Distance: ${addr.distanceKm} km (Out of 5km Trial Limit)" else "Distance: ${addr.distanceKm} km (Eligible for Trial @Home)", color = if (addr.distanceKm > 5.0) RubyAlert else EmeraldPrestige, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}
