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

// --- PALETTE ---
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
val EmeraldLight = Color(0xFFE2F4EB)
val RubyRed = Color(0xFFA62435)

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

// --- DATA MODELS ---
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

val sampleProducts = listOf(
    Product("1", "Kundan Choker Necklace", "Bridal", 3499.0, 4899.0, "👑", true, "HOT", "1-Gram Matte Gold Plated", "48.5 g", "36.2 g", "Brass & Copper Alloy", "Kundan & Emeralds", "12–18 in", "Golden Zari Dori", "RG 1-Gram Certified", "Royal bridal choker set handcrafted with precision Kundan foil work."),
    Product("2", "Korean Minimal Hoops", "Daily Wear", 349.0, 549.0, "💫", false, "NEW", "18K PVD Anti-Tarnish", "6.8 g", "6.8 g", "316L Surgical Titanium", "AAA+ Cubic Zirconia", "18mm", "Click Latch", "18K PVD", "Waterproof and hypoallergenic daily-wear hoops."),
    Product("3", "Temple Deity Jhumka Set", "Temple", 1299.0, 1899.0, "🪔", true, "BESTSELLER", "Antique Micro-Gold", "38.2 g", "30.5 g", "Bronze Alloy", "Kemp & Pearls", "14–16 in", "Lobster Lock", "Guaranteed Finish", "South Indian temple motif with ruby-red kemp stones."),
    Product("4", "Polki Bridal Maangtikka", "Bridal", 2899.0, 3599.0, "💍", true, "HOT", "Uncut Polki Foil", "22.4 g", "18.1 g", "Copper & Silver", "Polki & Onyx", "5.5 in", "Anchor Hook", "Artisan Certified", "Floral bridal maangtikka with silver foil polki setting.")
)

val sampleAddresses = listOf(
    Address("a1", "Home", "Meera Sharma", "21-1-564, Lakdi Ka Pul, Hyderabad", "500001", 2.4, isDefault = true),
    Address("a2", "Office", "Meera Sharma", "Mindspace, Hitech City, Hyderabad", "500081", 4.1, isDefault = false)
)

// --- VIEWMODEL ---
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

// --- ROUTER ---
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
