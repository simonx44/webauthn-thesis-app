# Masterarbeit FIDO2/WebAuthN Prototype

Dieses Projekt wurde im Zuge der Masterarbeit erstellt und stellt einen Prototyp für die Nutzung der Web Authentication API dar.
Somit dient dieses Projekt zur Demonstration der Passkeys. Das Projekt ist in ein Backend (Java/Spring Boot) und ein Frontend (AngularJS) unterteilt.


## Frontend starten

Die Abhängigkeiten müssen installiert werden. Hierfür wird NodeJS benötigt:

### `npm install`

Nach der Installation kann über den folgenden Befehl die Anwendung gestartet werden:

### `npm run start`
Danach kann das Frontend über den folgenden Link aufgerufen werden: http://localhost:4200/ 
Bitte den Prototyp mit einem Chromimum-Browser (https://de.wikipedia.org/wiki/Chromium_(Browser)) ausführen, da hierbei der Support am besten ist.
Derzeit unterstützt der Prototyp nur nur Windows vollumfänglich.

## Backend starten
Für das Backend wird Java 17 und Maven benötigt. Auch hier bitte die Abhängigkeiten installieren:

### `mvn clean install`

Nach der Installation kann der Spring-Boot Dienst gestartet werden


## Informationen zum Prototyp

Mittels des Prototyps ist eine Anmeldung mit Passkeys möglich.
Zudem können Passkeys gelöscht werden. 
