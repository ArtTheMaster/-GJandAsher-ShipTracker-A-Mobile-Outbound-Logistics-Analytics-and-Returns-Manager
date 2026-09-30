import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';
import 'controllers/ship_tracker_controller.dart';
import 'screens/auth_screen.dart';
import 'screens/main_scaffold.dart';
import 'screens/splash_screen.dart';
import 'theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  try {
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
    debugPrint('🔥 Firebase initialized successfully.');
  } catch (e) {
    debugPrint('⚠️ Firebase initialization note: $e');
  }

  runApp(const GJandAsherApp());
}

class GJandAsherApp extends StatefulWidget {
  const GJandAsherApp({super.key});

  @override
  State<GJandAsherApp> createState() => _GJandAsherAppState();
}

class _GJandAsherAppState extends State<GJandAsherApp> {
  late final ShipTrackerController _controller;

  @override
  void initState() {
    super.initState();
    _controller = ShipTrackerController();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, _) {
        return MaterialApp(
          title: 'GJandAsher ShipTracker',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.lightTheme,
          darkTheme: AppTheme.darkTheme,
          themeMode: ThemeMode.system,
          home: _buildCurrentScreen(),
        );
      },
    );
  }

  Widget _buildCurrentScreen() {
    if (_controller.isSplashVisible) {
      return SplashScreen(controller: _controller);
    }

    if (_controller.currentUser == null) {
      return AuthScreen(controller: _controller);
    }

    return MainScaffold(controller: _controller);
  }
}
