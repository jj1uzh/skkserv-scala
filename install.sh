#!/bin/sh -eux

sbt assembly

mkdir -p "$HOME/.config/skkserv-scala"
cp -n default-config.json ~/.config/skkserv-scala/config.json

path=$(readlink -f ./target/scala-2.13/skkserv-scala.jar)
service_dir="$HOME/.config/systemd/user"
mkdir -p "$service_dir"
cat skkserv.service | sed "s|__PATH__|$path|" > "$service_dir/skkserv-scala.service"

