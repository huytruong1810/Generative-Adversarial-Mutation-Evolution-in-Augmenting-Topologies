DROP DATABASE IF EXISTS Population;
CREATE DATABASE Population;
USE Population;

DROP TABLE IF EXISTS Accesses;
DROP TABLE IF EXISTS Species;
DROP TABLE IF EXISTS Individuals;
DROP TABLE IF EXISTS MRUs;
DROP TABLE IF EXISTS ACUs;

CREATE TABLE Accesses (
	Access_ID 			INT AUTO_INCREMENT,
	Access_Time 		TIME,
    Access_Date 		DATE,
	World_Size 			INT,
	No_Trials 			INT,
	No_Time_Steps 		INT,
	Max_Population 		INT,
	No_Episodes 		INT,
	Max_Generation 		INT,
	Latest_Score 		VARCHAR(50),
	PRIMARY KEY (Access_ID)
);

CREATE TABLE Individuals (
	Access_ID 			INT,
	Step_No 			INT,
	Score 				VARCHAR(50),
	Species_ID 			VARCHAR(50),
	Parent_Species_ID 	VARCHAR(50), 
	MRU_ID 				INT, 
	ACU_ID 				INT,
	PRIMARY KEY (Access_ID, Step_No, MRU_ID, ACU_ID)
);

CREATE TABLE Species (
	Access_ID 			INT,
	Step_No 			INT,
    Species_ID			INT,
    Score				VARCHAR(50),
    Ancestor_ID			INT,
    Repr_MRU_ID			INT,
    Repr_ACU_ID			INT,
    Population_Size		INT,
    PRIMARY KEY (Access_ID, Step_No, Species_ID)
);

CREATE TABLE MRUs (
	Access_ID 			INT,
	Step_No 			INT,
	MRU_ID 				INT, 
	ACU_ID 				INT, 
	MRU_Con_IN 			INT, 
	From_Node_IN 		INT, 
	To_Node_IN 			INT, 
	Forget_Weight 		FLOAT, 
	Input_Weight 		FLOAT, 
	Candidate_Weight 	FLOAT, 
	Output_Weight 		FLOAT,
	PRIMARY KEY (Access_ID, Step_No, MRU_ID, ACU_ID, MRU_Con_IN)
);

CREATE TABLE ACUs (
	Access_ID 			INT,
	Step_No 			INT,
	ACU_ID 				INT, 
	MRU_ID 				INT, 
	ACU_Con_IN 			INT, 
	From_Node_IN 		INT, 
	To_Node_IN 			INT, 
	Critic_Weight 		FLOAT, 
	Actor_Weight 		FLOAT,
	PRIMARY KEY (Access_ID, Step_No, ACU_ID, MRU_ID, ACU_Con_IN)
);

DROP PROCEDURE IF EXISTS getAvgScoreProgress;
DELIMITER //
CREATE PROCEDURE getAvgScoreProgress(IN accessID INT)
BEGIN
	SELECT Step_No, AVG(Score) Avg_Score
	FROM Individuals
	WHERE Score != '-Infinity' AND Access_ID = accessID
	GROUP BY Step_No;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getMaxScoreProgress;
DELIMITER //
CREATE PROCEDURE getMaxScoreProgress(IN accessID INT)
BEGIN
	SELECT Step_No, MAX(CAST(Score AS SIGNED)) Max_Score
	FROM Individuals
	WHERE Score != '-Infinity' AND Access_ID = accessID
	GROUP BY Step_No;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getNumSpeciesProgress;
DELIMITER //
CREATE PROCEDURE getNumSpeciesProgress(IN accessID INT)
BEGIN
	SELECT Step_No, COUNT(*) Num_Species
	FROM Species
	WHERE Access_ID = accessID
	GROUP BY Step_No;
END//
DELIMITER ;

SELECT * FROM Accesses;

SELECT * FROM Species;

SELECT * FROM MRUs;

SELECT * FROM ACUs;



