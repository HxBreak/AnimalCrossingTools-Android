import 'package:flutter/material.dart';

class UnknownScreen extends StatefulWidget {
  final RouteSettings settings;

  UnknownScreen(this.settings);

  @override
  _UnknownScreenState createState() => _UnknownScreenState();
}

class _UnknownScreenState extends State<UnknownScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
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
                      text: widget.settings.name,
                      style: Theme.of(context).textTheme.headline5.copyWith(
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
    );
  }
}
