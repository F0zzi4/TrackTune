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
- **ControlsFX / BootstrapFX** – Enhanced controls and stylin
- **Apache PDFBox** - For reading PDF documents
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

## 🔧 Deployment (use your path directories)
```
mvn clean package
jpackage --input "C:/Users/ACER/IdeaProjects/TrackTune/target" --name "TrackTune" --main-jar "tracktune-1.0.0.0.jar" --main-class "app.tracktune.Main" --type exe --icon "C:/Users/ACER/IdeaProjects/TrackTune/src/resources/assets/icon/appIcon.ico" --win-menu --win-shortcut --win-dir-chooser --app-version "1.0.0" --vendor "TrackTune Team"   --java-options "--enable-native-access=ALL-UNNAMED" --java-options "--add-exports=javafx.base/com.sun.javafx=ALL-UNNAMED" --java-options "--add-exports=javafx.graphics/com.sun.glass.utils=ALL-UNNAMED" --java-options "--add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED" --java-options "--add-exports=javafx.media/com.sun.media.jfxmediaimpl=ALL-UNNAMED"

```

## 👤 Author
[![GitHub](https://img.shields.io/badge/GitHub-@F0zzi4-181717?logo=github)](https://github.com/F0zzi4)
[![GitHub](https://img.shields.io/badge/GitHub-@MattiaRebonato-181717?logo=github)](https://github.com/MattiaRebonato)
