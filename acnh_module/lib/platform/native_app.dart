import 'package:flutter/services.dart';

class NativePlatform {
  NativePlatform._();
  final nativeApp = const MethodChannel('com.hxbreak.animalcrossingtools/app');
  final nativeEvent =
      const EventChannel('com.hxbreak.animalcrossingtools/app/event');

  static final _instance = NativePlatform._();

  factory NativePlatform() {
    return _instance;
  }

  Future<NativeThemeMode> currentTheme() async {
    final String result = await nativeApp.invokeMethod("currentTheme");
    return toTheme(result);
  }

  static NativeThemeMode toTheme(String theme) {
    switch (theme) {
      case "light":
        return NativeThemeMode.LIGHT;
      case "dark":
        return NativeThemeMode.DARK;
      case "system":
        return NativeThemeMode.SYSTEM;
      case "battery_saver":
        return NativeThemeMode.BATTERY_SAVER;
      default:
        return NativeThemeMode.LIGHT;
    }
  }
}

enum NativeThemeMode {
  LIGHT,
  DARK,
  SYSTEM,
  BATTERY_SAVER,
}
