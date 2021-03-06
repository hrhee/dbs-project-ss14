INSERT INTO `FU_Buli`.`Liga`
SELECT Liga.Liga_Nr, Liga.Verband
FROM  `bundesliga`.`Liga`;

INSERT INTO `FU_Buli`.`Spieler`
SELECT Spieler.Spieler_ID, Spieler.Spieler_Name,
       Spieler.Land
FROM `bundesliga`.`Spieler`;

INSERT INTO `FU_Buli`.`Verein`
SELECT Verein.V_ID, Verein.Name
FROM `bundesliga`.`Verein`;

INSERT INTO `FU_Buli`.`Spiel`
SELECT Spiel.Spiel_ID, Spiel.Spieltag, Spiel.Datum, 
       Spiel.Uhrzeit, Spiel.Tore_Heim,
       Spiel.Tore_Gast, Spiel.Heim, Spiel.Gast
FROM `bundesliga`.`Spiel`;

INSERT INTO `FU_Buli`.`Spielt_in`
SELECT Liga.Liga_Nr, Verein.V_ID, 2013
FROM `bundesliga`.`Liga`, `bundesliga`.`Verein`;

INSERT INTO `FU_Buli`.`Spielt_fuer`
SELECT Spieler.Spieler_ID, Verein.V_ID, 2013, 
       Spieler.Tore, Spieler.Trikot_Nr
FROM `bundesliga`.`Spieler`, `bundesliga`.`Verein`;

