import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(MyApp());

@immutable
class NavigationHandler extends StatefulWidget {
  final Widget child;

  NavigationHandler({@required this.child}) : super();

  @override
  _NavigationHandlerState createState() => _NavigationHandlerState();
}

class _NavigationHandlerState extends State<NavigationHandler>
    with WidgetsBindingObserver {
  static const platform =
      const MethodChannel('com.hxbreak.animalcrossingtools/main');

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    print("Build App");
    return NavigationHandler(
      child: MaterialApp(
        title: 'Flutter Demo',
        theme: ThemeData(
          primarySwatch: Colors.purple,
        ),
        routes: {
          "/": (ctx) => MyHomePage(
                title: "Hello Flutter",
                skip: true,
              ),
          "req": (ctx) => MyHomePage(
                title: "ReqPage",
              ),
        },
      ),
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

  static const platform =
      const MethodChannel('com.hxbreak.animalcrossingtools/main');

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
    final backButton = BackButton(
            onPressed: () {
              WidgetsBinding.instance.handlePopRoute();
            },
          );
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        leading: backButton,
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
