@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.roldygoldy.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
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

// ==========================================
// 🎨 COMPLETE PALETTE & STYLING
// ==========================================
val DarkCanvas = Color(0xFF120C07)
val DarkCard = Color(0xFF1C130D)
val DarkSurface = Color(0xFF2B1E15)
val GoldPrimary = Color(0xFFE5A93C)
val GoldLight = Color(0xFFF7D992)
val GoldDark = Color(0xFF9E6B18)
val LightCanvas = Color(0xFFFDFBF7)
val LightCard = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFEFE8DC)
val TextDark = Color(0xFF1F1610)
val TextMuted = Color(0xFF8C7E72)
val EmeraldSuccess = Color(0xFF1B6B46)
val EmeraldSoft = Color(0xFFE2F4EB)
val RubyRed = Color(0xFFA62435)
val WineVelvet = Color(0xFF4A0A28)
val WineRich = Color(0xFF6B1139)
val IvorySilky = Color(0xFFFAF6EE)
val GlassBorder = Color(0x33D4AF37)

val DarkObsidianGrad = Brush.verticalGradient(listOf(DarkSurface, DarkCard, DarkCanvas))

@Composable
fun RoldyGoldyLogo(size: Int = 38, showSubtitle: Boolean = false, isDarkBg: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("RoldyGoldy", fontSize = size.sp, fontWeight = FontWeight.Bold, color = if (isDarkBg) GoldLight else GoldDark, letterSpacing = 1.sp)
        if (showSubtitle) {
            Text("HER PRIDE • HER CHOICE • HER TRUST", fontSize = (size * 0.22).sp, fontWeight = FontWeight.Bold, color = if (isDarkBg) GoldPrimary else TextMuted, letterSpacing = 2.sp)
        }
    }
}

// ==========================================
// 📦 DATA MODELS & PRODUCT LIST
// ==========================================
data class Product(
    val id: String, val name: String, val category: String, val price: Double, val originalPrice: Double,
    val emoji: String, val isTrialEligible: Boolean, val tag: String, val karatInfo: String,
    val grossWeight: String, val netWeight: String, val metalType: String, val stoneType: String,
    val size: String, val closure: String, val hallmark: String, val description: String
)

data class CartItem(val product: Product, var quantity: Int = 1, var customPrice: Double? = null)
data class Address(val id: String, val label: String, val recipientName: String, val addressLine: String, val pincode: String, val distanceKm: Double, val isDefault: Boolean = false)
data class ChatMessage(val id: String, val senderId: String, val text: String, val timestamp: String, val isOffer: Boolean = false, val counterAmount: Double? = null)
data class OrderHistoryItem(val orderId: String, val productName: String, val emoji: String, val amount: Double, val status: String, val date: String, val isTrial: Boolean)
data class ExchangeSlip(val id: String, val materialCategory: String, val itemName: String, val weightGrams: Double, val grossValue: Double, val netCredit: Double, val otp: String = "ROEX123456", val date: String, val photoBitmap: Bitmap? = null)
data class OnboardingSlide(val title: String, val subtitle: String, val emoji: String, val badge: String)

val onboardingSlides = listOf(
    OnboardingSlide("3D Virtual Try-On", "Live AR camera tryout for necklaces, hoops & maangtikkas directly on your selfie camera.", "🪞", "Virtual Mirror"),
    OnboardingSlide("Trial @Home Concierge", "Try real pieces at your doorstep for 15–20 minutes with our insured trial kit.", "👑", "Doorstep Tryout"),
    OnboardingSlide("Old Jewellery Exchange", "Trade in old rolled-gold or broken items for ₹0.30–₹0.35/g credit deducted straight from your cart bill.", "♻️", "Trade-In Discount"),
    OnboardingSlide("Live Jeweller Bargaining", "Chat directly with master artisans, send custom offers, and lock handcrafted discount deals in real time.", "💬", "P2P Negotiation")
)

val sampleProducts = listOf(
    Product("1", "Kundan Choker Necklace", "Bridal", 3499.0, 4899.0, "👑", true, "HOT", "1-Gram Matte Gold Plated", "48.5 g", "36.2 g", "Brass & Copper Alloy", "Kundan & Hydro Emeralds", "12–18 in", "Golden Zari Dori", "RG 1-Gram Certified", "Royal bridal choker set handcrafted with precision Kundan foil work."),
    Product("2", "Korean Minimal Hoops", "Daily Wear", 349.0, 549.0, "💫", false, "NEW", "18K PVD Anti-Tarnish", "6.8 g", "6.8 g", "316L Surgical Titanium", "AAA+ Cubic Zirconia", "18mm Diameter", "Click-top Security Latch", "18K PVD Certified", "Waterproof and hypoallergenic daily-wear hoops."),
    Product("3", "Temple Deity Jhumka Set", "Temple", 1299.0, 1899.0, "🪔", true, "BESTSELLER", "Antique Micro-Gold Plated", "38.2 g", "30.5 g", "Bronze Alloy", "Kemp Stones & Pearls", "14–16 in", "Metallic Lobster Lock", "Micro-Gold Guaranteed Finish", "South Indian temple motif with ruby-red kemp stones."),
    Product("4", "Polki Bridal Maangtikka", "Bridal", 2899.0, 3599.0, "💍", true, "HOT", "Uncut Polki Foil Finish", "22.4 g", "18.1 g", "Copper & Silver Alloy", "Uncut Polki & Onyx Drops", "5.5 in Length", "Curved Hairpin Anchor Hook", "Artisan Certified", "Floral bridal maangtikka with silver foil polki setting.")
)

val sampleAddresses = listOf(
    Address("a1", "Home", "Meera Sharma", "21-1-564, Lakdi Ka Pul, Hyderabad", "500001", 2.4, isDefault = true),
    Address("a2", "Office", "Meera Sharma", "Mindspace, Hitech City, Hyderabad", "500081", 4.1, isDefault = false)
)

// ==========================================
// 🧠 VIEWMODEL & APP STATE
// ==========================================
data class AppState(
    val isFirstTimeUser: Boolean = true,
    val isLoggedIn: Boolean = false,
    val userFullName: String = "Meera Sharma",
    val userPhone: String = "+91 98765 43210",
    val selectedPincode: String = "500101",
    val addresses: List<Address> = sampleAddresses,
    val selectedAddress: Address = sampleAddresses[0],
    val products: List<Product> = sampleProducts,
    val selectedCategory: String = "All",
    val trialOnlyFilter: Boolean = false,
    val cart: List<CartItem> = listOf(CartItem(sampleProducts[0], 1), CartItem(sampleProducts[1], 1)),
    val trialKit: List<Product> = listOf(sampleProducts[0], sampleProducts[2], sampleProducts[3]),
    val wishlist: List<Product> = listOf(sampleProducts[0], sampleProducts[2], sampleProducts[3]),
    val activeVtoProduct: Product = sampleProducts[0],
    val appliedExchangeSlip: ExchangeSlip? = null,
    val exchangeLivePhoto: Bitmap? = null,
    val negotiatedPrices: Map<String, Double> = emptyMap(),
    val orderHistory: List<OrderHistoryItem> = listOf(
        OrderHistoryItem("RGORD123456", "Kundan Choker Necklace", "👑", 3499.0, "Order Confirmed", "28 May", false),
        OrderHistoryItem("RGTR123456", "Trial Box (3 Pieces)", "👑", 99.0, "Trial Scheduled (27 May, 4-7 PM)", "27 May", true)
    ),
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage("1", "seller", "Namaste Meera! How can I assist you with this bridal collection?", "10:30 AM")
    ),
    val trialFee: Int = 99,
    val selectedDate: String = "27 May, Tue",
    val selectedSlot: String = "Evening (04:00 PM - 07:00 PM)",
    val trialSecondsElapsed: Int = 0,
    val isTrialActive: Boolean = false,
    val selectedPaymentMethod: String = "UPI - Google Pay"
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private var timerJob: Job? = null

    fun completeOnboarding() { _state.update { it.copy(isFirstTimeUser = false) } }
    fun login(phone: String, name: String) { _state.update { it.copy(isLoggedIn = true, userPhone = phone, userFullName = name) } }
    fun logout() { _state.update { it.copy(isLoggedIn = false) } }
    fun setCategory(cat: String) { _state.update { it.copy(selectedCategory = cat) } }
    fun toggleTrialFilter(on: Boolean) { _state.update { it.copy(trialOnlyFilter = on) } }
    fun setVtoProduct(p: Product) { _state.update { it.copy(activeVtoProduct = p) } }
    fun selectAddress(address: Address) { _state.update { it.copy(selectedAddress = address) } }
    fun setSlot(date: String, slot: String) { _state.update { it.copy(selectedDate = date, selectedSlot = slot) } }

    fun autoDetectGps() {
        val detected = Address("gps", "Current Location (Live GPS)", _state.value.userFullName, "Lakdi Ka Pul, Hyderabad", "500001", 1.8, isDefault = true)
        _state.update { it.copy(addresses = listOf(detected) + it.addresses, selectedAddress = detected) }
    }

    fun toggleWishlist(product: Product) {
        val current = _state.value.wishlist.toMutableList()
        if (current.any { it.id == product.id }) current.removeAll { it.id == product.id }
        else current.add(product)
        _state.update { it.copy(wishlist = current) }
    }

    fun isWishlisted(id: String): Boolean = _state.value.wishlist.any { it.id == id }

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

    fun addProductToTrialKit(product: Product) {
        if (!product.isTrialEligible) return
        val current = _state.value.trialKit.toMutableList()
        if (current.none { it.id == product.id } && current.size < 4) {
            current.add(product)
            _state.update { it.copy(trialKit = current) }
        }
    }

    fun removeProductFromTrialKit(productId: String) {
        val current = _state.value.trialKit.toMutableList()
        current.removeAll { it.id == productId }
        _state.update { it.copy(trialKit = current) }
    }

    fun setExchangePhoto(bitmap: Bitmap) { _state.update { it.copy(exchangeLivePhoto = bitmap) } }

    fun applyExchange(category: String, itemName: String, weightGrams: Double) {
        val net = (weightGrams * 3.1) * 0.90
        val slip = ExchangeSlip(
            id = "ROEX123456",
            materialCategory = category,
            itemName = itemName.ifBlank { "$category ($weightGrams g)" },
            weightGrams = weightGrams,
            grossValue = weightGrams * 3.1,
            netCredit = (net * 100).toInt() / 100.0,
            date = "27 May, Tue",
            photoBitmap = _state.value.exchangeLivePhoto
        )
        _state.update { it.copy(appliedExchangeSlip = slip) }
    }

    fun removeExchange() { _state.update { it.copy(appliedExchangeSlip = null, exchangeLivePhoto = null) } }

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
        val orderId = if (isTrial) "RGTR123456" else "RGORD123456"
        val firstItem = if (isTrial) _state.value.trialKit.firstOrNull() ?: sampleProducts[0] else _state.value.cart.firstOrNull()?.product ?: sampleProducts[0]
        val total = calculateCartPayable()
        val newOrder = OrderHistoryItem(orderId, if (isTrial) "Trial Kit (${_state.value.trialKit.size} pieces)" else firstItem.name, firstItem.emoji, if (isTrial) _state.value.trialFee.toDouble() else total, "Order Confirmed", "Today", isTrial)
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

// ==========================================
// 🚀 NAVIGATION ROUTER
// ==========================================
sealed class Screen(val route: String, val title: String) {
    data object Splash : Screen("splash", "Splash")
    data object Onboarding1 : Screen("onboarding1", "Onboarding 1")
    data object Onboarding2 : Screen("onboarding2", "Onboarding 2")
    data object Onboarding3 : Screen("onboarding3", "Onboarding 3")
    data object AuthPhone : Screen("auth_phone", "Login")
    data object OtpVerify : Screen("otp_verify", "Verify OTP")
    data object Home : Screen("home", "Home")
    data object Categories : Screen("categories", "Categories")
    data object CategoryListing : Screen("cat_listing/{catName}", "Collections") { fun create(c: String) = "cat_listing/$c" }
    data object ProductDetail : Screen("pdp/{productId}", "Product Detail") { fun create(id: String) = "pdp/$id" }
    data object TryAtHomeInfo : Screen("trial_info", "Trial")
    data object SelectSlot : Screen("select_slot", "Select Slot")
    data object TrialCart : Screen("trial_cart", "Trial Cart")
    data object TrialAddress : Screen("trial_address", "Delivery Address")
    data object TrialConfirmed : Screen("trial_confirmed", "Trial Confirmed")
    data object ExchangeHome : Screen("exchange_home", "Exchange")
    data object ExchangeQuoteScreen : Screen("exchange_quote", "Your Quote")
    data object ExchangeConfirmed : Screen("exchange_confirmed", "Exchange Confirmed")
    data object Cart : Screen("cart", "My Cart")
    data object Checkout : Screen("checkout", "Checkout")
    data object OrderConfirmed : Screen("order_confirmed", "Order Confirmed")
    data object OrderTracking : Screen("order_tracking", "Track Order")
    data object TrialExperienceLive : Screen("trial_live", "Trial Live")
    data object ReturnsAfterTrial : Screen("trial_returns", "Return Items")
    data object Profile : Screen("profile", "Account")
    data object Wishlist : Screen("wishlist", "Wishlist")
    data object HelpSupport : Screen("help_support", "Help & Support")
    data object VirtualTryOn : Screen("vto", "3D Mirror")
    data object SellerChat : Screen("chat/{productId}", "Chat") { fun create(pId: String) = "chat/$pId" }
    data object AddressBook : Screen("address_book", "Addresses")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = GoldPrimary, background = LightCanvas, surface = LightCard)) {
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

    val topNavTabs = listOf(Screen.Home, Screen.Categories, Screen.TryAtHomeInfo, Screen.ExchangeHome, Screen.Profile)
    val isBottomBarVisible = currentRoute in topNavTabs.map { it.route }

    BackHandler(enabled = currentRoute != Screen.Home.route && currentRoute != Screen.Splash.route && currentRoute != Screen.AuthPhone.route) {
        if (!isBottomBarVisible) navController.popBackStack()
        else navController.navigate(Screen.Home.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible && state.isLoggedIn) {
                NavigationBar(containerColor = LightCard, tonalElevation = 8.dp, modifier = Modifier.border(1.dp, LightBorder)) {
                    topNavTabs.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        val icon = when(screen) {
                            Screen.Home -> Icons.Default.Home
                            Screen.Categories -> Icons.Default.Menu
                            Screen.TryAtHomeInfo -> Icons.Default.DateRange
                            Screen.ExchangeHome -> Icons.Default.Refresh
                            else -> Icons.Default.Person
                        }
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.TryAtHomeInfo && state.trialKit.isNotEmpty()) {
                                    BadgedBox(badge = { Badge(containerColor = GoldPrimary) { Text("${state.trialKit.size}", color = DarkCanvas) } }) {
                                        Icon(icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldDark, selectedTextColor = GoldDark, unselectedIconColor = TextMuted, unselectedTextColor = TextMuted, indicatorColor = GoldLight.copy(alpha = 0.4f)),
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
        val startScreen = when { state.isFirstTimeUser -> Screen.Splash.route; !state.isLoggedIn -> Screen.AuthPhone.route; else -> Screen.Home.route }

        NavHost(navController = navController, startDestination = startScreen, modifier = Modifier.padding(padding)) {
            composable(Screen.Splash.route) { SplashScreen { navController.navigate(if (state.isFirstTimeUser) Screen.Onboarding1.route else if (!state.isLoggedIn) Screen.AuthPhone.route else Screen.Home.route) } }
            composable(Screen.Onboarding1.route) { OnboardingScreen1 { navController.navigate(Screen.Onboarding2.route) } }
            composable(Screen.Onboarding2.route) { OnboardingScreen2 { navController.navigate(Screen.Onboarding3.route) } }
            composable(Screen.Onboarding3.route) { OnboardingScreen3 { vm.completeOnboarding(); navController.navigate(Screen.AuthPhone.route) } }
            composable(Screen.AuthPhone.route) { AuthPhoneScreen { navController.navigate(Screen.OtpVerify.route) } }
            composable(Screen.OtpVerify.route) { OtpVerifyScreen(state.userPhone) { vm.login(state.userPhone, "Meera Sharma"); navController.navigate(Screen.Home.route) } }
            composable(Screen.Home.route) { HomeScreen(vm, { navController.navigate(Screen.ProductDetail.create(it)) }, { navController.navigate(Screen.TryAtHomeInfo.route) }, { navController.navigate(Screen.Cart.route) }, { navController.navigate(Screen.Wishlist.route) }, { navController.navigate(Screen.Categories.route) }, { navController.navigate(Screen.VirtualTryOn.route) }) }
            composable(Screen.Categories.route) { CategoriesScreen { cat -> vm.setCategory(cat); navController.navigate(Screen.CategoryListing.create(cat)) } }
            composable(Screen.CategoryListing.route, arguments = listOf(navArgument("catName") { type = NavType.StringType })) { entry ->
                val cat = entry.arguments?.getString("catName") ?: "Bridal Collections"
                CategoryListingScreen(cat, vm, { navController.popBackStack() }, { navController.navigate(Screen.ProductDetail.create(it)) })
            }
            composable(Screen.ProductDetail.route, arguments = listOf(navArgument("productId") { type = NavType.StringType })) { entry ->
                val pId = entry.arguments?.getString("productId") ?: "1"
                val product = vm.state.collectAsState().value.products.find { it.id == pId } ?: sampleProducts[0]
                ProductDetailScreen(product, vm, { navController.popBackStack() }, { vm.addProductToTrialKit(product); navController.navigate(Screen.TryAtHomeInfo.route) }, { vm.addToCart(product); navController.navigate(Screen.Cart.route) }, { navController.navigate(Screen.SellerChat.create(product.id)) }, { vm.setVtoProduct(product); navController.navigate(Screen.VirtualTryOn.route) })
            }
            composable(Screen.TryAtHomeInfo.route) { TryAtHomeInfoScreen { navController.navigate(Screen.SelectSlot.route) } }
            composable(Screen.SelectSlot.route) { SelectTimeSlotScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.TrialCart.route) }) }
            composable(Screen.TrialCart.route) { TrialCartScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.TrialAddress.route) }, { navController.navigate(Screen.Categories.route) }) }
            composable(Screen.TrialAddress.route) { TrialAddressScreen(vm, { navController.popBackStack() }, { vm.placeOrder(true); navController.navigate(Screen.TrialConfirmed.route) }) }
            composable(Screen.TrialConfirmed.route) { TrialConfirmedScreen(vm, { navController.navigate(Screen.TrialExperienceLive.route) }, { navController.navigate(Screen.Home.route) }) }
            composable(Screen.ExchangeHome.route) { ExchangeHomeScreen(vm) { navController.navigate(Screen.ExchangeQuoteScreen.route) } }
            composable(Screen.ExchangeQuoteScreen.route) { ExchangeQuoteScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.ExchangeConfirmed.route) }) }
            composable(Screen.ExchangeConfirmed.route) { ExchangeConfirmedScreen({ navController.navigate(Screen.OrderTracking.route) }, { navController.navigate(Screen.Home.route) }) }
            composable(Screen.Cart.route) { CartScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.Checkout.route) }, { navController.navigate(Screen.Home.route) }) }
            composable(Screen.Checkout.route) { CheckoutScreen(vm, { navController.popBackStack() }, { vm.placeOrder(false); navController.navigate(Screen.OrderConfirmed.route) }) }
            composable(Screen.OrderConfirmed.route) { OrderConfirmedScreen({ navController.navigate(Screen.OrderTracking.route) }, { navController.navigate(Screen.Home.route) }) }
            composable(Screen.OrderTracking.route) { OrderTrackingScreen(vm) { navController.popBackStack() } }
            composable(Screen.TrialExperienceLive.route) { TrialExperienceLiveScreen({ navController.navigate(Screen.Cart.route) }, { navController.navigate(Screen.ReturnsAfterTrial.route) }) }
            composable(Screen.ReturnsAfterTrial.route) { ReturnsAfterTrialScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.Home.route) }) }
            composable(Screen.Profile.route) { ProfileScreen(vm, { navController.navigate(Screen.OrderTracking.route) }, { navController.navigate(Screen.TrialExperienceLive.route) }, { navController.navigate(Screen.ExchangeHome.route) }, { navController.navigate(Screen.Wishlist.route) }, { navController.navigate(Screen.HelpSupport.route) }, { navController.navigate(Screen.AddressBook.route) }, { navController.navigate(Screen.AuthPhone.route) }) }
            composable(Screen.Wishlist.route) { WishlistScreen(vm, { navController.popBackStack() }, { p -> vm.addToCart(p); vm.toggleWishlist(p); navController.navigate(Screen.Cart.route) }) }
            composable(Screen.HelpSupport.route) { HelpSupportScreen { navController.popBackStack() } }
            composable(Screen.VirtualTryOn.route) { VirtualTryOnScreen(vm, { navController.popBackStack() }, { navController.navigate(Screen.TryAtHomeInfo.route) }, { navController.navigate(Screen.Cart.route) }) }
            composable(Screen.AddressBook.route) { AddressBookScreen(vm) { navController.popBackStack() } }
            composable(Screen.SellerChat.route, arguments = listOf(navArgument("productId") { type = NavType.StringType })) { entry ->
                val pId = entry.arguments?.getString("productId") ?: "1"
                val product = vm.state.collectAsState().value.products.find { it.id == pId } ?: sampleProducts[0]
                SellerChatScreen(product, vm, { navController.popBackStack() }, { navController.navigate(Screen.Cart.route) })
            }
        }
    }
}

// ==========================================
// 📱 SCREEN COMPOSABLES (01 TO 30)
// ==========================================

// --- 01. SPLASH ---
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) { delay(2400); onFinish() }
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RoldyGoldyLogo(size = 38, showSubtitle = true, isDarkBg = true)
            Spacer(modifier = Modifier.height(48.dp))
            Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = 0.12f)).border(2.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                Text("👑", fontSize = 84.sp)
            }
        }
    }
}

// --- 02. ONBOARDING 1 ---
@Composable
fun OnboardingScreen1(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(190.dp).clip(RoundedCornerShape(24.dp)).background(GoldPrimary.copy(alpha = 0.15f)).border(1.5.dp, GoldPrimary, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Text("✨", fontSize = 84.sp)
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text("For Every You,\nFor Every Moment.", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LightCanvas, textAlign = TextAlign.Center, lineHeight = 30.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("From everyday elegance to dreamy bridal looks.", fontSize = 12.sp, color = GoldLight.copy(alpha = 0.85f), textAlign = TextAlign.Center)
        }
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp).align(Alignment.BottomCenter)) {
            Text("Next", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// --- 03. ONBOARDING 2 ---
@Composable
fun OnboardingScreen2(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingCard("👑", "Try at Home", "Book your favorite jewellery & try 3–4 pieces at home.")
            Spacer(modifier = Modifier.height(12.dp))
            OnboardingCard("♻️", "Exchange & Save", "Exchange your old jewellery and get instant cashback.")
            Spacer(modifier = Modifier.height(12.dp))
            OnboardingCard("🛡️", "Trusted Quality", "Premium imitation jewellery with 6 months warranty.")
        }
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp).align(Alignment.BottomCenter)) {
            Text("Next", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun OnboardingCard(icon: String, title: String, desc: String) {
    Card(colors = CardDefaults.cardColors(containerColor = DarkCard), border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldLight)
                Text(desc, fontSize = 11.sp, color = LightCanvas.copy(alpha = 0.8f), lineHeight = 15.sp)
            }
        }
    }
}

// --- 04. ONBOARDING 3 ---
@Composable
fun OnboardingScreen3(onGetStarted: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Her Choice.\nOur Promise.", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = LightCanvas, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            PromiseRow("1 Lakh+ Happy Customers")
            PromiseRow("Secure Payments")
            PromiseRow("Easy Returns")
            PromiseRow("Pan India Delivery")
        }
        Button(onClick = onGetStarted, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp).align(Alignment.BottomCenter)) {
            Text("Get Started", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PromiseRow(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LightCanvas)
    }
}

// --- 05. AUTH - PHONE ---
@Composable
fun AuthPhoneScreen(onContinue: (String) -> Unit) {
    var phoneInput by remember { mutableStateOf("98765 43210") }
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome to\nRoldyGoldy", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Login / Sign up", fontSize = 12.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(30.dp))
        Text("Enter your mobile number", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth().border(1.dp, LightBorder, RoundedCornerShape(10.dp)).background(LightCard).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("+91", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) { Text(phoneInput, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.SemiBold) }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = { onContinue(phoneInput) }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Continue", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("or continue with", fontSize = 11.sp, color = TextMuted) }
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            SocialBtn("G"); Spacer(modifier = Modifier.width(12.dp)); SocialBtn("f"); Spacer(modifier = Modifier.width(12.dp)); SocialBtn("")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("By continuing, you agree to our\nTerms & Conditions & Privacy Policy", fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun SocialBtn(label: String) {
    Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(LightCard).border(1.dp, LightBorder, CircleShape), contentAlignment = Alignment.Center) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
    }
}

// --- 06. OTP VERIFICATION ---
@Composable
fun OtpVerifyScreen(phone: String, onVerified: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(24.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Verify OTP", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Enter the 6 digit code sent to\n$phone", fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
        Spacer(modifier = Modifier.height(26.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("2", "4", "6", "8", "1", "1").forEach { digit ->
                Box(modifier = Modifier.size(42.dp).border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)).background(LightCard), contentAlignment = Alignment.Center) {
                    Text(digit, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("Resend OTP in 00:28", fontSize = 11.sp, color = TextMuted) }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Auto detecting OTP 246811 ✓", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onVerified, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Verify & Continue", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// --- 07. HOME ---
@Composable
fun HomeScreen(vm: MainViewModel, onOpenPdp: (String) -> Unit, onOpenTrial: () -> Unit, onOpenCart: () -> Unit, onOpenWishlist: () -> Unit, onOpenCategories: () -> Unit, onOpenVto: () -> Unit) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).verticalScroll(rememberScrollState())) {
        Surface(color = LightCard, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    RoldyGoldyLogo(size = 20, showSubtitle = false, isDarkBg = false)
                    Row {
                        IconButton(onClick = onOpenWishlist) { Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist", tint = TextDark) }
                        IconButton(onClick = onOpenCart) {
                            BadgedBox(badge = { if (state.cart.isNotEmpty()) Badge(containerColor = GoldPrimary) { Text("${state.cart.size}", color = DarkCanvas) } }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = TextDark)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Deliver to ", fontSize = 10.sp, color = TextMuted)
                        Text(state.selectedPincode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Row {
                        IconButton(onClick = {}, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextDark, modifier = Modifier.size(18.dp)) }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = onOpenVto, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Star, contentDescription = "AR", tint = GoldDark, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Trial @Home", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                    Text("Try up to 4 pieces\nat your doorstep", fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenTrial, colors = ButtonDefaults.buttonColors(containerColor = DarkCanvas), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(6.dp), modifier = Modifier.height(28.dp)) {
                        Text("BOOK NOW", fontSize = 10.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.size(68.dp).clip(RoundedCornerShape(10.dp)).background(GoldLight.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Text("👑", fontSize = 36.sp)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            HomeCatCircle("Daily Wear", "💫") { onOpenCategories() }
            HomeCatCircle("Korean", "✨") { onOpenCategories() }
            HomeCatCircle("Temple", "🪔") { onOpenCategories() }
            HomeCatCircle("Bridal", "👑") { onOpenCategories() }
            HomeCatCircle("New In", "💍") { onOpenCategories() }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Best Sellers", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
            Text("See All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.clickable { onOpenCategories() })
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            sampleProducts.take(2).forEach { p ->
                Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).clickable { onOpenPdp(p.id) }) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(105.dp).background(LightCanvas), contentAlignment = Alignment.Center) {
                            Text(p.emoji, fontSize = 42.sp)
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(p.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1)
                            Text("₹${p.price.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeCatCircle(title: String, icon: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(LightCard).border(1.dp, LightBorder, CircleShape), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

// --- 08. CATEGORIES ---
@Composable
fun CategoriesScreen(onSelectCategory: (String) -> Unit) {
    val categories = listOf("Daily Wear" to "💫", "Korean" to "✨", "Temple" to "🪔", "Bridal" to "👑", "Necklaces" to "📿", "Earrings" to "💎", "Bangles" to "⭕", "Rings" to "💍", "Mangalsutra" to "🖤", "Sets" to "🎁", "Anklets" to "🦶", "Accessories" to "🪞")
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas)) {
        Surface(color = LightCard, shadowElevation = 1.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Categories", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark) }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { (cat, emoji) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelectCategory(cat) }) {
                    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(LightCard).border(1.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(cat, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// --- 09. CATEGORY LISTING ---
@Composable
fun CategoryListingScreen(catName: String, vm: MainViewModel, onBack: () -> Unit, onOpenPdp: (String) -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas)) {
        Surface(color = LightCard, shadowElevation = 1.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark) }
                Text(catName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
            }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.products) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), shape = RoundedCornerShape(10.dp), modifier = Modifier.clickable { onOpenPdp(p.id) }) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(LightCanvas), contentAlignment = Alignment.Center) {
                            Text(p.emoji, fontSize = 44.sp)
                            Surface(color = GoldPrimary, shape = RoundedCornerShape(4.dp), modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                                Text(p.tag, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DarkCanvas, modifier = Modifier.padding(4.dp, 2.dp))
                            }
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(p.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1)
                            Text("₹${p.price.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}

// --- 10. PRODUCT DETAIL (PDP) ---
@Composable
fun ProductDetailScreen(product: Product, vm: MainViewModel, onBack: () -> Unit, onTryAtHome: () -> Unit, onBuyNow: () -> Unit, onOpenChat: () -> Unit, onOpenVto: () -> Unit) {
    val isWish = vm.isWishlisted(product.id)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark) } },
                actions = {
                    IconButton(onClick = { vm.toggleWishlist(product) }) {
                        Icon(if (isWish) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Wishlist", tint = if (isWish) RubyRed else TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightCanvas)
            )
        },
        bottomBar = {
            Surface(color = LightCard, shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onTryAtHome, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GoldDark)) {
                        Text("Try @Home", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(onClick = onBuyNow, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = DarkCanvas)) {
                        Text("Buy Now", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightCanvas).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(LightCard, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(product.emoji, fontSize = 80.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text("₹${product.price.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text("₹${product.originalPrice.toInt()}", fontSize = 12.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
            }
            Text("Inclusive of all taxes", fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PdpPill("Premium Quality"); PdpPill("Skin Friendly"); PdpPill("6 Months Warranty")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenVto, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("✨ 3D Mirror", fontSize = 11.sp, color = DarkCanvas, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onOpenChat, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, DarkCanvas)) {
                    Text("💬 Bargain Chat", fontSize = 11.sp, color = DarkCanvas, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PdpPill(text: String) {
    Surface(color = LightCard, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, LightBorder)) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
    }
}

// --- 11. TRY @HOME INFO ---
@Composable
fun TryAtHomeInfoScreen(onBookTrial: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Text("Trial @Home", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("How it works?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                TrialStepRow("1", "Book", "Select 3-4 jewellery pieces and choose time slot.")
                TrialStepRow("2", "Try", "We deliver at your doorstep. Try for 20-30 minutes.")
                TrialStepRow("3", "Decide", "Buy what you love. Return the rest.")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Trial Fee (Nominal)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                Text("₹99 per slot (Adjustable on purchase)", fontSize = 10.sp, color = TextMuted)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBookTrial, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Book Trial", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun TrialStepRow(num: String, title: String, desc: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(GoldPrimary), contentAlignment = Alignment.Center) {
            Text(num, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkCanvas)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
            Text(desc, fontSize = 10.sp, color = TextMuted)
        }
    }
}

// --- 12. SELECT TIME SLOT ---
@Composable
fun SelectTimeSlotScreen(vm: MainViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val days = listOf("26", "27", "28", "29", "30")
    val slots = listOf("Morning (07:00 AM - 09:00 AM)", "Late Morning (09:00 AM - 12:00 PM)", "Afternoon (12:00 PM - 04:00 PM)", "Evening (04:00 PM - 07:00 PM)", "Night (07:00 PM - 09:00 PM)")

    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Select Time Slot", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { d ->
                val isSel = d == "27"
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(if (isSel) GoldPrimary else LightCard).border(1.dp, if (isSel) GoldPrimary else LightBorder, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("May", fontSize = 8.sp, color = if (isSel) DarkCanvas else TextMuted)
                        Text(d, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSel) DarkCanvas else TextDark)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        slots.forEach { s ->
            val isSel = s.startsWith("Evening")
            Card(colors = CardDefaults.cardColors(containerColor = if (isSel) GoldLight.copy(alpha = 0.3f) else LightCard), border = BorderStroke(1.dp, if (isSel) GoldPrimary else LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(s, fontSize = 10.5.sp, color = TextDark, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    RadioButton(selected = isSel, onClick = { vm.setSlot("27 May, Tue", s) }, colors = RadioButtonDefaults.colors(selectedColor = GoldDark))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Continue (Trial Fee ₹99)", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 13. TRIAL CART ---
@Composable
fun TrialCartScreen(vm: MainViewModel, onBack: () -> Unit, onProceedToAddress: () -> Unit, onAddMore: () -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Trial Cart (${state.trialKit.size}/4)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        Text("You can try up to 4 items", fontSize = 10.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))
        state.trialKit.forEach { p ->
            Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(p.emoji, fontSize = 22.sp); Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                        Text("₹${p.price.toInt()}", fontSize = 10.sp, color = TextMuted)
                    }
                    IconButton(onClick = { vm.removeProductFromTrialKit(p.id) }) { Icon(Icons.Default.Close, contentDescription = "X", tint = RubyRed, modifier = Modifier.size(16.dp)) }
                }
            }
        }
        TextButton(onClick = onAddMore) { Text("+ Add More", color = GoldDark, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Trial Fee (Adjustable)", fontSize = 11.sp, color = TextMuted); Text("₹99", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onProceedToAddress, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Proceed to Address", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 15. TRIAL ADDRESS ---
@Composable
fun TrialAddressScreen(vm: MainViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Select Delivery Address", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        state.addresses.forEach { addr ->
            val isSel = state.selectedAddress.id == addr.id
            Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, if (isSel) GoldPrimary else LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { vm.selectAddress(addr) }) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isSel, onClick = { vm.selectAddress(addr) }, colors = RadioButtonDefaults.colors(selectedColor = GoldDark))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(addr.label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                        Text(addr.addressLine, fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("Delivering within 15 km · Trial available in your area", fontSize = 10.sp, color = EmeraldSuccess, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Continue", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 16. TRIAL CONFIRMED ---
@Composable
fun TrialConfirmedScreen(vm: MainViewModel, onViewBooking: () -> Unit, onContinueShopping: () -> Unit) {
    val state by vm.state.collectAsState()
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            RoldyGoldyLogo(size = 26, isDarkBg = true)
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(GoldPrimary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = DarkCanvas, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Trial Booked!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LightCanvas)
            Text("We will deliver on ${state.selectedDate}\n04:00 PM - 07:00 PM", fontSize = 11.sp, color = GoldLight, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
            Surface(color = DarkCard, shape = RoundedCornerShape(6.dp)) {
                Text("Booking ID: RGTR123456", fontSize = 9.5.sp, color = GoldLight, modifier = Modifier.padding(8.dp, 3.dp))
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Button(onClick = onViewBooking, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Text("View Booking", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(onClick = onContinueShopping, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GoldPrimary), modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Text("Continue Shopping", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// --- 17. EXCHANGE HOME ---
@Composable
fun ExchangeHomeScreen(vm: MainViewModel, onGetQuote: () -> Unit) {
    var selectedMat by remember { mutableStateOf("Brass Jewellery") }
    val materials = listOf("Brass Jewellery", "Copper Jewellery", "Mixed Alloy", "Glass Kundan", "Faded Polish Jewellery", "Broken / Scrap Lot")

    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Exchange & Save", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        Text("Turn your old jewellery into instant savings", fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(10.dp))
        materials.forEach { m ->
            val isSel = selectedMat == m
            Card(colors = CardDefaults.cardColors(containerColor = if (isSel) GoldLight.copy(alpha = 0.3f) else LightCard), border = BorderStroke(1.dp, if (isSel) GoldPrimary else LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { selectedMat = m }) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 14.sp); Spacer(modifier = Modifier.width(8.dp))
                    Text(m, fontSize = 11.sp, color = TextDark, modifier = Modifier.weight(1f))
                    RadioButton(selected = isSel, onClick = { selectedMat = m }, colors = RadioButtonDefaults.colors(selectedColor = GoldDark))
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(onClick = { vm.applyExchange(selectedMat, "Old Scrap", 100.0); onGetQuote() }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Upload & Get Quote", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 19. EXCHANGE QUOTE ---
@Composable
fun ExchangeQuoteScreen(vm: MainViewModel, onBack: () -> Unit, onProceed: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Your Scrap Quote", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Estimated Value", fontSize = 11.sp, color = TextMuted)
                Text("₹ 280", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("(After quality check)", fontSize = 10.sp, color = TextMuted)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onProceed, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Proceed to Exchange", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 20. EXCHANGE CONFIRMED ---
@Composable
fun ExchangeConfirmedScreen(onTrackExchange: () -> Unit, onContinueShopping: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(GoldPrimary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = DarkCanvas, modifier = Modifier.size(34.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Exchange Confirmed!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightCanvas)
            Text("Pickup on 27 May, Tue\nExchange ID: ROEX123456", fontSize = 10.sp, color = GoldLight, textAlign = TextAlign.Center)
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Button(onClick = onTrackExchange, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Text("Track Exchange", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(onClick = onContinueShopping, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GoldPrimary), modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Text("Continue Shopping", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// --- 22. CART ---
@Composable
fun CartScreen(vm: MainViewModel, onBack: () -> Unit, onProceedCheckout: () -> Unit, onContinueShopping: () -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("My Cart (${state.cart.size} items)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        state.cart.forEach { item ->
            Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.product.emoji, fontSize = 24.sp); Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                        Text("₹${item.product.price.toInt()}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Scrap Cashback Applied", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                Text("- ₹280", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("₹4,518", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onProceedCheckout, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Proceed to Checkout", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 23. CHECKOUT ---
@Composable
fun CheckoutScreen(vm: MainViewModel, onBack: () -> Unit, onPlaceOrder: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Address: 21-1-564, Lakdi Ka Pul, Hyderabad...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Payment: UPI - Google Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onPlaceOrder, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Place Order", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 24. ORDER CONFIRMED ---
@Composable
fun OrderConfirmedScreen(onViewOrder: () -> Unit, onContinueShopping: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(GoldPrimary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = DarkCanvas, modifier = Modifier.size(34.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Order Confirmed!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightCanvas)
            Text("Order ID: RGORD123456", fontSize = 10.sp, color = GoldLight)
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Button(onClick = onViewOrder, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Text("View Order", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(onClick = onContinueShopping, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GoldPrimary), modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Text("Continue Shopping", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// --- 25. ORDER TRACKING ---
@Composable
fun OrderTrackingScreen(vm: MainViewModel, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Track Order (RGORD123456)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(14.dp))
        TrackStep("Order Confirmed", "28 May", true)
        TrackStep("Packed", "27 May", true)
        TrackStep("Shipped", "28 May", true)
        TrackStep("Out for Delivery", "29 May", true)
        TrackStep("Delivered", "29 May", false)
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Call Rider", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun TrackStep(title: String, date: String, isDone: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isDone) EmeraldSuccess else TextMuted.copy(0.3f)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 11.sp, color = TextDark, modifier = Modifier.weight(1f))
        Text(date, fontSize = 9.sp, color = TextMuted)
    }
}

// --- 26. TRIAL LIVE ---
@Composable
fun TrialExperienceLiveScreen(onBuyNow: () -> Unit, onReturnAll: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkObsidianGrad).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your Trial is Live", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightCanvas)
            Spacer(modifier = Modifier.height(20.dp))
            Text("25:48", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = GoldLight)
            Text("Time Remaining", fontSize = 10.sp, color = TextMuted)
        }
        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBuyNow, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(44.dp)) {
                Text("Buy Now", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Button(onClick = onReturnAll, colors = ButtonDefaults.buttonColors(containerColor = DarkCard), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GoldPrimary), modifier = Modifier.weight(1f).height(44.dp)) {
                Text("Return All", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

// --- 27. RETURNS AFTER TRIAL ---
@Composable
fun ReturnsAfterTrialScreen(vm: MainViewModel, onBack: () -> Unit, onRequestPickup: () -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Return Items", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        state.trialKit.take(2).forEach { p ->
            Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(p.emoji, fontSize = 20.sp); Spacer(modifier = Modifier.width(8.dp))
                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark, modifier = Modifier.weight(1f))
                    Checkbox(checked = true, onCheckedChange = {})
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onRequestPickup, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Text("Request Pickup", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- 28. PROFILE ---
@Composable
fun ProfileScreen(vm: MainViewModel, onOpenOrders: () -> Unit, onOpenTrials: () -> Unit, onOpenExchanges: () -> Unit, onOpenWishlist: () -> Unit, onOpenSupport: () -> Unit, onOpenAddressBook: () -> Unit, onLogout: () -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                Text("MS", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkCanvas)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(state.userFullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                Text(state.userPhone, fontSize = 10.sp, color = TextMuted)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ProfileMenuRow("My Orders", onOpenOrders)
        ProfileMenuRow("My Trials", onOpenTrials)
        ProfileMenuRow("My Exchanges", onOpenExchanges)
        ProfileMenuRow("Addresses", onOpenAddressBook)
        ProfileMenuRow("Wishlist", onOpenWishlist)
        ProfileMenuRow("Help & Support", onOpenSupport)
        ProfileMenuRow("Logout", { vm.logout(); onLogout() }, isRed = true)
    }
}

@Composable
fun ProfileMenuRow(title: String, onClick: () -> Unit, isRed: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = if (isRed) RubyRed else TextDark)
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
    }
}

// --- 29. WISHLIST ---
@Composable
fun WishlistScreen(vm: MainViewModel, onBack: () -> Unit, onMoveToCart: (Product) -> Unit) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("My Wishlist (${state.wishlist.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
        }
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.wishlist) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(p.emoji, fontSize = 34.sp)
                        Text(p.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("₹${p.price.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { onMoveToCart(p) }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), contentPadding = PaddingValues(0.dp), modifier = Modifier.fillMaxWidth().height(26.dp)) {
                            Text("Move to Cart", fontSize = 8.5.sp, color = DarkCanvas)
                        }
                    }
                }
            }
        }
    }
}

// --- 30. HELP & SUPPORT ---
@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Help & Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("Hi, How can we help you?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
        Spacer(modifier = Modifier.height(10.dp))
        SupportCard("Chat with us", "We're online", Icons.Default.Email)
        SupportCard("Call Support", "10 AM - 7 PM", Icons.Default.Phone)
        SupportCard("FAQs", "Find answers", Icons.Default.Info)
        SupportCard("Raise a Ticket", "We'll get back to you", Icons.Default.Share)
    }
}

@Composable
fun SupportCard(title: String, sub: String, icon: ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = LightCard), border = BorderStroke(1.dp, LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                Text(sub, fontSize = 9.5.sp, color = TextMuted)
            }
        }
    }
}

// --- 3D VIRTUAL AR TRY-ON ---
@Composable
fun VirtualTryOnScreen(vm: MainViewModel, onBack: () -> Unit, onTryAtHome: () -> Unit, onBuyNow: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val state by vm.state.collectAsState()
    var hasCamera by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }

    LaunchedEffect(Unit) { if (!hasCamera) launcher.launch(Manifest.permission.CAMERA) }
    var jewelryOffset by remember { mutableStateOf(Offset(0f, 50f)) }
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
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp).align(Alignment.TopStart), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White.copy(0.8f))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
        }
        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> jewelryScale = (jewelryScale * zoom).coerceIn(0.6f, 2.2f); jewelryOffset = Offset(jewelryOffset.x + pan.x, jewelryOffset.y + pan.y) } }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(x = jewelryOffset.x.dp, y = jewelryOffset.y.dp)) {
                Text(state.activeVtoProduct.emoji, fontSize = (80 * jewelryScale).sp)
            }
        }
        Surface(color = LightCard.copy(0.95f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onTryAtHome, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GoldDark)) {
                        Text("Try @Home", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkCanvas)
                    }
                    Button(onClick = { vm.addToCart(state.activeVtoProduct); onBuyNow() }, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                        Text("Buy (₹${state.activeVtoProduct.price.toInt()})", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- SELLER BARGAIN CHAT ---
@Composable
fun SellerChatScreen(product: Product, vm: MainViewModel, onBack: () -> Unit, onCheckout: () -> Unit) {
    val state by vm.state.collectAsState()
    var msgText by remember { mutableStateOf("") }
    var offerInput by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jaipur Jewels (${product.name.take(10)}…)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightCanvas)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightCanvas)) {
            LazyColumn(modifier = Modifier.weight(1f).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.chatMessages) { msg ->
                    val isBuyer = msg.senderId == "buyer"
                    Column(horizontalAlignment = if (isBuyer) Alignment.End else Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(10.dp), color = if (isBuyer) GoldLight else LightCard, border = if (!isBuyer) BorderStroke(1.dp, LightBorder) else null, modifier = Modifier.widthIn(max = 250.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(msg.text, color = TextDark, fontSize = 11.sp)
                                if (msg.counterAmount != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(onClick = { vm.acceptCounterOffer(product.id, msg.counterAmount); vm.addToCart(product, msg.counterAmount); onCheckout() }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess), shape = RoundedCornerShape(4.dp)) {
                                        Text("Accept ₹${msg.counterAmount.toInt()}", color = LightCard, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(color = LightCard, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(12.dp)) {
                        Text("Bargain", color = DarkCanvas, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(value = msgText, onValueChange = { msgText = it }, placeholder = { Text("Message…", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = { if (msgText.isNotBlank()) { vm.sendTextMessage(msgText); msgText = "" } }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = GoldDark)
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

// --- 11b. ADDRESS BOOK ---
@Composable
fun AddressBookScreen(vm: MainViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(LightCanvas).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Saved Addresses", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { vm.autoDetectGps() }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Text("📍 Auto-Detect Live GPS Location", color = DarkCanvas, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        state.addresses.forEach { addr ->
            val isSel = state.selectedAddress.id == addr.id
            Card(colors = CardDefaults.cardColors(containerColor = if (isSel) GoldLight.copy(alpha = 0.25f) else LightCard), border = BorderStroke(1.dp, if (isSel) GoldPrimary else LightBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { vm.selectAddress(addr) }) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${addr.label} · ${addr.recipientName}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                        if (isSel) Surface(color = GoldPrimary, shape = RoundedCornerShape(4.dp)) { Text("Active", color = DarkCanvas, fontSize = 8.sp, modifier = Modifier.padding(4.dp, 1.dp)) }
                    }
                    Text(addr.addressLine, fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}
