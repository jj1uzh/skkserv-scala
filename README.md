# skkserv-scala
skkservのscala実装(雑な)

usage:
```shell
skkserv-scala $ sbt assembly 
skkserv-scala $ cp default-config.json ~/.config/skkserv-scala/config.json
skkserv-scala $ java -Xmx:??M -jar target/scala-2.13/skkserv-scala-assembly-0.1.jar
```

todo:
- テスト
- 辞書の自動ダウンロード
- S式評価?
