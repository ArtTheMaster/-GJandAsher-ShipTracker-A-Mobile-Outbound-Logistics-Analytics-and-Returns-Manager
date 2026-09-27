import '../models/models.dart';

class ClassificationResult {
  final PlatformType platform;
  final CourierType courier;
  final String confidence;

  const ClassificationResult({
    required this.platform,
    required this.courier,
    required this.confidence,
  });
}

class ClassificationHelper {
  static ClassificationResult classifyBarcode(String rawInput) {
    final code = rawInput.trim().toUpperCase();

    if (code.startsWith('SPX') || code.startsWith('630')) {
      return const ClassificationResult(
        platform: PlatformType.shopee,
        courier: CourierType.spx,
        confidence: 'Shopee Standard Delivery (High Confidence)',
      );
    }

    if (code.startsWith('JT') || code.startsWith('J&T') || code.startsWith('888') || code.startsWith('999')) {
      return const ClassificationResult(
        platform: PlatformType.shopee,
        courier: CourierType.jtExpress,
        confidence: 'J&T Express Shopee/Direct Airway (High Confidence)',
      );
    }

    if (code.startsWith('LZD') || code.startsWith('MP') || code.startsWith('LX')) {
      return const ClassificationResult(
        platform: PlatformType.lazada,
        courier: CourierType.ninjaVan,
        confidence: 'Lazada Marketplace Logistics (High Confidence)',
      );
    }

    if (code.startsWith('TT') || code.startsWith('990') || code.startsWith('TOK')) {
      return const ClassificationResult(
        platform: PlatformType.tiktok,
        courier: CourierType.flash,
        confidence: 'TikTok Shop Partner Delivery (High Confidence)',
      );
    }

    if (code.startsWith('LBC') || code.startsWith('177')) {
      return const ClassificationResult(
        platform: PlatformType.directOrder,
        courier: CourierType.lbc,
        confidence: 'LBC Outbound Air / Sea Freight (High Confidence)',
      );
    }

    if (code.startsWith('FBM') || code.startsWith('FB')) {
      return const ClassificationResult(
        platform: PlatformType.fbMarketplace,
        courier: CourierType.other,
        confidence: 'Facebook Marketplace Direct (High Confidence)',
      );
    }

    return const ClassificationResult(
      platform: PlatformType.shopee,
      courier: CourierType.jtExpress,
      confidence: 'Generic E-Commerce Format (Default assigned)',
    );
  }

  static bool isValidTrackingCode(String code) {
    final trimmed = code.trim();
    if (trimmed.length < 5) return false;
    final regex = RegExp(r'^[A-Za-z0-9\-]+$');
    return regex.hasMatch(trimmed);
  }
}
