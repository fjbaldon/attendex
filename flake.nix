{
  description = "attendex";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
        };

        javaVersion = pkgs.temurin-bin-21;

        gradleConfigured = pkgs.gradle.override {
          java = javaVersion;
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            javaVersion
            gradleConfigured
            pkgs.kotlin
            pkgs.nodejs_22
            pkgs.postgresql_16
            pkgs.podman-compose
          ];

          shellHook = ''
            ln -Tfs ${javaVersion} .jdk
            export JAVA_HOME=$PWD/.jdk
            export ANDROID_HOME=$HOME/Android/Sdk
            export PATH=$PATH:$ANDROID_HOME/platform-tools

            if [ ! -f .env ] && [ -f .env.example ]; then
                echo "Creating .env from .env.example..."
                cp .env.example .env
            fi

            if [ -f .env ]; then
                set -a
                source .env
                set +a
            fi

            echo "=============================================="
            echo "Attendex Dev Environment"
            echo "----------------------------------------------"
            echo "Java:   $(java -version 2>&1 | head -n 2 | tail -n 1)"
            echo "DB:     Run 'podman-compose up -d' to start"
            echo "=============================================="
          '';
        };
      }
    );
}
