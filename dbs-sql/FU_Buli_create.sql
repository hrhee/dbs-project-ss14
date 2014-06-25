CREATE DATABASE `FU_Buli`;

USE `FU_Buli`;

-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 24, 2014 at 09:32 PM
-- Server version: 5.5.37-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `FU_Buli`
--

-- --------------------------------------------------------

--
-- Table structure for table `Liga`
--

CREATE TABLE IF NOT EXISTS `Liga` (
  `Id` int(1) NOT NULL,
  `Name` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Spiel`
--

CREATE TABLE IF NOT EXISTS `Spiel` (
  `Id` int(11) NOT NULL,
  `Spieltag` int(11) NOT NULL,
  `Datum` date NOT NULL,
  `Uhrzeit` time NOT NULL,
  `ToreHeim` int(11) NOT NULL,
  `ToreAus` int(11) NOT NULL,
  `Heim` int(11) NOT NULL,
  `Aus` int(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Heim` (`Heim`),
  KEY `Aus` (`Aus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Spieler`
--

CREATE TABLE IF NOT EXISTS `Spieler` (
  `Id` int(11) NOT NULL,
  `Name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `Heimatland` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Spielt_fuer`
--

CREATE TABLE IF NOT EXISTS `Spielt_fuer` (
  `SId` int(11) NOT NULL,
  `VId` int(11) NOT NULL,
  `Saison` int(11) NOT NULL,
  `Tore` int(11) NOT NULL,
  `TrikotNr` int(11) NOT NULL,
  PRIMARY KEY (`SId`,`VId`),
  KEY `VereinKey` (`VId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Spielt_in`
--

CREATE TABLE IF NOT EXISTS `Spielt_in` (
  `LId` int(1) NOT NULL,
  `VId` int(11) NOT NULL,
  `Saison` int(11) NOT NULL,
  PRIMARY KEY (`LId`,`VId`),
  KEY `VerKey` (`VId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Verein`
--

CREATE TABLE IF NOT EXISTS `Verein` (
  `Id` int(11) NOT NULL,
  `Name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `Spiel`
--
ALTER TABLE `Spiel`
  ADD CONSTRAINT `AusKey` FOREIGN KEY (`Aus`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `HeimKey` FOREIGN KEY (`Heim`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `Spielt_fuer`
--
ALTER TABLE `Spielt_fuer`
  ADD CONSTRAINT `SpielerKey` FOREIGN KEY (`SId`) REFERENCES `Spieler` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `VereinKey` FOREIGN KEY (`VId`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `Spielt_in`
--
ALTER TABLE `Spielt_in`
  ADD CONSTRAINT `LigaKey` FOREIGN KEY (`LId`) REFERENCES `Liga` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `VerKey` FOREIGN KEY (`VId`) REFERENCES `Verein` (`Id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
