import 'package:acnh_module/platform/native_app.dart';
import 'package:flutter/material.dart';
import 'platform/platform.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    final theme = await NativePlatform().currentTheme();
    runApp(MyThemedApp(
      theme: theme,
    ));
  } catch (e) {
    runApp(MyThemedApp());
  }
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
        assert(opaque),
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
  void didAdd() {
    super.didAdd();
  }

  @override
  TickerFuture didPush() {
    return super.didPush();
  }

  @override
  void didChangePrevious(Route<dynamic> previousRoute) {
    super.didChangePrevious(previousRoute);
  }

  @override
  void didPopNext(Route<dynamic> nextRoute) {
    super.didPopNext(nextRoute);
  }

  @override
  void didChangeNext(Route<dynamic> nextRoute) {
    super.didChangeNext(nextRoute);
  }

  @override
  bool didPop(T result) {
    return super.didPop(result);
  }

  @override
  String get debugLabel => '${super.debugLabel}(${settings.name})';
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
          builder: (context) => Scaffold(
                body: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Builder(
                        builder: (context) => Text.rich(TextSpan(
                            text: "Unknown Route Path ",
                            style: Theme.of(context).textTheme.headline5,
                            children: [
                              TextSpan(
                                text: settings.name,
                                style: Theme.of(context)
                                    .textTheme
                                    .headline5
                                    .copyWith(
                                      color: Colors.red,
                                      fontStyle: FontStyle.italic,
                                    ),
                              )
                            ])),
                      ),
                      SizedBox(
                        height: 8,
                      ),
                      BackButton(),
                    ],
                  ),
                ),
              )),
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
  NativeThemeMode themeMode;

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
        "/": (c) => Scaffold(
              body: Center(
                child: Builder(
                  builder: (ctx) => FlatButton(
                      onPressed: () {
                        Navigator.pushNamed(ctx, "/about");
                      },
                      child: Text("Go")),
                ),
              ),
            ),
        "/about": (c) => MyHomePage(title: "about"),
      }, _navigatorKey),
      title: "Flutter Demo",
      theme: ThemeData(
        platform: TargetPlatform.iOS,
        primarySwatch: Colors.purple,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      darkTheme: ThemeData.dark().copyWith(
        primaryColorDark: Colors.red,
      ),
      backButtonDispatcher: CustomRootBackButtonDispatcher(_navigatorKey),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title, this.skip = false}) : super(key: key);

  final bool skip;
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'You have pushed the button this many times: ${Navigator.of(context).canPop()}',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}
