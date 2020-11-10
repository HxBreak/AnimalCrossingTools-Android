import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:url_launcher/url_launcher.dart' as url;

class LibraryInformation {
  final String name;
  final String link;
  final String description;

  const LibraryInformation(this.name, this.link, this.description);
}

class AboutScreen extends StatefulWidget {
  @override
  _AboutScreenState createState() => _AboutScreenState();
}

class _AboutScreenState extends State<AboutScreen> {
  final librarys = [
    LibraryInformation(
      "okhttp",
      "https://github.com/square/okhttp",
      "Square’s meticulous HTTP client for the JVM, Android, and GraalVM.",
    ),
    LibraryInformation(
      "retrofit",
      "https://github.com/square/retrofit",
      "A type-safe HTTP client for Android and the JVM",
    ),
    LibraryInformation(
      "glide",
      "https://github.com/bumptech/glide",
      "An image loading and caching library for Android focused on smooth scrolling",
    ),
    LibraryInformation(
      "tinypinyin",
      "https://github.com/promeG/TinyPinyin",
      "适用于Java和Android的快速、低内存占用的汉字转拼音库。",
    ),
    LibraryInformation(
      "ExoPlayer",
      "https://github.com/google/ExoPlayer",
      "An extensible media player for Android",
    ),
    LibraryInformation(
      "Hilt",
      "https://developer.android.google.cn/training/dependency-injection/hilt-android",
      "Hilt is a dependency injection library for Android",
    ),
    LibraryInformation(
      "AssistedInject",
      "https://github.com/square/AssistedInject",
      "Assisted injection for JSR 330.",
    ),
    LibraryInformation(
      "netty",
      "https://netty.io/",
      "Netty project - an event-driven asynchronous network application framework",
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final isBrightness = Theme.of(context).brightness == Brightness.light;
    final onTapACNHAPI = () {
      url.launch("https://acnhapi.com");
    };
    final onTapOpenIssueTrack = () {
      url.launch(
          "https://github.com/HxBreak/AnimalCrossingTools-Android-track");
    };
    return Scaffold(
      body: CustomScrollView(
        primary: true,
        slivers: [
          SliverAppBar(
            title: Text("About"),
          ),
          SliverList(
            delegate: SliverChildListDelegate.fixed([
              Padding(
                padding: const EdgeInsets.only(top: 32),
              ),
              Center(
                child: Text(
                  "Animal Crossing Tools",
                  style: Theme.of(context).textTheme.headline4,
                ),
              ),
              Padding(
                padding: const EdgeInsets.only(top: 8),
              ),
              Text(
                "This is a app to help player view collect mark things of animal crossing.",
                style: Theme.of(context).textTheme.caption,
              ),
            ]),
          ),
          SliverList(
            delegate: SliverChildListDelegate.fixed([
              Padding(padding: const EdgeInsets.only(top: 32)),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8),
                child: OutlineButton.icon(
                  icon: Image.asset(
                    "images/github_logo${isBrightness ? "" : "_white"}.png",
                    width: 48,
                  ),
                  label: Text(
                    "Bug Report / Feature Request",
                  ),
                  onPressed: onTapOpenIssueTrack,
                ),
              )
            ]),
          ),
          SliverList(
            delegate: SliverChildListDelegate.fixed([
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: Wrap(
                  children: [
                    Text("All data is come from "),
                    GestureDetector(
                      child: Text(
                        "acnhapi.com",
                        style: Theme.of(context).textTheme.button.copyWith(
                              fontStyle: FontStyle.italic,
                              decoration: TextDecoration.underline,
                            ),
                      ),
                      onTap: onTapACNHAPI,
                    ),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(
                  vertical: 16,
                  horizontal: 8,
                ),
                child: Row(
                  children: [
                    Text(
                      "Application Made With: ",
                      style: Theme.of(context).textTheme.button,
                    ),
                    SvgPicture.asset(
                      "images/jetpack_hero.svg",
                      width: 48,
                      height: 48,
                    ),
                    FlutterLogo(size: 40)
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(
                  vertical: 16,
                  horizontal: 8,
                ),
                child: Text(
                  "Resource below are also useful for me.",
                  style: Theme.of(context).textTheme.caption,
                ),
              )
            ]),
          ),
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) {
                return ListTile(
                  title: Text(librarys[index].name),
                  subtitle: librarys[index].description == null
                      ? null
                      : Text(librarys[index].description),
                  onTap: () {
                    url.launch(librarys[index].link);
                  },
                );
              },
              childCount: librarys.length,
            ),
          ),
          SliverPadding(
            padding: const EdgeInsets.only(top: 32),
          )
        ],
      ),
    );
  }
}
