-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Hôte : mysql:3306
-- Généré le : lun. 23 fév. 2026 à 23:16
-- Version du serveur : 8.0.44
-- Version de PHP : 8.3.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `elearning`
--

-- --------------------------------------------------------

--
-- Structure de la table `assignments`
--

CREATE TABLE `assignments` (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  `due_date` datetime NOT NULL,
  `max_grade` decimal(5,2) NOT NULL DEFAULT '20.00',
  `created_by` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `assignments`
--

INSERT INTO `assignments` (`id`, `course_id`, `title`, `description`, `due_date`, `max_grade`, `created_by`, `created_at`) VALUES
(1001, 1, 'TP1 - API REST', 'Créer une API REST complète', '2026-03-02 21:10:44', 20.00, 2, '2026-02-23 21:10:44'),
(1002, 1, 'TP2 - Frontend React', 'Interface React', '2026-03-09 21:10:44', 20.00, 2, '2026-02-23 21:10:44'),
(1003, 1, 'Projet Final', 'Application complète', '2026-03-25 21:10:44', 20.00, 2, '2026-02-23 21:10:44'),
(1004, 2, 'TP1 - Modélisation BDD', 'Schéma BDD', '2026-02-28 21:10:44', 20.00, 3, '2026-02-23 21:10:44'),
(1005, 2, 'TP2 - Requêtes SQL', 'SQL avancé', '2026-03-07 21:10:44', 20.00, 3, '2026-02-23 21:10:44');

-- --------------------------------------------------------

--
-- Structure de la table `assignment_classes`
--

CREATE TABLE `assignment_classes` (
  `id` bigint NOT NULL,
  `assignment_id` bigint NOT NULL,
  `classe_id` bigint NOT NULL,
  `date_affectation` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `assignment_classes`
--

INSERT INTO `assignment_classes` (`id`, `assignment_id`, `classe_id`, `date_affectation`) VALUES
(1, 1001, 4, '2026-02-23 21:10:44'),
(2, 1001, 5, '2026-02-23 21:10:44'),
(3, 1002, 4, '2026-02-23 21:10:44'),
(4, 1002, 5, '2026-02-23 21:10:44'),
(5, 1003, 4, '2026-02-23 21:10:44'),
(6, 1003, 5, '2026-02-23 21:10:44'),
(7, 1004, 4, '2026-02-23 21:10:44'),
(8, 1004, 5, '2026-02-23 21:10:44'),
(9, 1005, 4, '2026-02-23 21:10:44'),
(10, 1005, 5, '2026-02-23 21:10:44');

-- --------------------------------------------------------

--
-- Structure de la table `assignment_submissions`
--

CREATE TABLE `assignment_submissions` (
  `id` bigint NOT NULL,
  `assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `submitted_at` datetime DEFAULT NULL,
  `file_url` varchar(500) DEFAULT NULL,
  `grade` decimal(5,2) DEFAULT NULL,
  `feedback` text,
  `graded_at` datetime DEFAULT NULL,
  `graded_by` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `assignment_submissions`
--

INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `submitted_at`, `file_url`, `grade`, `feedback`, `graded_at`, `graded_by`) VALUES
(1, 1001, 10, '2026-02-21 21:10:44', 'https://s3.amazonaws.com/elearning/alice_tp1.zip', 18.50, 'Excellent !', '2026-02-22 21:10:44', 2),
(2, 1001, 11, '2026-02-20 21:10:44', 'https://s3.amazonaws.com/elearning/bob_tp1.zip', 15.00, 'Bon travail', '2026-02-22 21:10:44', 2),
(3, 1001, 12, '2026-02-22 21:10:44', 'https://s3.amazonaws.com/elearning/claire_tp1.zip', NULL, NULL, NULL, NULL),
(4, 1002, 10, '2026-02-23 20:10:44', 'https://s3.amazonaws.com/elearning/alice_tp2.zip', NULL, NULL, NULL, NULL),
(5, 1002, 13, '2026-02-23 18:10:44', 'https://s3.amazonaws.com/elearning/david_tp2.zip', NULL, NULL, NULL, NULL),
(6, 1004, 10, '2026-02-19 21:10:44', 'https://s3.amazonaws.com/elearning/alice_bdd1.pdf', 16.00, 'Bonne modélisation', '2026-02-21 21:10:44', 3),
(7, 1004, 11, '2026-02-18 21:10:44', 'https://s3.amazonaws.com/elearning/bob_bdd1.pdf', 14.50, 'Correct', '2026-02-21 21:10:44', 3),
(8, 1004, 21, '2026-02-23 22:35:23', 'https://elearning-miage-adam.s3.eu-north-1.amazonaws.com/assignments/4496f0a3-c634-46ce-beb3-8d6f4f937c2c_Fiche_revision_complete_PF_Generiques.docx', NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL,
  `actor_id` bigint NOT NULL,
  `action` varchar(100) NOT NULL,
  `entity_type` varchar(50) DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `before_data` text,
  `after_data` text,
  `ip_address` varchar(45) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `audit_logs`
--

INSERT INTO `audit_logs` (`id`, `actor_id`, `action`, `entity_type`, `entity_id`, `before_data`, `after_data`, `ip_address`, `created_at`) VALUES
(1, 11, 'BULK_ENROLL_CLASS', 'COURSE', 8912585819139254765, NULL, 'Inscription de 2 étudiants de la classe M2 Droit Public', '0:0:0:0:0:0:0:1', '2026-01-01 23:11:07'),
(2, 11, 'ASSIGN_TEACHER', 'COURSE', 8912585819139254765, NULL, 'Affectation de Sophie Durand', '0:0:0:0:0:0:0:1', '2026-01-01 23:11:09'),
(3, 11, 'REMOVE_TEACHER', 'COURSE', 8912585819139254765, NULL, 'Retrait de Sophie Durand', '0:0:0:0:0:0:0:1', '2026-01-01 23:25:52'),
(4, 11, 'ASSIGN_TEACHER', 'COURSE', 8912585819139254765, NULL, 'Affectation de Karim Benzakour', '0:0:0:0:0:0:0:1', '2026-01-01 23:25:59'),
(5, 11, 'BULK_ENROLL_CLASS', 'COURSE', 8912585819139254765, NULL, 'Inscription de 3 étudiants de la classe L2 Économie Groupe 2', '0:0:0:0:0:0:0:1', '2026-01-01 23:26:05'),
(6, 11, 'BULK_ENROLL_CLASS', 'COURSE', 8912585819139254765, NULL, 'Inscription de 0 étudiants de la classe M2 Droit Public', '0:0:0:0:0:0:0:1', '2026-01-01 23:26:10'),
(7, 11, 'REMOVE_TEACHER', 'COURSE', 1, NULL, 'Retrait de Sophie Durand', '0:0:0:0:0:0:0:1', '2026-01-01 23:30:35'),
(8, 11, 'ASSIGN_TEACHER', 'COURSE', 1, NULL, 'Affectation de Sophie Durand', '0:0:0:0:0:0:0:1', '2026-01-01 23:30:38'),
(9, 11, 'BULK_ENROLL_CLASS', 'COURSE', 1, NULL, 'Inscription de 1 étudiants de la classe M2 Droit Public', '0:0:0:0:0:0:0:1', '2026-01-01 23:30:41'),
(10, 11, 'BULK_ENROLL_CLASS', 'COURSE', 1, NULL, 'Inscription de 1 étudiants de la classe test2', '0:0:0:0:0:0:0:1', '2026-01-01 23:30:45'),
(11, 11, 'BULK_UNENROLL_CLASS', 'COURSE', 1, NULL, 'Désinscription de 3 étudiants de la classe test2', '0:0:0:0:0:0:0:1', '2026-01-01 23:40:10'),
(12, 11, 'BULK_UNENROLL_CLASS', 'COURSE', 1, NULL, 'Désinscription de 1 étudiants de la classe M2 Droit Public', '0:0:0:0:0:0:0:1', '2026-01-01 23:40:13'),
(13, 11, 'BULK_ENROLL_CLASS', 'COURSE', 1, NULL, 'Inscription de 3 étudiants de la classe L2 Économie Groupe 2', '0:0:0:0:0:0:0:1', '2026-01-01 23:40:16'),
(14, 11, 'REMOVE_TEACHER', 'COURSE', 1, NULL, 'Retrait de Sophie Durand', '0:0:0:0:0:0:0:1', '2026-01-01 23:40:19'),
(15, 11, 'ASSIGN_TEACHER', 'COURSE', 1, NULL, 'Affectation de eeee lar', '0:0:0:0:0:0:0:1', '2026-01-01 23:40:21');

-- --------------------------------------------------------

--
-- Structure de la table `classes`
--

CREATE TABLE `classes` (
  `id` bigint NOT NULL,
  `parcours_id` bigint NOT NULL,
  `nom` varchar(150) NOT NULL,
  `code` varchar(50) DEFAULT NULL,
  `annee_universitaire` varchar(9) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `classes`
--

INSERT INTO `classes` (`id`, `parcours_id`, `nom`, `code`, `annee_universitaire`) VALUES
(4, 4, 'L2 Économie Groupe 2', 'L2ECO-2', '2024-2025'),
(5, 3, 'M2 Droit Public', 'M2DP-1', '2024-2025'),
(6, 1, 'test', 'test', '2000'),
(7, 2, 'test2', 'test', '2000'),
(8, 2, 'stmg', 'smtg1', '20022');

-- --------------------------------------------------------

--
-- Structure de la table `courses`
--

CREATE TABLE `courses` (
  `id` bigint NOT NULL,
  `code` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  `created_by` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `courses`
--

INSERT INTO `courses` (`id`, `code`, `title`, `description`, `created_by`, `created_at`, `updated_at`) VALUES
(1, 'DEV-SB101', 'Développement Web avec Spring Boot', 'Cours backend pour MIAGE.', 2, '2025-11-16 04:23:41', NULL),
(2, 'BDD-202', 'Bases de Données Avancées', 'Index, arbres B+, hachage, stockage.', 3, '2025-11-16 04:23:41', NULL),
(3, 'DROIT101', 'Introduction au Droit Constitutionnel', 'Cours fictif.', 2, '2025-12-09 23:57:47', NULL),
(4, 'ECO102', 'Économie Internationale', 'Cours fictif.', 3, '2025-12-09 23:57:47', NULL),
(5, 'IA301', 'Machine Learning', 'Cours fictif.', 2, '2025-12-09 23:57:47', NULL),
(260259570445762243, '1DQZDQ', 'cours mapc', 'interface', 10, '2025-11-23 16:58:01', NULL),
(4869972576840470073, '301194', 'dev', 'dev', 10, '2025-11-23 17:03:32', NULL),
(8912585819139254752, 'mapc', 'mapc 4', 'mapc 4', 10, '2025-11-23 18:00:44', NULL),
(8912585819139254769, 'BDA', 'BDA', 'BDA COURS 1', 11, '2026-01-04 00:42:49', '2026-01-28 09:27:23'),
(8912585819139254770, 'bda1', 'bda1', 'bda1', 22, '2026-02-23 21:32:06', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `course_enrollments`
--

CREATE TABLE `course_enrollments` (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `enrolled_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `course_enrollments`
--

INSERT INTO `course_enrollments` (`id`, `course_id`, `student_id`, `enrolled_at`, `status`) VALUES
(3, 2, 4, '2025-11-16 04:23:41', 'ACTIVE'),
(4, 2, 6, '2025-11-16 04:23:41', 'ACTIVE'),
(12, 1, 8, '2026-01-01 23:40:16', 'ACTIVE'),
(13, 1, 14, '2026-01-01 23:40:16', 'ACTIVE'),
(14, 1, 19, '2026-01-01 23:40:16', 'ACTIVE');

-- --------------------------------------------------------

--
-- Structure de la table `course_resource`
--

CREATE TABLE `course_resource` (
  `id` bigint NOT NULL,
  `course_id` bigint DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `s3audio_url` varchar(1000) DEFAULT NULL,
  `transcript` longtext,
  `summary` longtext,
  `keywords` longtext,
  `structure_json` longtext,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `course_resource`
--

INSERT INTO `course_resource` (`id`, `course_id`, `type`, `title`, `s3audio_url`, `transcript`, `summary`, `keywords`, `structure_json`, `created_at`) VALUES
(1, 8912585819139254769, 'AUDIO', 'test', NULL, 'ルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルル', '', NULL, NULL, '2026-01-28 09:25:50'),
(2, 8912585819139254769, 'AUDIO', 'test', NULL, 'ルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルル ルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルル ルルルル ルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルル', 'Ce texte ne contient pas de sens cohérent, il semble être une série de caractères répétitifs sans signification particulière.', NULL, NULL, '2026-01-28 09:25:53'),
(3, 8912585819139254769, 'AUDIO', 'test', NULL, 'ルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルルル', '', NULL, NULL, '2026-01-28 09:25:53');

-- --------------------------------------------------------

--
-- Structure de la table `course_resources`
--

CREATE TABLE `course_resources` (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `s3_audio_url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transcript` longtext COLLATE utf8mb4_unicode_ci,
  `summary` longtext COLLATE utf8mb4_unicode_ci,
  `keywords` longtext COLLATE utf8mb4_unicode_ci,
  `structure_json` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `course_resources`
--

INSERT INTO `course_resources` (`id`, `course_id`, `type`, `title`, `description`, `url`, `file_name`, `content_type`, `s3_audio_url`, `transcript`, `summary`, `keywords`, `structure_json`, `created_at`, `updated_at`) VALUES
(1, 8912585819139254769, 'FILE', 'ee', 'ee', 'https://elearning-miage-adam.s3.eu-north-1.amazonaws.com/uploads/other/c9a54059-14ad-4a70-89b2-e524f2e57d0c_elearning (6) (3).sql', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-02-21 02:41:08', '2026-02-21 02:41:08'),
(2, 8912585819139254770, 'FILE', 'message (8).txt', NULL, 'https://elearning-miage-adam.s3.eu-north-1.amazonaws.com/uploads/2b579738-15f3-4da0-93dc-ce1b32cbf3ed_message (8).txt', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-02-23 21:32:06', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `course_resources_backup`
--

CREATE TABLE `course_resources_backup` (
  `id` bigint NOT NULL DEFAULT '0',
  `course_id` bigint NOT NULL,
  `type` varchar(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  `url` varchar(500) DEFAULT NULL,
  `s3_audio_url` varchar(1000) DEFAULT NULL,
  `transcript` longtext,
  `summary` longtext,
  `keywords` longtext,
  `structure_json` longtext,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `file_name` varchar(100) DEFAULT NULL,
  `content_type` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `course_sessions`
--

CREATE TABLE `course_sessions` (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `session_date` datetime NOT NULL,
  `description` text,
  `recording_url` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `course_sessions`
--

INSERT INTO `course_sessions` (`id`, `course_id`, `title`, `session_date`, `description`, `recording_url`, `created_at`) VALUES
(1, 1, 'Spring Boot - Chapitre 1', '2025-03-01 10:00:00', 'Première séance.', 'https://example.com/audio1.mp3', '2025-11-16 04:23:41'),
(2, 2, 'Stockage & Indexation', '2025-03-02 14:00:00', 'Séance sur indexation.', 'https://example.com/audio2.mp3', '2025-11-16 04:23:41');

-- --------------------------------------------------------

--
-- Structure de la table `course_teachers`
--

CREATE TABLE `course_teachers` (
  `course_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `course_teachers`
--

INSERT INTO `course_teachers` (`course_id`, `teacher_id`) VALUES
(2, 3),
(1, 10);

-- --------------------------------------------------------

--
-- Structure de la table `cours_classes`
--

CREATE TABLE `cours_classes` (
  `cours_id` bigint NOT NULL,
  `classe_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `cours_classes`
--

INSERT INTO `cours_classes` (`cours_id`, `classe_id`) VALUES
(4, 4),
(8912585819139254752, 4),
(3, 5);

-- --------------------------------------------------------

--
-- Structure de la table `cours_filieres`
--

CREATE TABLE `cours_filieres` (
  `cours_id` bigint NOT NULL,
  `filiere_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `cours_filieres`
--

INSERT INTO `cours_filieres` (`cours_id`, `filiere_id`) VALUES
(1, 1),
(2, 1),
(260259570445762243, 1);

-- --------------------------------------------------------

--
-- Structure de la table `filieres`
--

CREATE TABLE `filieres` (
  `id` bigint NOT NULL,
  `nom` varchar(150) NOT NULL,
  `description` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `filieres`
--

INSERT INTO `filieres` (`id`, `nom`, `description`) VALUES
(1, 'Informatiqueeee', 'Filière informatique — développement, systèmes, IA.eeeee'),
(2, 'Droit', 'Filière droit public, privé, pénal.'),
(3, 'Économie et Gestion', 'Filière économie, gestion et management.'),
(4, 'Sciences Sociales', 'Filière sociologie, psychologie, sciences humaines.'),
(7, 'MIAGEEE', 'MIAGEEE');

-- --------------------------------------------------------

--
-- Structure de la table `inactivity_alerts`
--

CREATE TABLE `inactivity_alerts` (
  `id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `days_inactive` int NOT NULL,
  `last_activity_at` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'OPEN',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `closed_at` datetime DEFAULT NULL,
  `handled_by` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `inactivity_alerts`
--

INSERT INTO `inactivity_alerts` (`id`, `student_id`, `course_id`, `days_inactive`, `last_activity_at`, `status`, `created_at`, `closed_at`, `handled_by`) VALUES
(1, 6, 2, 14, '2025-02-10 15:00:00', 'OPEN', '2025-11-16 04:23:42', NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `inscriptions_classes`
--

CREATE TABLE `inscriptions_classes` (
  `id` bigint NOT NULL,
  `classe_id` bigint NOT NULL,
  `etudiant_id` bigint NOT NULL,
  `date_inscription` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `statut` varchar(20) NOT NULL DEFAULT 'ACTIF'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `inscriptions_classes`
--

INSERT INTO `inscriptions_classes` (`id`, `classe_id`, `etudiant_id`, `date_inscription`, `statut`) VALUES
(23, 7, 4, '2025-12-28 23:14:47', 'ACTIF'),
(24, 7, 5, '2025-12-28 23:15:18', 'ACTIF'),
(25, 7, 6, '2025-12-28 23:15:18', 'ACTIF'),
(26, 5, 4, '2025-12-28 23:16:09', 'ACTIF'),
(27, 6, 18, '2025-12-28 23:32:04', 'ACTIF'),
(28, 4, 4, '2025-12-29 17:28:16', 'ACTIF'),
(30, 4, 10, '2026-02-23 21:10:44', 'ACTIVE'),
(31, 4, 11, '2026-02-23 21:10:44', 'ACTIVE'),
(33, 5, 13, '2026-02-23 21:10:44', 'ACTIVE'),
(34, 5, 14, '2026-02-23 21:10:44', 'ACTIVE'),
(35, 5, 15, '2026-02-23 21:10:44', 'ACTIVE'),
(36, 4, 21, '2026-02-23 21:28:44', 'ACTIF');

-- --------------------------------------------------------

--
-- Structure de la table `messages`
--

CREATE TABLE `messages` (
  `id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `recipient_id` bigint NOT NULL,
  `course_id` bigint DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `sent_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `messages`
--

INSERT INTO `messages` (`id`, `sender_id`, `recipient_id`, `course_id`, `subject`, `content`, `sent_at`, `is_read`) VALUES
(1, 4, 2, 1, 'Question TP', 'Madame, j’ai une question concernant le TP.', '2025-11-16 04:23:41', 0),
(2, 2, 10, 1, 'Réponse TP', 'Bonjour Adam, voici la réponse…', '2025-11-16 04:23:41', 0),
(3, 11, 10, 1, 'test', 'test', '2025-12-23 01:44:12', 0),
(4, 11, 17, 1, 'test', 'test', '2025-12-23 01:45:23', 1),
(5, 17, 11, 2, 'test', 'arracvhe toi frérot', '2025-12-23 01:46:25', 1),
(6, 11, 3, 1, 'éphémère ; les désserts à l\'assiette de Yann Couvreure', 'eeee', '2025-12-24 01:09:41', 0),
(7, 11, 2, 1, 'test', 'test', '2025-12-24 01:11:42', 0),
(8, 11, 19, NULL, 'test', 'ça va frérot ? ', '2025-12-30 23:41:44', 1);

-- --------------------------------------------------------

--
-- Structure de la table `niveaux_etudes`
--

CREATE TABLE `niveaux_etudes` (
  `id` bigint NOT NULL,
  `libelle` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `niveaux_etudes`
--

INSERT INTO `niveaux_etudes` (`id`, `libelle`) VALUES
(3, 'Doctorat'),
(1, 'Licence'),
(2, 'Master');

-- --------------------------------------------------------

--
-- Structure de la table `notes_cours`
--

CREATE TABLE `notes_cours` (
  `id` bigint NOT NULL,
  `cours_id` bigint NOT NULL,
  `etudiant_id` bigint NOT NULL,
  `note_finale` decimal(5,2) DEFAULT NULL,
  `note_max` decimal(5,2) NOT NULL DEFAULT '20.00',
  `mention` varchar(5) DEFAULT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'PROVISOIRE',
  `date_calcul` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `notes_cours`
--

INSERT INTO `notes_cours` (`id`, `cours_id`, `etudiant_id`, `note_finale`, `note_max`, `mention`, `statut`, `date_calcul`, `created_at`) VALUES
(9, 1, 4, NULL, 20.00, NULL, 'PROVISOIRE', NULL, '2026-02-23 19:19:52'),
(10, 1, 5, NULL, 20.00, NULL, 'PROVISOIRE', NULL, '2026-02-23 19:19:52'),
(14, 1, 6, NULL, 20.00, NULL, 'PROVISOIRE', NULL, '2026-02-23 19:19:52'),
(15, 1, 8, NULL, 20.00, NULL, 'PROVISOIRE', NULL, '2026-02-23 19:19:52'),
(17, 1, 19, NULL, 20.00, NULL, 'PROVISOIRE', NULL, '2026-02-23 19:19:52'),
(21, 1, 10, 17.25, 20.00, 'TB', 'VALIDE', '2026-02-23 21:10:44', '2026-02-23 21:10:44'),
(22, 2, 10, 16.00, 20.00, 'TB', 'VALIDE', '2026-02-23 21:10:44', '2026-02-23 21:10:44'),
(23, 1, 11, 14.50, 20.00, 'B', 'VALIDE', '2026-02-23 21:10:44', '2026-02-23 21:10:44'),
(24, 2, 11, 14.50, 20.00, 'B', 'VALIDE', '2026-02-23 21:10:44', '2026-02-23 21:10:44'),
(25, 1, 12, 11.75, 20.00, 'AB', 'PROVISOIRE', '2026-02-23 21:10:44', '2026-02-23 21:10:44'),
(26, 1, 13, 9.50, 20.00, NULL, 'PROVISOIRE', '2026-02-23 21:10:44', '2026-02-23 21:10:44'),
(27, 1, 14, 15.50, 20.00, 'B', 'VALIDE', '2026-02-23 21:10:44', '2026-02-23 21:10:44');

-- --------------------------------------------------------

--
-- Structure de la table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `related_course_id` bigint DEFAULT NULL,
  `related_alert_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `type`, `title`, `content`, `is_read`, `created_at`, `related_course_id`, `related_alert_id`) VALUES
(2, 6, 'INACTIVITY_ALERT', 'Alerte : inactivité détectée', 'Vous êtes inactif depuis 14 jours.', 0, '2025-11-16 04:23:42', 2, NULL),
(3, 10, 'GRADE', 'Nouvelle note', 'TP1: 18.5/20', 0, '2026-02-22 21:10:44', NULL, NULL),
(4, 10, 'ASSIGNMENT', 'Nouveau devoir', 'TP2 Frontend React', 1, '2026-02-20 21:10:44', NULL, NULL),
(5, 10, 'DEADLINE', 'Date limite proche', 'Projet Final dans 7j', 0, '2026-02-23 21:10:44', NULL, NULL),
(6, 11, 'GRADE', 'Nouvelle note', 'TP1: 15/20', 0, '2026-02-22 21:10:44', NULL, NULL),
(7, 11, 'QCM', 'Nouveau QCM', 'REST API disponible', 1, '2026-02-21 21:10:44', NULL, NULL),
(8, 13, 'ASSIGNMENT', 'Nouveau devoir', 'TP2 Frontend', 0, '2026-02-20 21:10:44', NULL, NULL),
(9, 13, 'DEADLINE', 'Retard', 'TP1 non rendu', 0, '2026-02-22 21:10:44', NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `parcours`
--

CREATE TABLE `parcours` (
  `id` bigint NOT NULL,
  `filiere_id` bigint NOT NULL,
  `niveau_id` bigint NOT NULL,
  `nom` varchar(150) NOT NULL,
  `description` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `parcours`
--

INSERT INTO `parcours` (`id`, `filiere_id`, `niveau_id`, `nom`, `description`) VALUES
(1, 1, 2, 'MIAGEeeee', 'Master en Méthodes Informatiques Appliquées à la Gestion des Entrepriseeees.'),
(2, 1, 1, 'Licence Informatique', 'Licence d’informatique — parcours général.'),
(3, 2, 2, 'Droit Public', 'Master de Droit Public.'),
(4, 3, 1, 'Licence Économie et Gestion', 'Licence d’économie et gestion — parcours analyse économique.'),
(5, 7, 2, 'Parcours IA', 'cccccc');

-- --------------------------------------------------------

--
-- Structure de la table `qcm`
--

CREATE TABLE `qcm` (
  `id` bigint NOT NULL,
  `cours_id` bigint NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text,
  `publie` tinyint(1) NOT NULL DEFAULT '0',
  `limite_temps_minutes` int DEFAULT NULL,
  `tentatives_max` int DEFAULT NULL,
  `cree_par` bigint NOT NULL,
  `date_creation` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date_mise_a_jour` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `qcm`
--

INSERT INTO `qcm` (`id`, `cours_id`, `titre`, `description`, `publie`, `limite_temps_minutes`, `tentatives_max`, `cree_par`, `date_creation`, `date_mise_a_jour`) VALUES
(1, 1, 'QCM – Programmation Java – Chapitre 1', 'Questions sur les bases du langage Java.', 1, NULL, NULL, 3, '2025-12-09 23:58:17', NULL),
(2, 3, 'QCM – Droit Constitutionnel – Chapitre 2', 'Contrôle de constitutionnalité.', 1, NULL, NULL, 6, '2025-12-09 23:58:17', NULL),
(3, 1, 'QCM – Programmation Java – Chapitre 1', 'Questions sur les bases du langage Java.', 1, NULL, NULL, 3, '2025-12-09 23:58:25', NULL),
(4, 3, 'QCM – Droit Constitutionnel – Chapitre 2', 'Contrôle de constitutionnalité.', 1, NULL, NULL, 6, '2025-12-09 23:58:25', NULL),
(8, 8912585819139254769, 'test', 'test qcm', 1, 11, 1, 11, '2026-02-21 14:56:41', '2026-02-21 15:13:02'),
(9, 8912585819139254769, '2ème qcm BDA2', 'qcm bda2', 0, 20, 1, 11, '2026-02-21 15:13:37', '2026-02-22 00:22:19'),
(10, 8912585819139254769, 'bda qcm10', 'bda qcm10', 1, 16, 1, 11, '2026-02-23 00:48:55', '2026-02-23 00:54:15'),
(12, 8912585819139254770, 'bda qcm1', 'bdaa', 1, 16, 1, 22, '2026-02-23 21:34:13', '2026-02-23 21:43:24'),
(13, 8912585819139254770, 'bda qcm10', 'dddd', 1, 14, 2, 22, '2026-02-23 22:04:40', NULL),
(14, 8912585819139254770, 'bda qcm10', 'edee', 0, 14, 2, 22, '2026-02-23 22:11:06', NULL),
(15, 8912585819139254770, 'ddd', 'ddd', 1, 15, 2, 22, '2026-02-23 22:13:35', NULL),
(16, 8912585819139254770, 'dddddd', 'dddd', 1, 9, 3, 22, '2026-02-23 22:17:06', NULL),
(17, 8912585819139254770, 'aaaaaa', 'aaaaaaa', 1, 16, 3, 22, '2026-02-23 22:31:39', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `qcms`
--

CREATE TABLE `qcms` (
  `id` bigint NOT NULL,
  `cours_id` bigint NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text,
  `duree_minutes` int DEFAULT NULL,
  `tentatives_max` int DEFAULT NULL,
  `publie` tinyint(1) NOT NULL DEFAULT '0',
  `created_by` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `qcms`
--

INSERT INTO `qcms` (`id`, `cours_id`, `titre`, `description`, `duree_minutes`, `tentatives_max`, `publie`, `created_by`, `created_at`) VALUES
(2001, 1, 'QCM 1 - Spring Boot', 'Spring Boot basics', 30, 2, 1, 2, '2026-02-23 21:10:44'),
(2002, 1, 'QCM 2 - REST API', 'REST API', 45, 3, 1, 2, '2026-02-23 21:10:44'),
(2003, 2, 'QCM SQL Avancé', 'SQL complexe', 40, 2, 1, 3, '2026-02-23 21:10:44');

-- --------------------------------------------------------

--
-- Structure de la table `qcm_classes`
--

CREATE TABLE `qcm_classes` (
  `id` bigint NOT NULL,
  `qcm_id` bigint NOT NULL,
  `classe_id` bigint NOT NULL,
  `date_affectation` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `qcm_options`
--

CREATE TABLE `qcm_options` (
  `id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `texte` text,
  `texte_option` text,
  `est_correcte` tinyint(1) NOT NULL DEFAULT '0',
  `ordre` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `qcm_options`
--

INSERT INTO `qcm_options` (`id`, `question_id`, `texte`, `texte_option`, `est_correcte`, `ordre`) VALUES
(29, 2110, 'aaaaaaa', 'aaaaaaa', 0, 0),
(30, 2110, 'a', 'a', 0, 0),
(31, 2110, 'aaaaaaaaaaaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaaaaa', 1, 0),
(32, 2110, 'aaaaa', 'aaaaa', 0, 0);

--
-- Déclencheurs `qcm_options`
--
DELIMITER $$
CREATE TRIGGER `sync_qcm_option_fields` BEFORE INSERT ON `qcm_options` FOR EACH ROW BEGIN
    -- Synchroniser texte_option → texte
    IF NEW.texte_option IS NOT NULL AND (NEW.texte IS NULL OR NEW.texte = '') THEN
        SET NEW.texte = NEW.texte_option;
    END IF;
    
    -- Synchroniser texte → texte_option
    IF NEW.texte IS NOT NULL AND (NEW.texte_option IS NULL OR NEW.texte_option = '') THEN
        SET NEW.texte_option = NEW.texte;
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `qcm_questions`
--

CREATE TABLE `qcm_questions` (
  `id` bigint NOT NULL,
  `qcm_id` bigint NOT NULL,
  `texte` text,
  `texte_question` text,
  `type` varchar(50) DEFAULT NULL,
  `type_question` varchar(50) DEFAULT NULL,
  `points` decimal(5,2) NOT NULL DEFAULT '1.00',
  `ordre` int DEFAULT NULL,
  `position` int DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `qcm_questions`
--

INSERT INTO `qcm_questions` (`id`, `qcm_id`, `texte`, `texte_question`, `type`, `type_question`, `points`, `ordre`, `position`) VALUES
(2110, 17, 'aaaaaaaaaa', 'aaaaaaaaaa', 'CHOIX_SIMPLE', 'CHOIX_SIMPLE', 1.00, 1, 1);

--
-- Déclencheurs `qcm_questions`
--
DELIMITER $$
CREATE TRIGGER `sync_qcm_question_fields` BEFORE INSERT ON `qcm_questions` FOR EACH ROW BEGIN
    -- Synchroniser texte_question ↔ texte
    IF NEW.texte_question IS NOT NULL AND (NEW.texte IS NULL OR NEW.texte = '') THEN
        SET NEW.texte = NEW.texte_question;
    END IF;
    
    IF NEW.texte IS NOT NULL AND (NEW.texte_question IS NULL OR NEW.texte_question = '') THEN
        SET NEW.texte_question = NEW.texte;
    END IF;
    
    -- Synchroniser type_question ↔ type
    IF NEW.type_question IS NOT NULL AND (NEW.type IS NULL OR NEW.type = '') THEN
        SET NEW.type = NEW.type_question;
    END IF;
    
    IF NEW.type IS NOT NULL AND (NEW.type_question IS NULL OR NEW.type_question = '') THEN
        SET NEW.type_question = NEW.type;
    END IF;
    
    -- Synchroniser position ↔ ordre
    IF NEW.position IS NOT NULL AND (NEW.ordre IS NULL OR NEW.ordre = 0) THEN
        SET NEW.ordre = NEW.position;
    END IF;
    
    IF NEW.ordre IS NOT NULL AND (NEW.position IS NULL OR NEW.position = 0) THEN
        SET NEW.position = NEW.ordre;
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `sync_qcm_question_text` BEFORE INSERT ON `qcm_questions` FOR EACH ROW BEGIN
    -- Si texte_question est rempli mais pas texte, copier
    IF NEW.texte_question IS NOT NULL AND (NEW.texte IS NULL OR NEW.texte = '') THEN
        SET NEW.texte = NEW.texte_question;
    END IF;
    
    -- Si texte est rempli mais pas texte_question, copier
    IF NEW.texte IS NOT NULL AND (NEW.texte_question IS NULL OR NEW.texte_question = '') THEN
        SET NEW.texte_question = NEW.texte;
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `qcm_reponses`
--

CREATE TABLE `qcm_reponses` (
  `id` bigint NOT NULL,
  `tentative_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `option_id` bigint DEFAULT NULL,
  `reponse_texte` text,
  `est_correcte` tinyint(1) DEFAULT NULL,
  `points_obtenus` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `qcm_tentatives`
--

CREATE TABLE `qcm_tentatives` (
  `id` bigint NOT NULL,
  `qcm_id` bigint NOT NULL,
  `etudiant_id` bigint NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime DEFAULT NULL,
  `score` decimal(5,2) DEFAULT NULL,
  `score_max` decimal(5,2) DEFAULT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'EN_COURS'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `roles`
--

CREATE TABLE `roles` (
  `id` bigint NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `roles`
--

INSERT INTO `roles` (`id`, `name`) VALUES
(1, 'ROLE_ADMIN'),
(3, 'ROLE_STUDENT'),
(2, 'ROLE_TEACHER');

-- --------------------------------------------------------

--
-- Structure de la table `session_transcriptions`
--

CREATE TABLE `session_transcriptions` (
  `id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  `audio_url` varchar(500) DEFAULT NULL,
  `transcript_url` varchar(500) DEFAULT NULL,
  `transcript_text` longtext,
  `summary_text` longtext,
  `keywords` text,
  `storage_provider` varchar(100) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `session_transcriptions`
--

INSERT INTO `session_transcriptions` (`id`, `session_id`, `audio_url`, `transcript_url`, `transcript_text`, `summary_text`, `keywords`, `storage_provider`, `created_at`) VALUES
(1, 1, 'https://example.com/audio1.mp3', 'https://example.com/transcript1.txt', 'Bienvenue au cours Spring Boot...', 'Résumé : introduction à Spring Boot, création de projets, structure.', 'spring boot, java, api rest', 'LOCAL', '2025-11-16 04:23:41'),
(2, 2, 'https://example.com/audio2.mp3', 'https://example.com/transcript2.txt', 'Aujourd’hui nous voyons la notion de page mémoire...', 'Résumé : stockage, indexation, arbres B+.', 'b-tree, index, pages, stockage', 'LOCAL', '2025-11-16 04:23:41');

-- --------------------------------------------------------

--
-- Structure de la table `student_activity`
--

CREATE TABLE `student_activity` (
  `id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `course_id` bigint DEFAULT NULL,
  `activity_type` varchar(50) NOT NULL,
  `activity_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `metadata` json DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `student_activity`
--

INSERT INTO `student_activity` (`id`, `student_id`, `course_id`, `activity_type`, `activity_time`, `metadata`) VALUES
(1, 4, 1, 'LOGIN', '2025-11-16 04:23:42', NULL),
(2, 4, 1, 'VIEW_COURSE', '2025-11-16 04:23:42', '{\"page\": \"dashboard\"}'),
(3, 5, 1, 'SUBMIT_ASSIGNMENT', '2025-11-16 04:23:42', '{\"assignment\": 1}'),
(4, 6, 2, 'VIEW_COURSE', '2025-02-10 15:00:00', '{\"chapter\": \"index\"}');

-- --------------------------------------------------------

--
-- Structure de la table `teacher_classes`
--

CREATE TABLE `teacher_classes` (
  `id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `classe_id` bigint NOT NULL,
  `date_affectation` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `teacher_classes`
--

INSERT INTO `teacher_classes` (`id`, `teacher_id`, `classe_id`, `date_affectation`) VALUES
(4, 11, 5, '2026-02-23 00:48:05'),
(6, 2, 5, '2026-02-23 21:10:44'),
(8, 3, 5, '2026-02-23 21:10:44'),
(9, 22, 4, '2026-02-23 21:31:23');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` bigint NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `phone` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `email`, `password`, `first_name`, `last_name`, `created_at`, `phone`) VALUES
(1, 'admin@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Admin', 'System', '2026-02-23 21:10:44', '+33612345678'),
(2, 'prof.martin@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Jean', 'Martin', '2026-02-23 21:10:44', '+33623456789'),
(3, 'prof.durand@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Marie', 'Durand', '2026-02-23 21:10:44', '+33634567890'),
(4, 'prof.bernard@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Pierre', 'Bernard', '2026-02-23 21:10:44', '+33645678901'),
(5, 'student2@miage.fr', '$2a$10$5kzB1qH8jZmNpQvT7wX9YeVfBq8pZmNpQvT7wX9YeVfBq8pZmNpQ', 'Lina', 'Moreau', '2025-11-16 04:23:40', NULL),
(6, 'student3@miage.fr', '$2a$10$dXJkT3NaL5pZmNwX9YeVfGqH8jZmNpQvT7wX9YeVfBq8pZmNpQvT', 'Rayan', 'Fischer', '2025-11-16 04:23:40', NULL),
(8, 'ericrocot123@gmail.com', '$2a$10$C4NBRQ7SgZrYBXS/n5N5wuzj9s33dzTrlJx15HWdvxybcD9fLUqn2', 'ssss', 'sssss', '2025-11-23 02:42:13', NULL),
(10, 'alice.dubois@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Alice', 'Dubois', '2026-02-23 21:10:44', '+33656789012'),
(11, 'bob.leroy@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Bob', 'Leroy', '2026-02-23 21:10:44', '+33667890123'),
(12, 'claire.petit@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Claire', 'Petit', '2026-02-23 21:10:44', '+33678901234'),
(13, 'david.roux@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'David', 'Roux', '2026-02-23 21:10:44', '+33689012345'),
(14, 'emma.moreau@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Emma', 'Moreau', '2026-02-23 21:10:44', '+33690123456'),
(15, 'felix.simon@elearning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', 'Félix', 'Simon', '2026-02-23 21:10:44', '+33601234567'),
(16, 'stucklan@gmail.com', '$2a$10$W/pJ586zVY0CFQ5MKbzwieOPiFDnN2cQ23GeKG6BZFfD.vKW5PhR2', 'asaad', 'lar', '2025-12-22 19:36:09', NULL),
(17, 'admin@admin.com', '$2a$10$ykII6scx1AiLDNuSRbGlEeNhA4WPKe1/5dk1zWL2CejuI2VgqL0Bm', 'admin', 'admin', '2025-12-22 19:37:04', NULL),
(18, 'test@email.com', '$2a$10$PDkGRYCi.J.KB38MXHXEquDoxNb71QmTU0cU66wJcpskyO7NpE4wK', 'test', 'test', '2025-12-23 18:21:40', NULL),
(19, 'ssssss@gmail.com', 'ssssss', 'mk', 'ad', '2025-12-29 17:27:32', NULL),
(20, 'test2@gmail.com', '$2a$10$BprYQwRffgF4RYxMFjYoQeZf/jY.N3cGhyQTLk9TKWEyOg2p0Ur8e', 'test 2', 'test 2', '2025-12-30 23:45:08', NULL),
(21, 'wwww@gmail.com', '$2a$10$/fC6Bm3IIuSMLx1MkdlbBuAarAz5kLCXKqLd8uj3SRKoPtLsWa1h.', 'Corleone', 'corleone_75667', '2026-02-23 00:51:55', NULL),
(22, 'fffff@gmail.com', '$2a$10$bOJYgQstZlDuEfKiF1TZdep6vxnIk84wD1oWXjtWOcspnbn6kKGx2', 'farid', 'ffff', '2026-02-23 21:30:23', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `user_roles`
--

CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `user_roles`
--

INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1),
(17, 1),
(10, 2),
(11, 2),
(12, 2),
(13, 2),
(14, 2),
(15, 2),
(22, 2),
(2, 3),
(3, 3),
(4, 3),
(5, 3),
(6, 3),
(8, 3),
(16, 3),
(18, 3),
(19, 3),
(20, 3),
(21, 3);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `assignments`
--
ALTER TABLE `assignments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_assign_course` (`course_id`),
  ADD KEY `fk_assign_created_by` (`created_by`);

--
-- Index pour la table `assignment_classes`
--
ALTER TABLE `assignment_classes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_assignment_classe` (`assignment_id`,`classe_id`),
  ADD KEY `classe_id` (`classe_id`);

--
-- Index pour la table `assignment_submissions`
--
ALTER TABLE `assignment_submissions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_sub_assign` (`assignment_id`),
  ADD KEY `fk_sub_student` (`student_id`),
  ADD KEY `fk_sub_graded_by` (`graded_by`);

--
-- Index pour la table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_actor` (`actor_id`),
  ADD KEY `idx_entity` (`entity_type`,`entity_id`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- Index pour la table `classes`
--
ALTER TABLE `classes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_classes_parcours` (`parcours_id`);

--
-- Index pour la table `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_courses_created_by` (`created_by`);

--
-- Index pour la table `course_enrollments`
--
ALTER TABLE `course_enrollments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_enroll_course` (`course_id`),
  ADD KEY `fk_enroll_student` (`student_id`),
  ADD KEY `idx_course_enrollment_course` (`course_id`),
  ADD KEY `idx_course_enrollment_student` (`student_id`);

--
-- Index pour la table `course_resource`
--
ALTER TABLE `course_resource`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `course_resources`
--
ALTER TABLE `course_resources`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_course_id` (`course_id`),
  ADD KEY `idx_type` (`type`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- Index pour la table `course_sessions`
--
ALTER TABLE `course_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_sessions_course` (`course_id`);

--
-- Index pour la table `course_teachers`
--
ALTER TABLE `course_teachers`
  ADD PRIMARY KEY (`course_id`,`teacher_id`),
  ADD KEY `fk_course_teachers_teacher` (`teacher_id`);

--
-- Index pour la table `cours_classes`
--
ALTER TABLE `cours_classes`
  ADD PRIMARY KEY (`cours_id`,`classe_id`),
  ADD KEY `fk_cc_classe` (`classe_id`);

--
-- Index pour la table `cours_filieres`
--
ALTER TABLE `cours_filieres`
  ADD PRIMARY KEY (`cours_id`,`filiere_id`),
  ADD KEY `fk_cf_filiere` (`filiere_id`);

--
-- Index pour la table `filieres`
--
ALTER TABLE `filieres`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_nom_filiere` (`nom`);

--
-- Index pour la table `inactivity_alerts`
--
ALTER TABLE `inactivity_alerts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_alert_student` (`student_id`),
  ADD KEY `fk_alert_course` (`course_id`),
  ADD KEY `fk_alert_handled_by` (`handled_by`);

--
-- Index pour la table `inscriptions_classes`
--
ALTER TABLE `inscriptions_classes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_inscription_classe` (`classe_id`),
  ADD KEY `fk_inscription_etudiant` (`etudiant_id`);

--
-- Index pour la table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_msg_sender` (`sender_id`),
  ADD KEY `fk_msg_recipient` (`recipient_id`),
  ADD KEY `fk_msg_course` (`course_id`);

--
-- Index pour la table `niveaux_etudes`
--
ALTER TABLE `niveaux_etudes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_libelle_niveau` (`libelle`);

--
-- Index pour la table `notes_cours`
--
ALTER TABLE `notes_cours`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_cours_etudiant` (`cours_id`,`etudiant_id`),
  ADD KEY `fk_nc_etudiant` (`etudiant_id`),
  ADD KEY `idx_notes_cours_cours` (`cours_id`),
  ADD KEY `idx_notes_cours_etudiant` (`etudiant_id`);

--
-- Index pour la table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_notif_user` (`user_id`),
  ADD KEY `fk_notif_course` (`related_course_id`),
  ADD KEY `fk_notif_alert` (`related_alert_id`);

--
-- Index pour la table `parcours`
--
ALTER TABLE `parcours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_parcours_filiere` (`filiere_id`),
  ADD KEY `fk_parcours_niveau` (`niveau_id`);

--
-- Index pour la table `qcm`
--
ALTER TABLE `qcm`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_qcm_cours` (`cours_id`),
  ADD KEY `fk_qcm_createur` (`cree_par`);

--
-- Index pour la table `qcms`
--
ALTER TABLE `qcms`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cours_id` (`cours_id`),
  ADD KEY `created_by` (`created_by`);

--
-- Index pour la table `qcm_classes`
--
ALTER TABLE `qcm_classes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `qcm_classe_unique` (`qcm_id`,`classe_id`),
  ADD KEY `classe_id` (`classe_id`),
  ADD KEY `qcm_id` (`qcm_id`);

--
-- Index pour la table `qcm_options`
--
ALTER TABLE `qcm_options`
  ADD PRIMARY KEY (`id`),
  ADD KEY `question_id` (`question_id`);

--
-- Index pour la table `qcm_questions`
--
ALTER TABLE `qcm_questions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `qcm_id` (`qcm_id`);

--
-- Index pour la table `qcm_reponses`
--
ALTER TABLE `qcm_reponses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `tentative_id` (`tentative_id`),
  ADD KEY `question_id` (`question_id`),
  ADD KEY `option_id` (`option_id`);

--
-- Index pour la table `qcm_tentatives`
--
ALTER TABLE `qcm_tentatives`
  ADD PRIMARY KEY (`id`),
  ADD KEY `qcm_id` (`qcm_id`),
  ADD KEY `etudiant_id` (`etudiant_id`);

--
-- Index pour la table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Index pour la table `session_transcriptions`
--
ALTER TABLE `session_transcriptions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_transcript_session` (`session_id`);

--
-- Index pour la table `student_activity`
--
ALTER TABLE `student_activity`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_activity_student` (`student_id`),
  ADD KEY `fk_activity_course` (`course_id`);

--
-- Index pour la table `teacher_classes`
--
ALTER TABLE `teacher_classes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_teacher_classe` (`teacher_id`,`classe_id`),
  ADD KEY `idx_teacher` (`teacher_id`),
  ADD KEY `idx_classe` (`classe_id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Index pour la table `user_roles`
--
ALTER TABLE `user_roles`
  ADD PRIMARY KEY (`user_id`,`role_id`),
  ADD KEY `fk_user_roles_role` (`role_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `assignments`
--
ALTER TABLE `assignments`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1006;

--
-- AUTO_INCREMENT pour la table `assignment_classes`
--
ALTER TABLE `assignment_classes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT pour la table `assignment_submissions`
--
ALTER TABLE `assignment_submissions`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT pour la table `classes`
--
ALTER TABLE `classes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `courses`
--
ALTER TABLE `courses`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8912585819139254771;

--
-- AUTO_INCREMENT pour la table `course_enrollments`
--
ALTER TABLE `course_enrollments`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT pour la table `course_resource`
--
ALTER TABLE `course_resource`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `course_resources`
--
ALTER TABLE `course_resources`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `course_sessions`
--
ALTER TABLE `course_sessions`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `filieres`
--
ALTER TABLE `filieres`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `inactivity_alerts`
--
ALTER TABLE `inactivity_alerts`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `inscriptions_classes`
--
ALTER TABLE `inscriptions_classes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT pour la table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `niveaux_etudes`
--
ALTER TABLE `niveaux_etudes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `notes_cours`
--
ALTER TABLE `notes_cours`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT pour la table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `parcours`
--
ALTER TABLE `parcours`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `qcm`
--
ALTER TABLE `qcm`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT pour la table `qcms`
--
ALTER TABLE `qcms`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2004;

--
-- AUTO_INCREMENT pour la table `qcm_classes`
--
ALTER TABLE `qcm_classes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `qcm_options`
--
ALTER TABLE `qcm_options`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT pour la table `qcm_questions`
--
ALTER TABLE `qcm_questions`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2111;

--
-- AUTO_INCREMENT pour la table `qcm_reponses`
--
ALTER TABLE `qcm_reponses`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `qcm_tentatives`
--
ALTER TABLE `qcm_tentatives`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `session_transcriptions`
--
ALTER TABLE `session_transcriptions`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `student_activity`
--
ALTER TABLE `student_activity`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `teacher_classes`
--
ALTER TABLE `teacher_classes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `assignments`
--
ALTER TABLE `assignments`
  ADD CONSTRAINT `fk_assign_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_assign_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT;

--
-- Contraintes pour la table `assignment_classes`
--
ALTER TABLE `assignment_classes`
  ADD CONSTRAINT `assignment_classes_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `assignment_classes_ibfk_2` FOREIGN KEY (`classe_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `assignment_submissions`
--
ALTER TABLE `assignment_submissions`
  ADD CONSTRAINT `fk_sub_assign` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_sub_graded_by` FOREIGN KEY (`graded_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_sub_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD CONSTRAINT `fk_audit_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `classes`
--
ALTER TABLE `classes`
  ADD CONSTRAINT `fk_classes_parcours` FOREIGN KEY (`parcours_id`) REFERENCES `parcours` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `courses`
--
ALTER TABLE `courses`
  ADD CONSTRAINT `fk_courses_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT;

--
-- Contraintes pour la table `course_enrollments`
--
ALTER TABLE `course_enrollments`
  ADD CONSTRAINT `fk_enroll_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_enroll_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `course_resources`
--
ALTER TABLE `course_resources`
  ADD CONSTRAINT `fk_course_resources_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `course_sessions`
--
ALTER TABLE `course_sessions`
  ADD CONSTRAINT `fk_sessions_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `course_teachers`
--
ALTER TABLE `course_teachers`
  ADD CONSTRAINT `fk_course_teachers_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_course_teachers_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `cours_classes`
--
ALTER TABLE `cours_classes`
  ADD CONSTRAINT `fk_cc_classe` FOREIGN KEY (`classe_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_cc_cours` FOREIGN KEY (`cours_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `cours_filieres`
--
ALTER TABLE `cours_filieres`
  ADD CONSTRAINT `fk_cf_cours` FOREIGN KEY (`cours_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_cf_filiere` FOREIGN KEY (`filiere_id`) REFERENCES `filieres` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `inactivity_alerts`
--
ALTER TABLE `inactivity_alerts`
  ADD CONSTRAINT `fk_alert_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_alert_handled_by` FOREIGN KEY (`handled_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_alert_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `inscriptions_classes`
--
ALTER TABLE `inscriptions_classes`
  ADD CONSTRAINT `fk_inscription_classe` FOREIGN KEY (`classe_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_inscription_etudiant` FOREIGN KEY (`etudiant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `fk_msg_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_msg_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `notes_cours`
--
ALTER TABLE `notes_cours`
  ADD CONSTRAINT `fk_nc_cours` FOREIGN KEY (`cours_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_nc_etudiant` FOREIGN KEY (`etudiant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notif_alert` FOREIGN KEY (`related_alert_id`) REFERENCES `inactivity_alerts` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_notif_course` FOREIGN KEY (`related_course_id`) REFERENCES `courses` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `parcours`
--
ALTER TABLE `parcours`
  ADD CONSTRAINT `fk_parcours_filiere` FOREIGN KEY (`filiere_id`) REFERENCES `filieres` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_parcours_niveau` FOREIGN KEY (`niveau_id`) REFERENCES `niveaux_etudes` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `qcm`
--
ALTER TABLE `qcm`
  ADD CONSTRAINT `fk_qcm_cours` FOREIGN KEY (`cours_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_qcm_createur` FOREIGN KEY (`cree_par`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `session_transcriptions`
--
ALTER TABLE `session_transcriptions`
  ADD CONSTRAINT `fk_transcript_session` FOREIGN KEY (`session_id`) REFERENCES `course_sessions` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `student_activity`
--
ALTER TABLE `student_activity`
  ADD CONSTRAINT `fk_activity_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_activity_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `teacher_classes`
--
ALTER TABLE `teacher_classes`
  ADD CONSTRAINT `fk_teacher_classes_classe` FOREIGN KEY (`classe_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_teacher_classes_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
