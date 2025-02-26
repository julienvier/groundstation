-- Tabelle für die Planeten (muss zuerst erstellt werden)
CREATE TABLE IF NOT EXISTS Planet
(
    planetid VARCHAR(100) PRIMARY KEY,
    width INT NOT NULL,
    height INT NOT NULL
    
    );

-- Tabelle für die Roboter, wobei der Name als primärer Bezeichner (ID) genutzt wird
CREATE TABLE IF NOT EXISTS robot
(
    robotid VARCHAR(100) PRIMARY KEY, -- Hier verwenden wir VARCHAR für die Robot-ID
    status VARCHAR(50) NOT NULL
    );

-- Tabelle für die Positionen der Roboter auf den Planeten (muss als letztes erstellt werden)
CREATE TABLE IF NOT EXISTS Position (
    positionid SERIAL PRIMARY KEY,
    planetid VARCHAR(100) REFERENCES Planet(PlanetID) ON DELETE CASCADE,
    robotid VARCHAR(100) REFERENCES robot(robotID) ON DELETE SET NULL,
    X INT NOT NULL,
    Y INT NOT NULL,
    ground VARCHAR(50) NOT NULL
);
