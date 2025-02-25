-- Tabelle für die Planeten (muss zuerst erstellt werden)
CREATE TABLE IF NOT EXISTS Planet
(
    PlanetID VARCHAR(100) PRIMARY KEY,
    Height INT NOT NULL,
    Width INT NOT NULL
    );

-- Tabelle für die Roboter, wobei der Name als primärer Bezeichner (ID) genutzt wird
CREATE TABLE IF NOT EXISTS robot
(
    robotID VARCHAR(100) PRIMARY KEY, -- Hier verwenden wir VARCHAR für die Robot-ID
    Status VARCHAR(50) NOT NULL
    );

-- Tabelle für die Positionen der Roboter auf den Planeten (muss als letztes erstellt werden)
CREATE TABLE IF NOT EXISTS Position (
    PositionID SERIAL PRIMARY KEY,
    PlanetID VARCHAR(100) REFERENCES Planet(PlanetID) ON DELETE CASCADE,
    robotID VARCHAR(100) REFERENCES robot(robotID) ON DELETE SET NULL,
    X INT NOT NULL,
    Y INT NOT NULL,
    ground VARCHAR(50) NOT NULL
);
