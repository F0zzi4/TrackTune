# 🎵 TrackTune
![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-green?logo=java)
![Build](https://img.shields.io/badge/build-Maven-blueviolet)
![Last Commit](https://img.shields.io/github/last-commit/F0zzi4/TrackTune)

## Software Engineering Course Assessment
🎧 **TrackTune** – Java project to design a management system related to sheet music, texts, chords, MIDI, MP3, videos, YouTube links, and more.

---

## 🛠️ Technologies & Dependencies

- **Java 21**
- **JavaFX 21**
- **MaterialFX** – Modern and responsive UI components
- **Ikonli** – Icon packs with FontAwesome & Material Design
- **FormsFX** – Form creation and binding library
- **ControlsFX / BootstrapFX** – Enhanced controls and styling
- **SQLite** – Embedded relational database
- **JUnit 5** – Unit testing framework

---

## 🚀 Getting Started

### 📦 Clone the Repository

```bash
git clone https://github.com/F0zzi4/TrackTune.git
cd TrackTune
```

---

## 🔧 Deployment
```
mvn clean package // from the root directory ..\TrackTune\
java --module-path "C:\JavaFX\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml -jar TrackTune-1.0.0.0.jar
jpackage ^
--input "C:/Users/ACER/IdeaProjects/TrackTune/target" ^
--name "TrackTune" ^
--main-jar "tracktune-1.0.0.0-jar-with-dependencies.jar" ^
--main-class "app.tracktune.Main" ^
--type exe ^
--icon "C:/Users/ACER/IdeaProjects/TrackTune/src/resources/assets/icon/appIcon.ico" ^
--runtime-image "custom-javafx-runtime" ^
--win-menu ^
--win-shortcut ^
--win-dir-chooser ^
--app-version "1.0.0" ^
--vendor "TrackTune Team"

```

## 👤 Author
[![GitHub](https://img.shields.io/badge/GitHub-@F0zzi4-181717?logo=github)](https://github.com/F0zzi4)
[![GitHub](https://img.shields.io/badge/GitHub-@MattiaRebonato-181717?logo=github)](https://github.com/MattiaRebonato)
