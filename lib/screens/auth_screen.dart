import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';
import '../theme/app_theme.dart';

class AuthScreen extends StatefulWidget {
  final ShipTrackerController controller;

  const AuthScreen({super.key, required this.controller});

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final _usernameController = TextEditingController();
  final _pinController = TextEditingController();
  bool _obscurePin = true;
  String? _errorMessage;

  @override
  void dispose() {
    _usernameController.dispose();
    _pinController.dispose();
    super.dispose();
  }

  void _onSignIn() {
    setState(() => _errorMessage = null);
    final user = _usernameController.text.trim();
    final pin = _pinController.text.trim();

    if (user.isEmpty || pin.isEmpty) {
      setState(() => _errorMessage = 'Please enter both username and access PIN');
      return;
    }

    widget.controller.authenticate(user, pin, (success, msg) {
      if (!success) {
        setState(() => _errorMessage = msg);
      }
    });
  }

  void _openSignUpSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => _StaffSignUpSheet(controller: widget.controller),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Header Logo & Badge
                  Center(
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: const Color(0xFF1E3A8A).withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.shield_outlined,
                        size: 48,
                        color: Color(0xFF1E3A8A),
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'GJandAsher Warehouse',
                    textAlign: TextAlign.center,
                    style: theme.textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                      letterSpacing: -0.5,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Sign in with your operational PIN',
                    textAlign: TextAlign.center,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: isDark ? Colors.grey[400] : const Color(0xFF64748B),
                    ),
                  ),
                  const SizedBox(height: 28),

                  // Quick Access Profile Chips
                  Text(
                    'QUICK SIGN-IN',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      letterSpacing: 1.2,
                      color: isDark ? Colors.grey[400] : const Color(0xFF64748B),
                    ),
                  ),
                  const SizedBox(height: 10),
                  AnimatedBuilder(
                    animation: widget.controller,
                    builder: (context, _) {
                      return Row(
                        children: widget.controller.users.map((user) {
                          final isOwner = user.role == UserRole.owner;
                          return Expanded(
                            child: Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 4),
                              child: InkWell(
                                onTap: () {
                                  _usernameController.text = user.username;
                                  _pinController.text = user.pin;
                                  _onSignIn();
                                },
                                borderRadius: BorderRadius.circular(12),
                                child: Container(
                                  padding: const EdgeInsets.all(12),
                                  decoration: BoxDecoration(
                                    color: isOwner
                                        ? const Color(0xFF1E3A8A).withOpacity(0.08)
                                        : const Color(0xFF0284C7).withOpacity(0.08),
                                    borderRadius: BorderRadius.circular(12),
                                    border: Border.all(
                                      color: isOwner
                                          ? const Color(0xFF1E3A8A).withOpacity(0.3)
                                          : const Color(0xFF0284C7).withOpacity(0.3),
                                    ),
                                  ),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Row(
                                        children: [
                                          CircleAvatar(
                                            radius: 12,
                                            backgroundColor: isOwner ? const Color(0xFF1E3A8A) : const Color(0xFF0284C7),
                                            child: Text(
                                              user.fullName.isNotEmpty ? user.fullName[0] : '?',
                                              style: const TextStyle(fontSize: 11, color: Colors.white, fontWeight: FontWeight.bold),
                                            ),
                                          ),
                                          const SizedBox(width: 6),
                                          Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                            decoration: BoxDecoration(
                                              color: isOwner ? const Color(0xFF1E3A8A) : const Color(0xFF0284C7),
                                              borderRadius: BorderRadius.circular(4),
                                            ),
                                            child: Text(
                                              isOwner ? 'OWNER' : 'STAFF',
                                              style: const TextStyle(fontSize: 9, color: Colors.white, fontWeight: FontWeight.bold),
                                            ),
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 8),
                                      Text(
                                        user.fullName,
                                        style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold),
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      Text(
                                        '@${user.username} (PIN: ${user.pin})',
                                        style: TextStyle(
                                          fontSize: 11,
                                          color: isDark ? Colors.grey[400] : const Color(0xFF64748B),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ),
                          );
                        }).toList(),
                      );
                    },
                  ),

                  const SizedBox(height: 24),

                  // Credential Input Card
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          TextField(
                            controller: _usernameController,
                            decoration: const InputDecoration(
                              labelText: 'Username',
                              prefixIcon: Icon(Icons.person_outline),
                              hintText: 'e.g. nolancaparros, gjcaparros',
                            ),
                          ),
                          const SizedBox(height: 16),
                          TextField(
                            controller: _pinController,
                            obscureText: _obscurePin,
                            keyboardType: TextInputType.number,
                            decoration: InputDecoration(
                              labelText: 'Security PIN',
                              prefixIcon: const Icon(Icons.lock_outline),
                              suffixIcon: IconButton(
                                icon: Icon(_obscurePin ? Icons.visibility_off : Icons.visibility),
                                onPressed: () => setState(() => _obscurePin = !_obscurePin),
                              ),
                            ),
                          ),
                          if (_errorMessage != null) ...[
                            const SizedBox(height: 14),
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: const Color(0xFFFEE2E2),
                                borderRadius: BorderRadius.circular(8),
                                border: Border.all(color: const Color(0xFFF87171)),
                              ),
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Icon(Icons.error_outline, size: 18, color: Color(0xFFB91C1C)),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      _errorMessage!,
                                      style: const TextStyle(color: Color(0xFF991B1B), fontSize: 13),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                          const SizedBox(height: 20),
                          ElevatedButton(
                            onPressed: _onSignIn,
                            child: const Text('Sign In to Warehouse Terminal'),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 20),

                  // Staff Sign Up Trigger (Operational Role Assignment Removed!)
                  OutlinedButton.icon(
                    onPressed: _openSignUpSheet,
                    icon: const Icon(Icons.person_add_alt_1_outlined),
                    label: const Text('New Staff Member? Create Account'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    '🔒 Staff accounts default to warehouse staff. Only Owner Nolan can manage operational permissions.',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 11, color: Color(0xFF94A3B8)),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _StaffSignUpSheet extends StatefulWidget {
  final ShipTrackerController controller;

  const _StaffSignUpSheet({required this.controller});

  @override
  State<_StaffSignUpSheet> createState() => _StaffSignUpSheetState();
}

class _StaffSignUpSheetState extends State<_StaffSignUpSheet> {
  final _nameController = TextEditingController();
  final _userController = TextEditingController();
  final _pinController = TextEditingController();
  String? _error;

  @override
  void dispose() {
    _nameController.dispose();
    _userController.dispose();
    _pinController.dispose();
    super.dispose();
  }

  void _submit() {
    setState(() => _error = null);
    widget.controller.registerAccount(
      fullName: _nameController.text,
      username: _userController.text,
      pin: _pinController.text,
      callback: (success, msg) {
        if (success) {
          Navigator.pop(context);
        } else {
          setState(() => _error = msg);
        }
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
        top: 24,
        left: 24,
        right: 24,
      ),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFF0284C7).withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.badge_outlined, color: Color(0xFF0284C7)),
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Staff Account Registration',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      Text(
                        'Warehouse Staff Access Only',
                        style: TextStyle(fontSize: 12, color: Color(0xFF64748B)),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: const Color(0xFFF0FDF4),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: const Color(0xFF86EFAC)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.info_outline, size: 18, color: Color(0xFF16A34A)),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Operational Role: Automatically assigned as Warehouse Staff. Only Owner Nolan can modify roles and permission flags.',
                      style: TextStyle(fontSize: 12, color: Color(0xFF15803D)),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Full Name',
                prefixIcon: Icon(Icons.badge_outlined),
                hintText: 'e.g. Maria Santos',
              ),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: _userController,
              decoration: const InputDecoration(
                labelText: 'Username',
                prefixIcon: Icon(Icons.alternate_email),
                hintText: 'e.g. mariasantos',
              ),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: _pinController,
              keyboardType: TextInputType.number,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Access PIN (4 digits)',
                prefixIcon: Icon(Icons.lock_outline),
                hintText: 'e.g. 5678',
              ),
            ),
            if (_error != null) ...[
              const SizedBox(height: 12),
              Text(
                _error!,
                style: const TextStyle(color: Color(0xFFDC2626), fontSize: 13, fontWeight: FontWeight.w500),
              ),
            ],
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _submit,
              child: const Text('Create Staff Account & Sign In'),
            ),
          ],
        ),
      ),
    );
  }
}
