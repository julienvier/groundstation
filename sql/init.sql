-- Tabelle für die Planeten (muss zuerst erstellt werden)
CREATE TABLE IF NOT EXISTS Planet
(
    PlanetID
    SERIAL
    PRIMARY
    KEY,
    Name
    VARCHAR
(
    100
) NOT NULL,
    Höhe INT NOT NULL,
    Breite INT NOT NULL
    );

-- Tabelle für die Roboter (muss als zweites erstellt werden)
CREATE TABLE IF NOT EXISTS Roboter
(
    RoboterID
    SERIAL
    PRIMARY
    KEY,
    Status
    VARCHAR
(
    50
) NOT NULL
    );

-- Tabelle für die Positionen der Roboter auf den Planeten (muss als letztes erstellt werden)
CREATE TABLE IF NOT EXISTS Position
(
    PositionID
    SERIAL
    PRIMARY
    KEY,
    PlanetID
    INT
    REFERENCES
    Planet
(
    PlanetID
) ON DELETE CASCADE,
    RoboterID INT REFERENCES Roboter
(
    RoboterID
)
  ON DELETE SET NULL,
    X INT NOT NULL,
    Y INT NOT NULL,
    Beschaffenheit VARCHAR
(
    50
) NOT NULL
    );