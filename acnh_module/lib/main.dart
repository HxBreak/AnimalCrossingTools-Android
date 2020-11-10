import 'package:acnh_module/platform/native_app.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/scheduler.dart';
import 'platform/platform.dart';

import 'screens/screens.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final theme = await NativePlatform().currentTheme();
  runApp(MyThemedApp(
    theme: theme,
  ));
}

class CustomMaterialPageRoute<T> extends PageRoute<T>
    with MaterialRouteTransitionMixin<T> {
  CustomMaterialPageRoute({
    @required this.builder,
    RouteSettings settings,
    this.maintainState = true,
    bool fullscreenDialog = false,
  })  : assert(builder != null),
        assert(maintainState != null),
        assert(fullscreenDialog != null),
        super(settings: settings, fullscreenDialog: fullscreenDialog);

  @override
  final WidgetBuilder builder;

  @override
  final bool maintainState;

  @override
  Future<RoutePopDisposition> willPop() async {
    final result = super.willPop();
    // ignore: unrelated_type_equality_checks
    if (result == RoutePopDisposition.bubble) {
      return RoutePopDisposition.bubble;
    }
    return result;
  }

  AnimationController controller;

  @override
  AnimationController createAnimationController() {
    controller = super.createAnimationController();
    return controller;
  }

  @override
  void didChangePrevious(Route<dynamic> previousRoute) {
    super.didChangePrevious(previousRoute);
    if (previousRoute?.settings?.name == '/') {
      controller.duration = Duration.zero;
      controller.reverseDuration = Duration.zero;
      if (controller.isAnimating) {
        controller.stop();
        controller.forward(from: 1);
      }
    }
    print([settings, 'didChangePrevious', previousRoute?.settings]);
  }

  @override
  void didPopNext(Route<dynamic> nextRoute) {
    super.didPopNext(nextRoute);
    if (settings.name == '/') {
      controller.duration = Duration.zero;
    }
    print([settings, 'didPopNext']);
  }

  @override
  void didChangeNext(Route<dynamic> nextRoute) {
    super.didChangeNext(nextRoute);
    print([settings, 'didChangeNext', nextRoute?.settings]);
  }

  @override
  bool didPop(T result) {
    print([settings, 'didPop']);
    return super.didPop(result);
  }

  @override
  String get debugLabel => '${super.debugLabel}(${settings.name})';

  @override
  Widget buildContent(BuildContext context) => builder(context);
}

class SimpleRouteInfoParser extends RouteInformationParser<RouteInformation> {
  @override
  Future<RouteInformation> parseRouteInformation(
      RouteInformation routeInformation) {
    return Future.value(routeInformation);
  }

  @override
  RouteInformation restoreRouteInformation(RouteInformation configuration) {
    return configuration;
  }
}

class SimpleRouteDelegate extends RouterDelegate<RouteInformation>
    with ChangeNotifier, PopNavigatorRouterDelegateMixin<RouteInformation> {
  SimpleRouteDelegate(this.routes, this._navigatorKey);

  final Map<String, WidgetBuilder> routes;
  final GlobalKey<NavigatorState> _navigatorKey;

  @override
  Widget build(BuildContext context) {
    return Navigator(
      key: _navigatorKey,
      onGenerateInitialRoutes: Navigator.defaultGenerateInitialRoutes,
      onUnknownRoute: (settings) => CustomMaterialPageRoute(
          builder: (context) => UnknownScreen(settings)),
      onPopPage: (Route<dynamic> route, result) {
        final _result = route.didPop(result);
        if (_result) {
          notifyListeners();
        }
        return _result;
      },
      onGenerateRoute: (settings) {
        if (!routes.keys.contains(settings.name)) {
          return null;
        }
        return CustomMaterialPageRoute(
            builder: routes[settings.name], settings: settings);
      },
      reportsRouteUpdateToEngine: true,
    );
  }

  @override
  GlobalKey<NavigatorState> get navigatorKey => _navigatorKey;

  @override
  Future<void> setNewRoutePath(RouteInformation configuration) async {
    _navigatorKey.currentState.pushNamed(configuration.location);
    return;
  }

  @override
  Future<bool> popRoute() async {
    return true;
  }
}

class CustomRootBackButtonDispatcher extends BackButtonDispatcher
    with WidgetsBindingObserver {
  CustomRootBackButtonDispatcher(this._navigatorKey);
  final GlobalKey<NavigatorState> _navigatorKey;

  @override
  void addCallback(ValueGetter<Future<bool>> callback) {
    if (!hasCallbacks) WidgetsBinding.instance.addObserver(this);
    super.addCallback(callback);
  }

  @override
  void removeCallback(ValueGetter<Future<bool>> callback) {
    super.removeCallback(callback);
    if (!hasCallbacks) WidgetsBinding.instance.removeObserver(this);
  }

  @override
  Future<bool> didPopRoute() async {
    if (_navigatorKey.currentState == null) return false;
    return await _navigatorKey.currentState.maybePop();
  }
}

class MyThemedApp extends StatefulWidget {
  final NativeThemeMode theme;

  MyThemedApp({this.theme = NativeThemeMode.LIGHT});

  @override
  _MyThemedAppState createState() => _MyThemedAppState();
}

class _MyThemedAppState extends State<MyThemedApp> {
  final _navigatorKey = GlobalKey<NavigatorState>();
  final navigationChannel =
      MethodChannel("com.hxbreak.animalcrossingtools/navigation");
  NativeThemeMode themeMode;

  _MyThemedAppState() {
    // navigationChannel.setMethodCallHandler((call) async {
    // switch(call.method){
    // case "pushRoute": _navigatorKey.currentState?.push(route)
    // }
    // });
  }

  @override
  void initState() {
    super.initState();
    NativePlatform().nativeEvent.receiveBroadcastStream().listen((event) {
      if (event['type'] == 'theme' &&
          themeMode != NativePlatform.toTheme(event['data'])) {
        setState(() {
          themeMode = NativePlatform.toTheme(event['data']);
        });
      }
    }, cancelOnError: true);
  }

  ThemeMode _currentTheme() {
    final _mode = themeMode ?? widget.theme;
    switch (_mode) {
      case NativeThemeMode.LIGHT:
        return ThemeMode.light;
      case NativeThemeMode.DARK:
        return ThemeMode.dark;
      default:
        return ThemeMode.system;
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      themeMode: _currentTheme(),
      routeInformationParser: SimpleRouteInfoParser(),
      routerDelegate: SimpleRouteDelegate({
        "/": (c) => SizedBox(),
        "/about": (c) => AboutScreen(),
      }, _navigatorKey),
      title: "Flutter Demo",
      theme: ThemeData(
        platform: TargetPlatform.android,
        primarySwatch: MaterialColor(
          0xFF1565c0,
          <int, Color>{
            50: Color(0xFFE3F2FD),
            100: Color(0xFFBBDEFB),
            200: Color(0xFF90CAF9),
            300: Color(0xFF64B5F6),
            400: Color(0xFF42A5F5),
            500: Color(0xFF1565c0),
            600: Color(0xFF1E88E5),
            700: Color(0xFF1976D2),
            800: Color(0xFF1565C0),
            900: Color(0xFF0D47A1),
          },
        ),
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      darkTheme: ThemeData.dark().copyWith(
        primaryColor: Color(0xFF003c8f),
        primaryColorDark: MaterialColor(
          0xFF003c8f,
          <int, Color>{
            50: Color(0xFFE3F2FD),
            100: Color(0xFFBBDEFB),
            200: Color(0xFF90CAF9),
            300: Color(0xFF64B5F6),
            400: Color(0xFF42A5F5),
            500: Color(0xFF003c8f),
            600: Color(0xFF1E88E5),
            700: Color(0xFF1976D2),
            800: Color(0xFF1565C0),
            900: Color(0xFF0D47A1),
          },
        ),
      ),
      backButtonDispatcher: CustomRootBackButtonDispatcher(_navigatorKey),
    );
  }
}
