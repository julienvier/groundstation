# Projektplan: Exoplanet Research

## 1. Phasen-Übersicht

1. **Analyse und Projektinitialisierung**  
2. **Pflichtenhefterstellung und Planung**  
3. **Entwurf (OOA/OOD)**  
4. **Implementierung**  
   - 4.1 RemoteRobot-Client  
   - 4.2 Bodenstation (inkl. Datenbank-Anbindung)  
   - 4.3 Web-Komponente  
5. **Integration und Test**  
6. **Feinschliff und Dokumentation**  
7. **Abnahme und Projektabschluss**

---

## 2. Zeitlicher Ablauf (Beispiel)

| **Phase**                                | **Aufgaben**                                                                                                                                                                        | **Zeitraum** (Beispiel)              | **Meilensteine**                                                          |
|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------|----------------------------------------------------------------------------|
| **1. Analyse und Projektinitialisierung** | - Projektumgebung vorbereiten (IDE, Git, DB etc.)<br/>- ExoPlanet-Server kennenlernen<br/>- Anforderungen grob sammeln                                                               | KW 1 (z.B. erste Woche Januar)         | Projektfreigabe, erste Konzeptidee                                        |
| **2. Pflichtenheft & Planung**           | - Detaillierte Anforderungen (Muss-/Wunsch-Kriterien)<br/>- Abgrenzung & Ziele definieren<br/>- Grober Zeitplan / Ressourcenplanung                                                  | KW 2                                  | Abgabe Pflichtenheft (z.B. 17.01.2025)                                    |
| **3. Entwurf (OOA/OOD)**                 | - Use-Case-Diagramm, grobes Aktivitätsdiagramm<br/>- Datenbank-Entwurf (ERM/RDM)<br/>- Klassendiagramme (Robot, Bodenstation etc.)                                                 | KW 3                                  | Fertiger Entwurf (UML-Diagramme, ERM liegt vor)                           |
| **4. Implementierung**                   | **4.1 RemoteRobot-Client** <br/>- TCP-Kommunikation (Text-/JSON-Protokoll)<br/>- Befehle `orbit`, `land`, `move`, `scan`, etc.<br/>- Robot-Logik (ggf. autonom/bedienergesteuert)<br/><br/>**4.2 Bodenstation**<br/>- Empfangen & Speichern der Messdaten<br/>- Positionen verwalten (Kollisionsvermeidung)<br/>- Datenbankanbindung (MySQL/PostgreSQL)<br/><br/>**4.3 Web-Komponente**<br/>- Web-Frontend (z.B. Lit/TS + Vite)<br/>- REST/HTTP-Schnittstelle oder direkter Bodenstations-API-Aufruf<br/>- Visualisierung der Positionen & Messwerte | KW 4–6                               | Teilergebnisse pro Komponente (z.B. lauffähiger Robot-Client in KW5)       |
| **5. Integration und Test**              | - Zusammenführen aller Komponenten (Robot ↔ Bodenstation ↔ DB ↔ Web)<br/>- Systemtests (mehrere Roboter, Kollisionstests)<br/>- Testprotokoll erstellen und durchführen             | KW 7–8                               | Interner Testbericht, Testprotokoll                                       |
| **6. Feinschliff und Dokumentation**     | - Code-Review, Optimierung (Performance, Fehlerhandling)<br/>- Produktdokumentation verfassen (Installationsanleitung, Benutzerhandbuch)<br/>- Ggf. Erweiterungen (JSON-Protokoll, GUI, Plugins) implementieren | KW 8–9                               | Alle Muss-Kriterien erfüllt; Entscheidung über Kann-Kriterien             |
| **7. Abnahme & Projektabschluss**        | - Abschlusspräsentation mit Live-Demo<br/>- Übergabe aller Dokumente (Pflichtenheft, Doku, Testprotokoll, Quellcode)                                                                 | KW 9–10 (Ende Feb. 2025)              | Endabnahme (z.B. 26.02.2025), Feedback und Projektbewertung               |

---

## 3. Meilensteine im Detail

1. **Projektfreigabe & Kickoff** (Ende der Analyse)  
   - Verstehen der Server-Funktionen und Protokolle  
   - Grobes Ziel und Rahmen abgesteckt

2. **Abgabe Pflichtenheft** (z.B. 17.01.2025)  
   - Muss-/Wunsch-Kriterien stehen fest  
   - Projektplan verabschiedet

3. **Fertiger Entwurf** (z.B. KW 3)  
   - UML-Diagramme (Use-Case, Klassendiagramm, ERM) liegen vor

4. **Teillieferung: Lauffähiger Robot-Client** (z.B. KW 5)  
   - Robot kann sich anmelden (orbit), landen und Befehle (move, scan etc.) ausführen

5. **Bodenstation + Datenbank** (z.B. KW 6)  
   - Daten werden erfolgreich in DB geschrieben  
   - Kollisionsvermeidung im Ansatz funktionsfähig

6. **Integration + Web-Frontend** (z.B. KW 8)  
   - Alle Systeme greifen ineinander  
   - Erste funktionierende Web-Visualisierung

7. **Abschlusspräsentation** (z.B. 26.02.2025)  
   - Live-Test mit mehreren Robotern  
   - Projekt-Dokumentation + Testprotokoll abgeschlossen

---

## 4. Ressourcen & Rollen

- **Teamgröße**: 2er- oder 3er-Gruppen  
- **Rollen** (Beispiel):  
  - **Projektleiter**: Koordiniert Termine, trägt Verantwortung für Dokumentation  
  - **Entwickler Robot-Client**: Hauptsächlich für Java-Robot-Logik zuständig  
  - **Entwickler Bodenstation/DB**: Erstellt DB-Struktur, Schnittstellen, Bodenstations-Logik  
  - **Entwickler Web-Frontend**: Kümmert sich um Layout, Frontend-Logik, Datendarstellung  
- **Tools**:  
  - Entwicklungsumgebung (IntelliJ/Eclipse + Java 17)  
  - DB-Server (z.B. MySQL, PostgreSQL)  
  - Git (Versionsverwaltung)  
  - Vite + Lit (Frontend)

---

## 5. Risiko- und Änderungsmanagement

- **Risiken**:
  - Zeitverzug bei Server-Integration (ExoPlanet)
  - Komplexität der DB-Anbindung / Koordination mehrerer Roboter
  - Fehlerhafte oder unzureichende Tests bei parallelen Robotern
- **Änderungen**:
  - Neue Funktionen (z.B. Plugin-Roboter, JSON-Protocol) werden zuerst als **Wunschkriterium** priorisiert und nur umgesetzt, wenn Muss-Kriterien erfüllt und Zeit vorhanden

---

## 6. Kontroll- und Dokumentationspunkte

- **Wöchentlicher Fortschritts-Check** (z.B. im Unterricht/Meeting):  
  - Was wurde erreicht?  
  - Gibt es Blocker oder Probleme?
- **Erfassung wichtiger Erkenntnisse** (z.B. in Projekt-Tagebuch, Jira, Trello)
- **Testprotokoll**:
  - Dokumentierung jedes Meilenstein-Tests (Erwartetes Ergebnis vs. Tatsächliches Ergebnis)

---

### Zusammenfassung

Dieser Projektplan gliedert die Umsetzung der **RemoteRobot-Clients**, der **Bodenstation** (mit DB-Anbindung) sowie der **Web-Komponente** in zeitliche und inhaltliche Phasen. Durch frühzeitiges Festlegen von **Meilensteinen**, **Rollen** und **Risikobewertungen** wird sichergestellt, dass das Projekt strukturiert umgesetzt und termingerecht abgeschlossen wird.
