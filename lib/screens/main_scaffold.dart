import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';
import '../theme/app_theme.dart';
import 'dashboard_screen.dart';
import 'dispatch_screen.dart';
import 'returns_screen.dart';
import 'rule_60_30_10_dialog.dart';
import 'scan_screen.dart';
import 'shipments_screen.dart';
import 'staff_management_dialog.dart';

class MainScaffold extends StatelessWidget {
  final ShipTrackerController controller;

  const MainScaffold({super.key, required this.controller});

  void _openStaffManager(BuildContext context) {
    if (controller.currentUser?.role != UserRole.owner) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('🔒 Access Restricted: Only Owner Nolan can manage staff and permissions.'),
          backgroundColor: Color(0xFFDC2626),
        ),
      );
      return;
    }

    showDialog(
      context: context,
      builder: (ctx) => StaffManagementDialog(controller: controller),
    );
  }

  void _showUserSwitcher(BuildContext context) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Switch Operational Account',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              const Text(
                'Select an active profile to change terminal operator',
                style: TextStyle(fontSize: 12, color: Color(0xFF64748B)),
              ),
              const SizedBox(height: 16),
              ...controller.users.map((u) {
                final isCurrent = u.id == controller.currentUser?.id;
                final isOwner = u.role == UserRole.owner;
                return ListTile(
                  leading: CircleAvatar(
                    backgroundColor: isOwner ? const Color(0xFF1E3A8A) : const Color(0xFF0284C7),
                    child: Text(
                      u.fullName.isNotEmpty ? u.fullName[0] : '?',
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  ),
                  title: Text(u.fullName, style: const TextStyle(fontWeight: FontWeight.w600)),
                  subtitle: Text(
                    '@${u.username} • ${u.role.label} ${!u.isActive ? "• (DEACTIVATED)" : ""}',
                    style: TextStyle(color: u.isActive ? const Color(0xFF64748B) : Colors.red),
                  ),
                  trailing: isCurrent
                      ? const Icon(Icons.check_circle, color: Color(0xFF10B981))
                      : null,
                  onTap: () {
                    Navigator.pop(ctx);
                    controller.quickSignIn(u, (success, msg) {
                      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
                    });
                  },
                );
              }),
              const Divider(height: 24),
              ListTile(
                leading: const Icon(Icons.logout, color: Colors.red),
                title: const Text('Sign Out', style: TextStyle(color: Colors.red, fontWeight: FontWeight.w600)),
                onTap: () {
                  Navigator.pop(ctx);
                  controller.logout();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final user = controller.currentUser;
    final isOwner = user?.role == UserRole.owner;

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 16,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Text(
                  'GJandAsher',
                  style: TextStyle(fontSize: 17, fontWeight: FontWeight.bold, letterSpacing: -0.3),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: isOwner ? const Color(0xFF1E3A8A) : const Color(0xFF0284C7),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    isOwner ? 'OWNER' : 'STAFF',
                    style: const TextStyle(fontSize: 10, color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
            Text(
              user?.fullName ?? 'Operator',
              style: const TextStyle(fontSize: 12, color: Color(0xFF64748B)),
            ),
          ],
        ),
        actions: [
          // Offline pending sync chip
          if (controller.pendingSyncCount > 0)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: ActionChip(
                backgroundColor: const Color(0xFFFEF3C7),
                side: const BorderSide(color: Color(0xFFF59E0B)),
                avatar: const Icon(Icons.cloud_upload_outlined, size: 16, color: Color(0xFFB45309)),
                label: Text(
                  '${controller.pendingSyncCount} Sync',
                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFFB45309)),
                ),
                onPressed: () {
                  controller.syncPendingRecords((count) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('✅ Synced $count pending offline parcels!')),
                    );
                  });
                },
              ),
            ),

          // 60-30-10 Design Architecture Inspector
          IconButton(
            icon: const Icon(Icons.palette_outlined, color: ColorRule603010.accentLogisticsBlue),
            tooltip: '60-30-10 Rule Inspector (Color & Spatial Architecture)',
            onPressed: () => Rule603010Dialog.show(context),
          ),

          // Owner Staff Management Button
          if (isOwner)
            IconButton(
              icon: const Icon(Icons.manage_accounts_outlined),
              tooltip: 'Staff & Roles Manager (Owner Only)',
              onPressed: () => _openStaffManager(context),
            ),

          // Profile / Switch Account
          IconButton(
            icon: CircleAvatar(
              radius: 14,
              backgroundColor: isOwner ? const Color(0xFF1E3A8A) : const Color(0xFF0284C7),
              child: Text(
                user != null && user.fullName.isNotEmpty ? user.fullName[0] : '?',
                style: const TextStyle(fontSize: 12, color: Colors.white, fontWeight: FontWeight.bold),
              ),
            ),
            tooltip: 'User Switcher / Sign Out',
            onPressed: () => _showUserSwitcher(context),
          ),
          const SizedBox(width: 4),
        ],
      ),
      body: AnimatedBuilder(
        animation: controller,
        builder: (context, _) {
          return IndexedStack(
            index: controller.currentTab.index,
            children: [
              ShipmentsScreen(controller: controller),
              DispatchScreen(controller: controller),
              ScanScreen(controller: controller),
              ReturnsScreen(controller: controller),
              if (isOwner)
                DashboardScreen(controller: controller)
              else
                Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.lock_person_outlined, size: 64, color: Color(0xFF94A3B8)),
                      const SizedBox(height: 16),
                      const Text(
                        'Analytics Restricted to Owner Nolan',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Warehouse Staff accounts do not have access to business analytics.',
                        style: TextStyle(color: Color(0xFF64748B)),
                      ),
                    ],
                  ),
                ),
            ],
          );
        },
      ),
      bottomNavigationBar: AnimatedBuilder(
        animation: controller,
        builder: (context, _) {
          return NavigationBar(
            selectedIndex: controller.currentTab.index,
            onDestinationSelected: (idx) {
              final tab = ScreenTab.values[idx];
              if (tab == ScreenTab.dashboard && !isOwner) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('🔒 Analytics is restricted to Owner Nolan.'),
                    backgroundColor: Color(0xFFDC2626),
                  ),
                );
                return;
              }
              controller.selectTab(tab);
            },
            destinations: [
              const NavigationDestination(
                icon: Icon(Icons.inventory_2_outlined),
                selectedIcon: Icon(Icons.inventory_2),
                label: 'Parcels',
              ),
              const NavigationDestination(
                icon: Icon(Icons.local_shipping_outlined),
                selectedIcon: Icon(Icons.local_shipping),
                label: 'Dispatch',
              ),
              const NavigationDestination(
                icon: Icon(Icons.qr_code_scanner_outlined),
                selectedIcon: Icon(Icons.qr_code_scanner),
                label: 'Scan',
              ),
              const NavigationDestination(
                icon: Icon(Icons.assignment_return_outlined),
                selectedIcon: Icon(Icons.assignment_return),
                label: 'Returns',
              ),
              NavigationDestination(
                icon: Icon(isOwner ? Icons.insights_outlined : Icons.lock_outline),
                selectedIcon: const Icon(Icons.insights),
                label: isOwner ? 'Analytics' : 'Owner Only',
              ),
            ],
          );
        },
      ),
    );
  }
}
