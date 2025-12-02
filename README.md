# Sundenbock (Ticket-Tracking-System)
---
## Projektübersicht

`Sundenbock` ist eine Enterprise Web Application für das Management von Projekten, Tickets und dessen Benutzern. Die Anwendung ist als 3-Schichten-Architektur konzipiert:

- `Backend` Spring Boot 3 (Java 25) mit Spring Security (JWT), Spring Data JPA und MapStruct.<br><br>
- `Frontend` Angular (v20.3) mit TypeScript, **Tailwind CSS** und **DaisyUI** Komponenten. <br><br>
- `Datenbank (Dev)` H2 In-Memory-Datenbank, die beim Start, falls noch nicht Initialisiert, automatisch mit Beispieldaten befüllt wird. Zusätzlich ein Prod-Profil für eine PostgreSQL-Datenbank für späteres Deployment.

---

### Zusatzinformationen

- Alle API-Endpunkte erfordern eine Authentifizierung. Die einzigen Ausnahmen sind `/api/v1/auth/register` und `/api/v1/auth/authenticate`, die für den Registrierungs- und Login-Vorgang öffentlich zugänglich sind.<br><br>
 
- Zusätzlich sind aktuell die Endpunkte `/swagger-ui/**`, `/v3/api-docs/**` und `/h2-console/**` für Entwicklungs- und Review-Zwecke offen. Bei einem finalen Produktiv-Deployment würden diese Endpunkte gesichert oder entfernt und die H2-Datenbank durch PostgreSQL (siehe `application.yaml`) ersetzt.<br><br>
 
- Wenn ich die Zeit habe, wird die App auf meinem lokalen Linux Server in Docker laufen und unter der Domain `sundenbock.lars-hq.vip` erreichbar sein. -Lars

---

## 1. Voraussetzungen

Stellen Sie sicher, dass Sie die folgende Software haben, bevor Sie mit dem Setup fortfahren:

- [ Java Development Kit (JDK) 25](https://jdk.java.net/25/)
- [Node.js und npm](https://nodejs.org/en/download) (Empfohlen: LTS-Version 20.x, passend zu Angular 20)
- [Apache Maven](https://maven.apache.org/download.cgi) (für die mvn commands)
- [git](https://git-scm.com/install/windows)

IDE Backend:
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (empfohlene IDE für das Backend)

IDE Frontend:
- [Visual Studio Code](https://code.visualstudio.com/download) (empfohlene IDE für das Frontend)

---

## 2. Installation & Setup

Das Projektverzeichnis enthält zwei Hauptordner: backend und frontend. Die Schritte müssen für beide Teile getrennt durchgeführt werden.

### 2.1 Backend-Setup (IntelliJ)

1. Öffnen Sie IntelliJ IDEA.
2. Wählen Sie `File` -> `New` -> `Project from Version Control` oder `Get from VCS`.
3. Wählen Sie Git und geben Sie die Repository-URL ein: `https://gitlab2.nordakademie.de/JannickGottschalk-I22/iaa_hausarbeit_nicht_stolarczyk_pick_gottschalk.git`.
4. Klicken Sie auf `Clone`.
5. Loggen Sie sich mit Ihren Daten ein.
6. IntelliJ IDEA erkennt das Maven-Projekt automatisch und importiert es.

### 2.2 Frontend-Setup (VS Code)

1. Öffnen Sie Visual Studio Code.
2. Wählen Sie `File` -> `Open Folder...`
3. Öffnen Sie den frontend-Ordner.
4. Öffnen Sie ein neues Terminal in VS Code.
5. Installieren Sie alle npm-Abhängigkeiten:```npm install```
6. Installieren Sie die Angular CLI-Abhängigkeiten:```npm install -g @angular/cli``` (For the ng commands)

---

## 3. Anwendung lokal ausführen

Um die Anwendung zu nutzen, müssen beide Teile (Backend und Frontend) gleichzeitig laufen.

### 3.1 Backend starten (Port 8080)

Sie können das Backend direkt über IntelliJ oder per Maven im Terminal starten.

**Option A: Mit IntelliJ IDEA (Empfohlen)**

1.  Navigieren Sie zur Klasse `SundenbockApplication.java\` (im `src/main/java/de/nak/iaa/sundenbock\` Verzeichnis).
2.  Klicken Sie auf den grünen "Play"-Button neben der `main`-Methode.

**Option B: Mit Maven-Terminal**

1.  Öffnen Sie ein Terminal und navigieren Sie in den \`backend\`-Ordner.
2.  Starten Sie die Anwendung mit dem entsprechendeen mvn-Command:

```
mvn spring-boot:run
```

Das Backend läuft nun auf `http://localhost:8080`. Beim ersten Start wird die H2-Datenbank initialisiert und mit Beispieldaten befüllt.

Sie können die H2-Datenbank-Konsole im Browser unter `http://localhost:8080/h2-console` erreichen und die Daten mit der JDBC URL: `jdbc:h2:file:./data/sundenbock-db` und User Name:  `sa` einsehen (Details in \`application-dev.yaml\`).

### 3.2 Frontend starten (Port 4200)

1.  Öffnen Sie ein zweites Terminal (das Backend muss weiterlaufen!).
2.  Navigieren Sie in den `frontend`-Ordner und starten Sie den Angular Development Server:
```
cd frontend
npm start
```

Dieser Befehl startet die Frontend-Anwendung und öffnet automatisch `http://localhost:4200` in Ihrem Browser.

Hinweis zur API-Verbindung: Der npm start-Befehl nutzt automatisch die proxy.conf.json-Datei. Diese leitet alle API-Anfragen (z.B. an /api/v1) vom Frontend-Port 4200 an das Backend auf http://localhost:8080 weiter. Das Funktionieren der Frontend-Backend-Kommunikation wird über diesen Proxy-Mechanismus sichergestellt.

---

## 4. Nutzung der Anwendung (Erste Schritte)

Öffnen Sie `http://localhost:4200` im Browser.

Sie werden zur Login-Seite weitergeleitet.

Sie können sich nun als neuer Benutzer registrieren oder einen der folgenden Beispieldatensätze verwenden, um sich anzumelden:

| Benutzername | Passwort |
| :--- | :--- |
| Super-admin-666 | password357 | 
| OG-Developer | password420 |

Nach dem Login werden Sie zum Dashboard weitergeleitet und die Navigationselemente werden basierend auf Ihren Berechtigungen dynamisch geladen.

---

## 5. Tests

### 5.1 Backend-Tests (JUnit & Mockito)

Sie können die Tests über die IntelliJ IDEA Maven Sidebar oder per Terminal ausführen.

**Option A: Mit IntelliJ IDEA (Empfohlen)**

1.  Öffnen Sie die Maven-Sidebar (normalerweise rechts im Fenster oder über `View` -> `Tool Windows` -> `Maven`).
2.  Erweitern Sie `sundenbock-backend` -> `Lifecycle`.
3.  Doppelklicken Sie auf `test`, um alle Backend-Tests auszuführen .

**Option B: Mit Maven-Terminal**

Führen Sie im Terminal folgende Befehle aus:
```
cd backend
mvn test
```
