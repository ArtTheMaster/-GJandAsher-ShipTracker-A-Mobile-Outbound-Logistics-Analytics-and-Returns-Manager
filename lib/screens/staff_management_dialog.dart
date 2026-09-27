import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';

class StaffManagementDialog extends StatefulWidget {
  final ShipTrackerController controller;

  const StaffManagementDialog({super.key, required this.controller});

  @override
  State<StaffManagementDialog> createState() => _StaffManagementDialogState();
}

class _StaffManagementDialogState extends State<StaffManagementDialog> {
  bool _isAddingStaff = false;
  final _usernameCtrl = TextEditingController();
  final _fullNameCtrl = TextEditingController();
  final _pinCtrl = TextEditingController();

  bool _canScan = true;
  bool _canDispatch = true;
  bool _canReturns = true;
  bool _canEdit = true;

  @override
  void dispose() {
    _usernameCtrl.dispose();
    _fullNameCtrl.dispose();
    _pinCtrl.dispose();
    super.dispose();
  }

  void _saveStaff() {
    widget.controller.addStaffMemberByOwner(
      username: _usernameCtrl.text,
      fullName: _fullNameCtrl.text,
      pin: _pinCtrl.text,
      canScan: _canScan,
      canDispatch: _canDispatch,
      canReturns: _canReturns,
      canEdit: _canEdit,
      callback: (success, msg) {
        if (success) {
          setState(() {
            _isAddingStaff = false;
            _usernameCtrl.clear();
            _fullNameCtrl.clear();
            _pinCtrl.clear();
          });
        }
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
      },
    );
  }

  void _promptDeactivate(User user) {
    if (user.role == UserRole.owner) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cannot deactivate the Owner account!')),
      );
      return;
    }

    final reasonCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(user.isActive ? 'Deactivate Staff Access?' : 'Reactivate Staff Access?'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Action on account @${user.username} (${user.fullName}).'),
            if (user.isActive) ...[
              const SizedBox(height: 12),
              TextField(
                controller: reasonCtrl,
                decoration: const InputDecoration(
                  labelText: 'Reason for deactivation',
                  hintText: 'e.g. Resigned, End of Contract',
                ),
              ),
            ],
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: user.isActive ? Colors.red : const Color(0xFF059669),
            ),
            onPressed: () {
              Navigator.pop(ctx);
              widget.controller.toggleStaffStatus(user, reasonCtrl.text);
            },
            child: Text(user.isActive ? 'Deactivate' : 'Reactivate'),
          ),
        ],
      ),
    );
  }

  void _editPermissions(User user) {
    bool canScan = user.canScanParcels;
    bool canDispatch = user.canDispatch;
    bool canReturns = user.canHandleReturns;
    bool canEdit = user.canEditShipments;

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setLocalState) {
          return AlertDialog(
            title: Text('Edit Permissions: ${user.fullName}'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                SwitchListTile(
                  title: const Text('Can Scan Parcels'),
                  value: canScan,
                  onChanged: (v) => setLocalState(() => canScan = v),
                ),
                SwitchListTile(
                  title: const Text('Can Dispatch Batches'),
                  value: canDispatch,
                  onChanged: (v) => setLocalState(() => canDispatch = v),
                ),
                SwitchListTile(
                  title: const Text('Can Log Returns'),
                  value: canReturns,
                  onChanged: (v) => setLocalState(() => canReturns = v),
                ),
                SwitchListTile(
                  title: const Text('Can Edit / Update Shipments'),
                  value: canEdit,
                  onChanged: (v) => setLocalState(() => canEdit = v),
                ),
              ],
            ),
            actions: [
              TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
              ElevatedButton(
                onPressed: () {
                  Navigator.pop(ctx);
                  widget.controller.updateStaffPermissions(
                    user,
                    canScan: canScan,
                    canDispatch: canDispatch,
                    canHandleReturns: canReturns,
                    canEdit: canEdit,
                  );
                },
                child: const Text('Save Permissions'),
              ),
            ],
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 600, maxHeight: 720),
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E3A8A).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Icon(Icons.manage_accounts, color: Color(0xFF1E3A8A)),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Staff & Roles Manager',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        Text(
                          'Owner Nolan: Configure permissions & active accounts',
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

              if (!_isAddingStaff) ...[
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'ACTIVE STAFF ACCOUNTS (${widget.controller.users.length})',
                      style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF64748B)),
                    ),
                    ElevatedButton.icon(
                      onPressed: () => setState(() => _isAddingStaff = true),
                      icon: const Icon(Icons.person_add, size: 16),
                      label: const Text('Add Staff Member'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        minimumSize: const Size(140, 36),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Expanded(
                  child: AnimatedBuilder(
                    animation: widget.controller,
                    builder: (context, _) {
                      final users = widget.controller.users;
                      return ListView.separated(
                        itemCount: users.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 8),
                        itemBuilder: (context, idx) {
                          final u = users[idx];
                          final isOwner = u.role == UserRole.owner;

                          return Card(
                            color: !u.isActive ? Colors.grey[100] : null,
                            child: Padding(
                              padding: const EdgeInsets.all(12),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    children: [
                                      CircleAvatar(
                                        radius: 18,
                                        backgroundColor: isOwner ? const Color(0xFF1E3A8A) : const Color(0xFF0284C7),
                                        child: Text(
                                          u.fullName.isNotEmpty ? u.fullName[0] : '?',
                                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13),
                                        ),
                                      ),
                                      const SizedBox(width: 10),
                                      Expanded(
                                        child: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            Row(
                                              children: [
                                                Flexible(
                                                  child: Text(
                                                    u.fullName,
                                                    style: TextStyle(
                                                      fontWeight: FontWeight.bold,
                                                      fontSize: 14,
                                                      decoration: !u.isActive ? TextDecoration.lineThrough : null,
                                                    ),
                                                    maxLines: 1,
                                                    overflow: TextOverflow.ellipsis,
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
                                                if (!u.isActive) ...[
                                                  const SizedBox(width: 6),
                                                  Container(
                                                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                                    decoration: BoxDecoration(
                                                      color: Colors.red.withOpacity(0.1),
                                                      borderRadius: BorderRadius.circular(4),
                                                    ),
                                                    child: const Text(
                                                      'DEACTIVATED',
                                                      style: TextStyle(fontSize: 9, color: Colors.red, fontWeight: FontWeight.bold),
                                                    ),
                                                  ),
                                                ],
                                              ],
                                            ),
                                            const SizedBox(height: 2),
                                            Text(
                                              '@${u.username} • PIN: ${u.pin}',
                                              style: const TextStyle(fontSize: 11, color: Color(0xFF64748B)),
                                            ),
                                          ],
                                        ),
                                      ),
                                      if (!isOwner) ...[
                                        IconButton(
                                          icon: const Icon(Icons.tune_outlined, size: 20),
                                          tooltip: 'Edit Permissions',
                                          onPressed: () => _editPermissions(u),
                                        ),
                                        IconButton(
                                          icon: Icon(
                                            u.isActive ? Icons.person_off_outlined : Icons.person_outline,
                                            size: 20,
                                            color: u.isActive ? Colors.red : const Color(0xFF059669),
                                          ),
                                          tooltip: u.isActive ? 'Deactivate' : 'Reactivate',
                                          onPressed: () => _promptDeactivate(u),
                                        ),
                                      ],
                                    ],
                                  ),
                                  if (!isOwner) ...[
                                    const SizedBox(height: 8),
                                    Wrap(
                                      spacing: 6,
                                      runSpacing: 4,
                                      children: [
                                        _PermChip(label: 'Scan', active: u.canScanParcels),
                                        _PermChip(label: 'Dispatch', active: u.canDispatch),
                                        _PermChip(label: 'Returns', active: u.canHandleReturns),
                                        _PermChip(label: 'Edit', active: u.canEditShipments),
                                      ],
                                    ),
                                  ],
                                ],
                              ),
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
              ] else ...[
                // Add Staff Form
                Expanded(
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        const Text(
                          'Configure New Staff Member',
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                        ),
                        const SizedBox(height: 12),
                        TextField(
                          controller: _fullNameCtrl,
                          decoration: const InputDecoration(labelText: 'Full Name', hintText: 'e.g. Maria Santos'),
                        ),
                        const SizedBox(height: 10),
                        TextField(
                          controller: _usernameCtrl,
                          decoration: const InputDecoration(labelText: 'Username', hintText: 'e.g. mariasantos'),
                        ),
                        const SizedBox(height: 10),
                        TextField(
                          controller: _pinCtrl,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: '4-Digit PIN', hintText: 'e.g. 5678'),
                        ),
                        const SizedBox(height: 14),
                        const Text('Operational Permissions:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                        SwitchListTile(
                          title: const Text('Parcel Scanning & Logging'),
                          value: _canScan,
                          onChanged: (v) => setState(() => _canScan = v),
                        ),
                        SwitchListTile(
                          title: const Text('Batch Dispatch Handover'),
                          value: _canDispatch,
                          onChanged: (v) => setState(() => _canDispatch = v),
                        ),
                        SwitchListTile(
                          title: const Text('Log Customer Returns'),
                          value: _canReturns,
                          onChanged: (v) => setState(() => _canReturns = v),
                        ),
                        SwitchListTile(
                          title: const Text('Edit / Update Parcels'),
                          value: _canEdit,
                          onChanged: (v) => setState(() => _canEdit = v),
                        ),
                        const SizedBox(height: 16),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            TextButton(
                              onPressed: () => setState(() => _isAddingStaff = false),
                              child: const Text('Back to List'),
                            ),
                            const SizedBox(width: 8),
                            ElevatedButton(
                              onPressed: _saveStaff,
                              child: const Text('Save Staff Member'),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _PermChip extends StatelessWidget {
  final String label;
  final bool active;

  const _PermChip({required this.label, required this.active});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: active ? const Color(0xFFDCFCE7) : const Color(0xFFF1F5F9),
        borderRadius: BorderRadius.circular(4),
        border: Border.all(
          color: active ? const Color(0xFF86EFAC) : const Color(0xFFCBD5E1),
        ),
      ),
      child: Text(
        '$label: ${active ? "YES" : "NO"}',
        style: TextStyle(
          fontSize: 10,
          fontWeight: FontWeight.bold,
          color: active ? const Color(0xFF15803D) : const Color(0xFF64748B),
        ),
      ),
    );
  }
}
