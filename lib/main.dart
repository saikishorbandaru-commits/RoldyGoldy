import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';

// ==========================================
// 🎨 PALETTE & STYLING
// ==========================================
class AppColors {
  static const Color wineDark = Color(0xFF140C07);
  static const Color wineCard = Color(0xFF1C130D);
  static const Color wineSurface = Color(0xFF281C13);
  static const Color winePrimary = Color(0xFF55102F);
  static const Color gold = Color(0xFFE5A93C);
  static const Color goldLight = Color(0xFFF7D992);
  static const Color goldDark = Color(0xFF9E6B18);
  static const Color ivory = Color(0xFFFDFBF7);
  static const Color cardBg = Color(0xFFFFFFFF);
  static const Color border = Color(0xFFEFE8DC);
  static const Color textDark = Color(0xFF1F1610);
  static const Color textMuted = Color(0xFF8C7E72);
  static const Color emerald = Color(0xFF1B6B46);
  static const Color emeraldLight = Color(0xFFE2F4EB);
  static const Color ruby = Color(0xFFA62435);
}

// ==========================================
// 📦 DATA MODELS
// ==========================================
class Product {
  final String id;
  final String name;
  final String category;
  final double price;
  final double originalPrice;
  final String emoji;
  final bool isTrialEligible;
  final String tag;
  final String karatInfo;
  final String grossWeight;
  final String netWeight;
  final String metalType;
  final String stoneType;
  final String size;
  final String closure;
  final String hallmark;
  final String description;

  Product({
    required this.id,
    required this.name,
    required this.category,
    required this.price,
    required this.originalPrice,
    required this.emoji,
    this.isTrialEligible = true,
    required this.tag,
    required this.karatInfo,
    required this.grossWeight,
    required this.netWeight,
    required this.metalType,
    required this.stoneType,
    required this.size,
    required this.closure,
    required this.hallmark,
    required this.description,
  });
}

class CartItem {
  final Product product;
  int quantity;
  double? customPrice;

  CartItem({required this.product, this.quantity = 1, this.customPrice});

  double get unitPrice => customPrice ?? product.price;
  double get total => unitPrice * quantity;
}

class OrderItem {
  final String orderId;
  final String productName;
  final String emoji;
  final double amount;
  final String status;
  final String date;
  final bool isTrial;

  OrderItem({
    required this.orderId,
    required this.productName,
    required this.emoji,
    required this.amount,
    required this.status,
    required this.date,
    this.isTrial = false,
  });
}

// ==========================================
// 🧠 GLOBAL APP STATE
// ==========================================
class AppState extends ChangeNotifier {
  bool isFirstTimeUser = true;
  bool isLoggedIn = false;
  String userName = "Meera Sharma";
  String userPhone = "+91 98765 43210";
  String selectedPincode = "500101";

  final List<Product> products = [
    Product(
      id: '1',
      name: 'Kundan Choker Necklace',
      category: 'Bridal',
      price: 3499,
      originalPrice: 4899,
      emoji: '👑',
      tag: 'HOT',
      karatInfo: '1-Gram Matte Gold Plated',
      grossWeight: '48.5 g',
      netWeight: '36.2 g',
      metalType: 'Brass & Copper Alloy',
      stoneType: 'Hand-cut Kundan & Emeralds',
      size: 'Adjustable 12–18 in',
      closure: 'Golden Zari Dori',
      hallmark: 'RG 1-Gram Certified',
      description: 'Royal bridal choker set with Kundan foil work and velvet trousseau box packaging.',
    ),
    Product(
      id: '2',
      name: 'Korean Minimal Hoops',
      category: 'Daily Wear',
      price: 349,
      originalPrice: 549,
      emoji: '💫',
      tag: 'NEW',
      karatInfo: '18K PVD Anti-Tarnish',
      isTrialEligible: false,
      grossWeight: '6.8 g',
      netWeight: '6.8 g',
      metalType: '316L Surgical Titanium',
      stoneType: 'AAA+ Cubic Zirconia',
      size: '18mm Diameter',
      closure: 'Click-top Latch',
      hallmark: '18K PVD Certified',
      description: 'Waterproof daily minimal hoops with hypoallergenic titanium core.',
    ),
    Product(
      id: '3',
      name: 'Temple Deity Jhumka Set',
      category: 'Temple',
      price: 1299,
      originalPrice: 1899,
      emoji: '🪔',
      tag: 'BESTSELLER',
      karatInfo: 'Antique Micro-Gold',
      grossWeight: '38.2 g',
      netWeight: '30.5 g',
      metalType: "Jeweller's Bronze Alloy",
      stoneType: 'Kemp Stones & Pearls',
      size: 'Adjustable 14–16 in',
      closure: 'Metallic Lobster Lock',
      hallmark: 'Micro-Gold Guaranteed',
      description: 'South Indian temple deity motif sculpted with ruby kemp stones.',
    ),
    Product(
      id: '4',
      name: 'Polki Bridal Maangtikka',
      category: 'Bridal',
      price: 2899,
      originalPrice: 3599,
      emoji: '💍',
      tag: 'HOT',
      karatInfo: 'Uncut Polki Foil',
      grossWeight: '22.4 g',
      netWeight: '18.1 g',
      metalType: 'Copper & Silver Alloy',
      stoneType: 'Uncut Polki & Onyx',
      size: '5.5 in Length',
      closure: 'Anchor Hook',
      hallmark: 'Artisan Certified',
      description: 'Floral bridal maangtikka with hanging dark green onyx bead drops.',
    ),
  ];

  final List<CartItem> cart = [];
  final List<Product> trialKit = [];
  final List<Product> wishlist = [];
  final List<OrderItem> orders = [
    OrderItem(orderId: 'RGORD123456', productName: 'Kundan Choker Necklace', emoji: '👑', amount: 3499, status: 'Order Confirmed', date: '28 May'),
    OrderItem(orderId: 'RGTR123456', productName: 'Trial Box (3 Pieces)', emoji: '👑', amount: 99, status: 'Trial Scheduled (27 May)', date: '27 May', isTrial: true),
  ];

  double scrapCashback = 0.0;
  String selectedDate = '27 May, Tue';
  String selectedSlot = 'Evening (04:00 PM - 07:00 PM)';
  final int trialFee = 99;

  void completeOnboarding() {
    isFirstTimeUser = false;
    notifyListeners();
  }

  void login(String phone, String name) {
    userPhone = phone;
    userName = name;
    isLoggedIn = true;
    notifyListeners();
  }

  void logout() {
    isLoggedIn = false;
    notifyListeners();
  }

  void addToCart(Product p, {double? customPrice}) {
    final idx = cart.indexWhere((i) => i.product.id == p.id);
    if (idx >= 0) {
      cart[idx].quantity++;
    } else {
      cart.add(CartItem(product: p, customPrice: customPrice));
    }
    notifyListeners();
  }

  void updateCartQty(String id, int delta) {
    final idx = cart.indexWhere((i) => i.product.id == id);
    if (idx >= 0) {
      cart[idx].quantity += delta;
      if (cart[idx].quantity <= 0) cart.removeAt(idx);
      notifyListeners();
    }
  }

  double get cartSubtotal => cart.fold(0, (sum, i) => sum + i.total);
  double get finalPayable => (cartSubtotal - scrapCashback).clamp(0, double.infinity);

  void toggleWishlist(Product p) {
    if (wishlist.any((i) => i.id == p.id)) {
      wishlist.removeWhere((i) => i.id == p.id);
    } else {
      wishlist.add(p);
    }
    notifyListeners();
  }

  bool isWishlisted(String id) => wishlist.any((i) => i.id == id);

  void addToTrialKit(Product p) {
    if (trialKit.length < 4 && !trialKit.any((i) => i.id == p.id)) {
      trialKit.add(p);
      notifyListeners();
    }
  }

  void removeFromTrialKit(String id) {
    trialKit.removeWhere((i) => i.id == id);
    notifyListeners();
  }

  void setSlot(String date, String slot) {
    selectedDate = date;
    selectedSlot = slot;
    notifyListeners();
  }

  void applyScrapCashback(double amt) {
    scrapCashback = amt;
    notifyListeners();
  }

  void removeScrapCashback() {
    scrapCashback = 0.0;
    notifyListeners();
  }

  void placeOrder({required bool isTrial}) {
    final id = isTrial ? 'RGTR123456' : 'RGORD123456';
    final name = isTrial ? 'Trial Box (${trialKit.length} pieces)' : (cart.isNotEmpty ? cart.first.product.name : 'Jewellery Order');
    final emoji = isTrial ? '👑' : (cart.isNotEmpty ? cart.first.product.emoji : '💎');
    final amount = isTrial ? trialFee.toDouble() : finalPayable;

    orders.insert(
      0,
      OrderItem(orderId: id, productName: name, emoji: emoji, amount: amount, status: 'Order Confirmed', date: 'Today', isTrial: isTrial),
    );
    if (!isTrial) {
      cart.clear();
      scrapCashback = 0.0;
    }
    notifyListeners();
  }
}

// ==========================================
// 🚀 APP ENTRY
// ==========================================
void main() {
  runApp(
    MultiProvider(
      providers: [ChangeNotifierProvider(create: (_) => AppState())],
      child: const RoldyGoldyApp(),
    ),
  );
}

class RoldyGoldyApp extends StatelessWidget {
  const RoldyGoldyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'RoldyGoldy',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: AppColors.ivory,
        textTheme: GoogleFonts.manropeTextTheme(),
        appBarTheme: AppBarTheme(
          backgroundColor: AppColors.ivory,
          elevation: 0,
          titleTextStyle: GoogleFonts.cormorantGaramond(fontSize: 20, fontWeight: FontWeight.bold, color: AppColors.textDark),
          iconTheme: const IconThemeData(color: AppColors.textDark),
        ),
      ),
      home: const RootDecider(),
    );
  }
}

class RootDecider extends StatelessWidget {
  const RootDecider({super.key});

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();
    if (state.isFirstTimeUser) return const SplashScreen();
    if (!state.isLoggedIn) return const AuthPhoneScreen();
    return const MainNavShell();
  }
}

// ==========================================
// 🧭 MAIN NAVIGATION SHELL (5 TABS)
// ==========================================
class MainNavShell extends StatefulWidget {
  final int initialIndex;
  const MainNavShell({super.key, this.initialIndex = 0});

  @override
  State<MainNavShell> createState() => _MainNavShellState();
}

class _MainNavShellState extends State<MainNavShell> {
  late int _currentIndex;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    final screens = [
      const HomeScreen(),
      const CategoriesScreen(),
      const TryAtHomeInfoScreen(),
      const ExchangeHomeScreen(),
      const ProfileScreen(),
    ];

    return Scaffold(
      body: IndexedStack(index: _currentIndex, children: screens),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (i) => setState(() => _currentIndex = i),
        backgroundColor: AppColors.cardBg,
        indicatorColor: AppColors.goldLight.withOpacity(0.5),
        destinations: [
          const NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home, color: AppColors.goldDark), label: 'Home'),
          const NavigationDestination(icon: Icon(Icons.grid_view_outlined), selectedIcon: Icon(Icons.grid_view, color: AppColors.goldDark), label: 'Categories'),
          NavigationDestination(
            icon: Badge(
              label: Text('${state.trialKit.length}'),
              isLabelVisible: state.trialKit.isNotEmpty,
              child: const Icon(Icons.schedule_outlined),
            ),
            selectedIcon: const Icon(Icons.schedule, color: AppColors.goldDark),
            label: 'Trial',
          ),
          const NavigationDestination(icon: Icon(Icons.cached_outlined), selectedIcon: Icon(Icons.cached, color: AppColors.goldDark), label: 'Exchange'),
          const NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person, color: AppColors.goldDark), label: 'Account'),
        ],
      ),
    );
  }
}

// ==========================================
// 📱 01–06: ONBOARDING & AUTH
// ==========================================
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});
  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const OnboardingScreen()));
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.wineDark,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('RoldyGoldy', style: GoogleFonts.cormorantGaramond(fontSize: 38, fontWeight: FontWeight.bold, color: AppColors.goldLight, letterSpacing: 1.5)),
            const SizedBox(height: 6),
            const Text('HER PRIDE • HER CHOICE • HER TRUST', style: TextStyle(fontSize: 9, fontWeight: FontWeight.bold, color: AppColors.gold, letterSpacing: 2)),
            const SizedBox(height: 48),
            const Text('👑', style: TextStyle(fontSize: 80)),
          ],
        ),
      ),
    );
  }
}

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});
  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final PageController _ctrl = PageController();
  int _page = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.wineDark,
      body: SafeArea(
        child: Column(
          children: [
            Align(
              alignment: Alignment.topRight,
              child: TextButton(
                onPressed: () {
                  context.read<AppState>().completeOnboarding();
                  Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const AuthPhoneScreen()));
                },
                child: const Text('Skip', style: TextStyle(color: AppColors.goldLight)),
              ),
            ),
            Expanded(
              child: PageView(
                controller: _ctrl,
                onPageChanged: (i) => setState(() => _page = i),
                children: [
                  _slide1(),
                  _slide2(),
                  _slide3(),
                ],
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(
                3,
                (i) => Container(
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  width: _page == i ? 22 : 6,
                  height: 6,
                  decoration: BoxDecoration(color: _page == i ? AppColors.gold : Colors.white24, borderRadius: BorderRadius.circular(3)),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(24),
              child: SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                  onPressed: () {
                    if (_page < 2) {
                      _ctrl.nextPage(duration: const Duration(milliseconds: 300), curve: Curves.easeInOut);
                    } else {
                      context.read<AppState>().completeOnboarding();
                      Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const AuthPhoneScreen()));
                    }
                  },
                  child: Text(_page == 2 ? 'Get Started' : 'Next'),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _slide1() => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('✨', style: TextStyle(fontSize: 72)),
            const SizedBox(height: 24),
            Text('For Every You,\nFor Every Moment.', textAlign: TextAlign.center, style: GoogleFonts.cormorantGaramond(fontSize: 26, fontWeight: FontWeight.bold, color: AppColors.ivory)),
            const SizedBox(height: 10),
            const Text('From everyday elegance to dreamy bridal looks.', textAlign: TextAlign.center, style: TextStyle(fontSize: 12, color: AppColors.goldLight)),
          ],
        ),
      );

  Widget _slide2() => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            _featCard('👑', 'Try at Home', 'Book 3-4 pieces and try at home for 20-30 mins.'),
            _featCard('♻️', 'Exchange & Save', 'Exchange old jewellery for instant cashback.'),
            _featCard('🛡️', 'Trusted Quality', 'Premium imitation finish with 6 months warranty.'),
          ],
        ),
      );

  Widget _featCard(String e, String t, String d) => Card(
        color: AppColors.wineCard,
        margin: const EdgeInsets.only(bottom: 10),
        child: ListTile(
          leading: Text(e, style: const TextStyle(fontSize: 24)),
          title: Text(t, style: const TextStyle(color: AppColors.goldLight, fontWeight: FontWeight.bold, fontSize: 13)),
          subtitle: Text(d, style: const TextStyle(color: AppColors.ivory, fontSize: 11)),
        ),
      );

  Widget _slide3() => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Her Choice.\nOur Promise.', textAlign: TextAlign.center, style: GoogleFonts.cormorantGaramond(fontSize: 26, fontWeight: FontWeight.bold, color: AppColors.ivory)),
            const SizedBox(height: 20),
            ...['1 Lakh+ Happy Customers', '100% Secure Payments', 'Easy Returns', 'Pan India Delivery'].map(
              (t) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  children: [
                    const Icon(Icons.check_circle, color: AppColors.gold, size: 16),
                    const SizedBox(width: 8),
                    Text(t, style: const TextStyle(color: AppColors.ivory, fontSize: 12)),
                  ],
                ),
              ),
            ),
          ],
        ),
      );
}

class AuthPhoneScreen extends StatelessWidget {
  const AuthPhoneScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final phoneCtrl = TextEditingController(text: "98765 43210");

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 20),
              Text('Welcome to\nRoldyGoldy', style: GoogleFonts.cormorantGaramond(fontSize: 28, fontWeight: FontWeight.bold, color: AppColors.textDark)),
              const Text('Login / Sign up', style: TextStyle(fontSize: 12, color: AppColors.textMuted)),
              const SizedBox(height: 30),
              const Text('Enter your mobile number', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
              const SizedBox(height: 6),
              TextField(
                controller: phoneCtrl,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(prefixText: '+91 ', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                  onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => OtpVerifyScreen(phone: phoneCtrl.text))),
                  child: const Text('Continue'),
                ),
              ),
              const SizedBox(height: 20),
              const Center(child: Text('or continue with', style: TextStyle(fontSize: 11, color: AppColors.textMuted))),
              const SizedBox(height: 12),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: ['Google', 'Facebook', 'Apple'].map((s) => Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: OutlinedButton(onPressed: () {}, child: Text(s, style: const TextStyle(fontSize: 11, color: AppColors.textDark))),
                )).toList(),
              ),
              const Spacer(),
              const Center(child: Text('By continuing, you agree to our\nTerms & Conditions & Privacy Policy', textAlign: TextAlign.center, style: TextStyle(fontSize: 10, color: AppColors.textMuted))),
            ],
          ),
        ),
      ),
    );
  }
}

class OtpVerifyScreen extends StatelessWidget {
  final String phone;
  const OtpVerifyScreen({super.key, required this.phone});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Verify OTP')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Enter the 6 digit code sent to\n+91 $phone', style: const TextStyle(fontSize: 12, color: AppColors.textMuted)),
            const SizedBox(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: ['2', '4', '6', '8', '1', '1'].map((d) => Container(
                width: 40,
                height: 44,
                decoration: BoxDecoration(color: AppColors.cardBg, borderRadius: BorderRadius.circular(8), border: Border.all(color: AppColors.gold)),
                alignment: Alignment.Center,
                child: Text(d, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              )).toList(),
            ),
            const SizedBox(height: 16),
            const Center(child: Text('Resend OTP in 00:28', style: TextStyle(fontSize: 11, color: AppColors.textMuted))),
            const Spacer(),
            const Center(child: Text('Auto detecting OTP 246811 ✓', style: TextStyle(color: AppColors.emerald, fontWeight: FontWeight.bold, fontSize: 11))),
            const SizedBox(height: 10),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                onPressed: () {
                  context.read<AppState>().login(phone, 'Meera Sharma');
                  Navigator.pushAndRemoveUntil(context, MaterialPageRoute(builder: (_) => const MainNavShell()), (r) => false);
                },
                child: const Text('Verify & Continue'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ==========================================
// 🏠 07–10: HOME, CATALOG & PDP
// ==========================================
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(
        title: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('RoldyGoldy', style: GoogleFonts.cormorantGaramond(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.goldDark)),
            Row(
              children: [
                IconButton(icon: const Icon(Icons.favorite_border), onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const WishlistScreen()))),
                IconButton(
                  icon: Badge(
                    label: Text('${state.cart.length}'),
                    isLabelVisible: state.cart.isNotEmpty,
                    child: const Icon(Icons.shopping_bag_outlined),
                  ),
                  onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen())),
                ),
              ],
            ),
          ],
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Card(
              color: AppColors.cardBg,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('Trial @Home', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                          const Text('Try up to 4 pieces at your doorstep', style: TextStyle(fontSize: 11, color: AppColors.textMuted)),
                          const SizedBox(height: 8),
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(backgroundColor: AppColors.wineDark, foregroundColor: AppColors.goldLight),
                            onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const TryAtHomeInfoScreen())),
                            child: const Text('BOOK NOW · ₹99', style: TextStyle(fontSize: 10)),
                          ),
                        ],
                      ),
                    ),
                    const Text('👑', style: TextStyle(fontSize: 44)),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _catItem(context, 'Daily Wear', '💫'),
                _catItem(context, 'Korean', '✨'),
                _catItem(context, 'Temple', '🪔'),
                _catItem(context, 'Bridal', '👑'),
                _catItem(context, 'New In', '💍'),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Best Sellers', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.textDark)),
                TextButton(onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CategoriesScreen())), child: const Text('See All', style: TextStyle(color: AppColors.goldDark, fontSize: 12))),
              ],
            ),
            GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: state.products.length,
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 2, childAspectRatio: 0.72, crossAxisSpacing: 10, mainAxisSpacing: 10),
              itemBuilder: (context, i) {
                final p = state.products[i];
                return GestureDetector(
                  onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ProductDetailScreen(product: p))),
                  child: Card(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Container(
                            decoration: BoxDecoration(color: AppColors.goldLight.withOpacity(0.2), borderRadius: const BorderRadius.vertical(top: Radius.circular(12))),
                            alignment: Alignment.Center,
                            child: Text(p.emoji, style: const TextStyle(fontSize: 44)),
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.all(8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(p.name, maxLines: 1, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11)),
                              Text('₹${p.price.toInt()}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: AppColors.winePrimary)),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _catItem(BuildContext context, String title, String emoji) {
    return GestureDetector(
      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => CategoryListingScreen(category: title))),
      child: Column(
        children: [
          CircleAvatar(radius: 24, backgroundColor: AppColors.cardBg, child: Text(emoji, style: const TextStyle(fontSize: 20))),
          const SizedBox(height: 4),
          Text(title, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}

class CategoriesScreen extends StatelessWidget {
  const CategoriesScreen({super.key});

  final List<Map<String, String>> categories = const [
    {'title': 'Daily Wear', 'icon': '💫'},
    {'title': 'Korean', 'icon': '✨'},
    {'title': 'Temple', 'icon': '🪔'},
    {'title': 'Bridal', 'icon': '👑'},
    {'title': 'Necklaces', 'icon': '📿'},
    {'title': 'Earrings', 'icon': '💎'},
    {'title': 'Bangles', 'icon': '⭕'},
    {'title': 'Rings', 'icon': '💍'},
    {'title': 'Mangalsutra', 'icon': '🖤'},
    {'title': 'Sets', 'icon': '🎁'},
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Categories')),
      body: GridView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: categories.length,
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 3, childAspectRatio: 0.9, crossAxisSpacing: 10, mainAxisSpacing: 10),
        itemBuilder: (context, i) {
          final c = categories[i];
          return GestureDetector(
            onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => CategoryListingScreen(category: c['title']!))),
            child: Card(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(c['icon']!, style: const TextStyle(fontSize: 28)),
                  const SizedBox(height: 6),
                  Text(c['title']!, textAlign: TextAlign.center, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class CategoryListingScreen extends StatelessWidget {
  final String category;
  const CategoryListingScreen({super.key, required this.category});

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(title: Text(category)),
      body: GridView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: state.products.length,
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 2, childAspectRatio: 0.72, crossAxisSpacing: 10, mainAxisSpacing: 10),
        itemBuilder: (context, i) {
          final p = state.products[i];
          return GestureDetector(
            onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ProductDetailScreen(product: p))),
            child: Card(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Container(
                      decoration: BoxDecoration(color: AppColors.goldLight.withOpacity(0.2), borderRadius: const BorderRadius.vertical(top: Radius.circular(12))),
                      alignment: Alignment.Center,
                      child: Text(p.emoji, style: const TextStyle(fontSize: 44)),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(p.name, maxLines: 1, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11)),
                        Text('₹${p.price.toInt()}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: AppColors.winePrimary)),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class ProductDetailScreen extends StatefulWidget {
  final Product product;
  const ProductDetailScreen({super.key, required this.product});

  @override
  State<ProductDetailScreen> createState() => _ProductDetailScreenState();
}

class _ProductDetailScreenState extends State<ProductDetailScreen> {
  double? _bargainPrice;

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();
    final p = widget.product;
    final isWish = state.isWishlisted(p.id);

    return Scaffold(
      appBar: AppBar(
        title: Text(p.name),
        actions: [
          IconButton(
            icon: Icon(isWish ? Icons.favorite : Icons.favorite_border, color: isWish ? AppColors.ruby : AppColors.textDark),
            onPressed: () => state.toggleWishlist(p),
          ),
        ],
      ),
      bottomNavigationBar: Container(
        padding: const EdgeInsets.all(12),
        decoration: const BoxDecoration(color: AppColors.cardBg, border: Border(top: BorderSide(color: AppColors.border))),
        child: Row(
          children: [
            Expanded(
              child: OutlinedButton(
                onPressed: () {
                  state.addToTrialKit(p);
                  Navigator.push(context, MaterialPageRoute(builder: (_) => const TrialCartScreen()));
                },
                child: const Text('Try @Home'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.wineDark, foregroundColor: AppColors.goldLight),
                onPressed: () {
                  state.addToCart(p, customPrice: _bargainPrice);
                  Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen()));
                },
                child: const Text('Buy Now'),
              ),
            ),
          ],
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 200,
              decoration: BoxDecoration(color: AppColors.goldLight.withOpacity(0.2), borderRadius: BorderRadius.circular(12)),
              alignment: Alignment.Center,
              child: Text(p.emoji, style: const TextStyle(fontSize: 72)),
            ),
            const SizedBox(height: 14),
            Text(p.name, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.textDark)),
            const SizedBox(height: 4),
            Row(
              children: [
                Text('₹${(_bargainPrice ?? p.price).toInt()}', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: AppColors.winePrimary)),
                const SizedBox(width: 8),
                Text('₹${p.originalPrice.toInt()}', style: const TextStyle(fontSize: 12, decoration: TextDecoration.lineThrough, color: AppColors.textMuted)),
              ],
            ),
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  children: [
                    _spec('Metal Alloy', p.metalType),
                    _spec('Plating / Karat', p.karatInfo),
                    _spec('Gross Weight', p.grossWeight),
                    _spec('Net Weight', p.netWeight),
                    _spec('Stone Type', p.stoneType),
                    _spec('Closure', p.closure),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
            Card(
              child: ListTile(
                leading: const Text('💬', style: TextStyle(fontSize: 20)),
                title: const Text('Chat & Bargain with Jeweller', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                subtitle: const Text('Send custom offer directly to seller', style: TextStyle(fontSize: 10)),
                onTap: () => _bargainDialog(context, p),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _spec(String k, String v) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 3),
        child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          Text(k, style: const TextStyle(fontSize: 11, color: AppColors.textMuted)),
          Text(v, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
        ]),
      );

  void _bargainDialog(BuildContext context, Product p) {
    final ctrl = TextEditingController(text: '${(p.price * 0.9).toInt()}');
    showModalBottomSheet(
      context: context,
      builder: (_) => Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Bargain with Jeweller', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            TextField(controller: ctrl, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Your Offer (₹)', border: OutlineInputBorder())),
            const SizedBox(height: 14),
            ElevatedButton(
              onPressed: () {
                final off = double.tryParse(ctrl.text);
                if (off != null) {
                  setState(() => _bargainPrice = off * 1.05);
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Seller countered with ₹${_bargainPrice!.toInt()}! Price locked.')));
                }
              },
              child: const Text('Send Offer'),
            ),
          ],
        ),
      ),
    );
  }
}

// ==========================================
// 👑 11–16: TRIAL @HOME FLOW
// ==========================================
class TryAtHomeInfoScreen extends StatelessWidget {
  const TryAtHomeInfoScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Trial @Home')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Card(
              child: Padding(
                padding: EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('How it works?', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                    SizedBox(height: 8),
                    Text('1. Book up to 4 pieces.\n2. We deliver to your doorstep.\n3. Try for 20-30 mins.\n4. Buy what you love, return the rest.', style: TextStyle(fontSize: 11, height: 1.5)),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 10),
            const Card(
              child: ListTile(
                title: Text('Trial Fee (Nominal)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                subtitle: Text('₹99 per slot (Adjustable on purchase)', style: TextStyle(fontSize: 10)),
              ),
            ),
            const Spacer(),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const SelectSlotScreen())),
                child: const Text('Book Trial'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class SelectSlotScreen extends StatefulWidget {
  const SelectSlotScreen({super.key});

  @override
  State<SelectSlotScreen> createState() => _SelectSlotScreenState();
}

class _SelectSlotScreenState extends State<SelectSlotScreen> {
  String _slot = 'Evening (04:00 PM - 07:00 PM)';

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(title: const Text('Select Time Slot')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            ...['Morning (07:00 AM - 09:00 AM)', 'Afternoon (12:00 PM - 04:00 PM)', 'Evening (04:00 PM - 07:00 PM)'].map(
              (s) => Card(
                child: RadioListTile<String>(
                  value: s,
                  groupValue: _slot,
                  onChanged: (v) => setState(() => _slot = v!),
                  title: Text(s, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
                ),
              ),
            ),
            const Spacer(),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                onPressed: () {
                  state.setSlot('27 May, Tue', _slot);
                  Navigator.push(context, MaterialPageRoute(builder: (_) => const TrialCartScreen()));
                },
                child: const Text('Continue · Trial Fee ₹99'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class TrialCartScreen extends StatelessWidget {
  const TrialCartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(title: Text('Trial Cart (${state.trialKit.length}/4)')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            if (state.trialKit.isEmpty)
              const Center(child: Text('No items in trial kit'))
            else
              ...state.trialKit.map((p) => Card(
                    child: ListTile(
                      leading: Text(p.emoji, style: const TextStyle(fontSize: 22)),
                      title: Text(p.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11)),
                      trailing: IconButton(icon: const Icon(Icons.close, color: AppColors.ruby, size: 18), onPressed: () => state.removeFromTrialKit(p.id)),
                    ),
                  )),
            const Spacer(),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                onPressed: state.trialKit.isEmpty
                    ? null
                    : () {
                        state.placeOrder(isTrial: true);
                        Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const TrialConfirmedScreen()));
                      },
                child: const Text('Confirm Trial Booking · Pay ₹99'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class TrialConfirmedScreen extends StatelessWidget {
  const TrialConfirmedScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.wineDark,
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.check_circle, color: AppColors.gold, size: 60),
              const SizedBox(height: 16),
              const Text('Trial Booked!', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.ivory)),
              const SizedBox(height: 6),
              const Text('Booking ID: RGTR123456\nRider will arrive with sealed kit.', textAlign: TextAlign.center, style: TextStyle(color: AppColors.goldLight, fontSize: 12)),
              const SizedBox(height: 24),
              ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                onPressed: () => Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const TrialLiveTimerScreen())),
                child: const Text('Open Doorstep Tryout Timer'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class TrialLiveTimerScreen extends StatelessWidget {
  const TrialLiveTimerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.wineDark,
      appBar: AppBar(backgroundColor: AppColors.wineDark, title: const Text('Trial Live', style: TextStyle(color: AppColors.ivory))),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('⏱️ 25:48', style: TextStyle(fontSize: 44, fontWeight: FontWeight.bold, color: AppColors.goldLight)),
            const Text('Decide within 30 minutes', style: TextStyle(color: AppColors.ivory, fontSize: 12)),
            const SizedBox(height: 24),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
              onPressed: () => Navigator.pop(context),
              child: const Text('Buy Now / Return Items'),
            ),
          ],
        ),
      ),
    );
  }
}

// ==========================================
// ♻️ 17–20: EXCHANGE & SAVE
// ==========================================
class ExchangeHomeScreen extends StatefulWidget {
  const ExchangeHomeScreen({super.key});

  @override
  State<ExchangeHomeScreen> createState() => _ExchangeHomeScreenState();
}

class _ExchangeHomeScreenState extends State<ExchangeHomeScreen> {
  String _mat = 'Brass Jewellery';
  final TextEditingController _ctrl = TextEditingController(text: '100');

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();
    final g = double.tryParse(_ctrl.text) ?? 100;
    final net = (g * 3.1 * 0.90).roundToDouble();

    return Scaffold(
      appBar: AppBar(title: const Text('Exchange & Save')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Card(
              color: AppColors.emeraldLight,
              child: Padding(
                padding: EdgeInsets.all(12),
                child: Text('♻️ Trade in old jewellery for instant cashback applied directly to your cart bill.', style: TextStyle(fontSize: 11, color: AppColors.emerald, height: 1.4)),
              ),
            ),
            const SizedBox(height: 14),
            const Text('Select Material', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
            const SizedBox(height: 6),
            ...['Brass Jewellery', 'Copper Jewellery', 'Mixed Alloy', 'Broken Scrap'].map(
              (m) => Card(
                child: RadioListTile<String>(
                  value: m,
                  groupValue: _mat,
                  onChanged: (v) => setState(() => _mat = v!),
                  title: Text(m, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
                ),
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _ctrl,
              keyboardType: TextInputType.number,
              onChanged: (_) => setState(() {}),
              decoration: const InputDecoration(labelText: 'Approx Weight (Grams)', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 14),
            Card(
              color: AppColors.wineCard,
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  children: [
                    const Text('ESTIMATED CASHBACK CREDIT', style: TextStyle(color: AppColors.goldLight, fontSize: 9, fontWeight: FontWeight.bold)),
                    Text('₹${net.toInt()}', style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: AppColors.goldLight)),
                    const Text('Net of 10% purity check deduction', style: TextStyle(color: AppColors.ivory, fontSize: 8)),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.gold, foregroundColor: AppColors.wineDark),
                onPressed: () {
                  state.applyScrapCashback(net);
                  Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen()));
                },
                child: const Text('Apply Cashback to Cart'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ==========================================
// 🛒 22–25: CART, CHECKOUT & ORDERS
// ==========================================
class CartScreen extends StatelessWidget {
  const CartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(title: Text('My Cart (${state.cart.length} items)')),
      body: state.cart.isEmpty
          ? const Center(child: Text('Your Cart is Empty'))
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  ...state.cart.map((item) => Card(
                        child: Padding(
                          padding: const EdgeInsets.all(10),
                          child: Row(
                            children: [
                              Text(item.product.emoji, style: const TextStyle(fontSize: 28)),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(item.product.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11)),
                                    Text('₹${item.unitPrice.toInt()}'),
                                  ],
                                ),
                              ),
                              Row(
                                children: [
                                  IconButton(icon: const Icon(Icons.remove, size: 16), onPressed: () => state.updateCartQty(item.product.id, -1)),
                                  Text('${item.quantity}', style: const TextStyle(fontWeight: FontWeight.bold)),
                                  IconButton(icon: const Icon(Icons.add, size: 16), onPressed: () => state.updateCartQty(item.product.id, 1)),
                                ],
