📘 FICHE TECHNIQUE — Plateforme d’E-Learning (Version Rédigée en Phrases)

La plateforme d’e-learning a pour objectif de proposer une solution web moderne, innovante, modulaire et sécurisée. Elle repose sur une architecture complète combinant Spring Boot pour le backend, une API REST sécurisée, une base de données MySQL, la conteneurisation Docker et une interface utilisateur légère développée en HTML, CSS, JavaScript et Thymeleaf. L’ensemble est conçu pour être scalable, maintenable et facile à déployer.

🎯 Objectif Technique Global

L’objectif principal du projet consiste à développer une application web d’e-learning capable de gérer efficacement les utilisateurs, les cours, les ressources pédagogiques, les devoirs, les évaluations et la communication interne. La solution doit être sécurisée, performante et évolutive, tout en intégrant éventuellement des fonctionnalités innovantes et avancées comme la transcription automatique et la détection d’inactivité via des modules d’IA.

🏗️ Architecture Générale du Projet

La plateforme adopte une architecture 3-tiers, comprenant un frontend web, un backend Spring Boot et une base de données MySQL.
Le frontend est construit en HTML, CSS et JavaScript, tout en s’appuyant sur Thymeleaf pour générer des pages dynamiques. Les échanges entre le navigateur et le serveur passent par des requêtes AJAX/Fetch vers des endpoints REST sécurisés.

Le backend, développé avec Spring Boot, expose l’ensemble des fonctionnalités via une API REST. Il s’appuie sur une structure claire de type Controller–Service–Repository afin de séparer proprement les responsabilités. Il gère la logique métier liée à l’authentification, aux utilisateurs, aux cours, aux ressources, aux quiz, aux devoirs, aux messages internes et aux éventuelles fonctionnalités IA.

La base de données MySQL stocke toutes les entités essentielles : utilisateurs, rôles, cours, ressources, quiz, soumissions d’étudiants, messages internes, logs d’activité, etc. Les données sont gérées à l’aide de JPA/Hibernate, avec la possibilité d’intégrer Flyway pour versionner le schéma de manière professionnelle.

L’ensemble du système est conteneurisé avec Docker. Un fichier docker-compose.yml orchestre les services nécessaires : le backend Spring Boot, MySQL et un outil de gestion comme PhpMyAdmin ou Adminer. Chaque service est isolé dans un conteneur et communique via un réseau interne.

🛠️ Stack Technique

Le backend repose sur Spring Boot 3+, Spring Web pour les endpoints REST, Spring Data JPA pour l’accès aux données et Lombok pour faciliter l’écriture du code. Le projet peut être construit avec Gradle.

Plusieurs modules internes sont mis en place pour organiser proprement l’application :
un module User (gestion des utilisateurs et rôles), un module Auth (connexion, inscription), un module Cours (gestion des cours et de leurs ressources), un module Quiz, un module Assignments pour les devoirs, un module Messaging, ainsi qu’un module AI pour les fonctionnalités d’intelligence artificielle.

🗄️ Base de Données

La base de données comporte des tables dédiées aux utilisateurs, aux rôles, aux cours, aux ressources de cours, aux quiz, aux questions, aux réponses, aux devoirs et aux soumissions. Elle inclut également des tables de suivi d’activité étudiante ainsi que des logs spécifiques aux fonctionnalités IA.

Les relations entre les tables (OneToMany, ManyToOne, ManyToMany) sont définies avec précision. Des index peuvent être créés pour optimiser les performances. L’intégration d’un outil comme Flyway permettrait de gérer l’évolution du schéma de manière professionnelle.

🌐 Frontend

Le frontend repose sur HTML5, CSS3 et JavaScript, en complément de Thymeleaf pour générer des pages dynamiques côté serveur. Les appels à l’API REST sont effectués principalement via la Fetch API.
La plateforme propose plusieurs interfaces essentielles : connexion, inscription, dashboard étudiant, dashboard enseignant, gestion des cours, consultation des ressources, dépôt des devoirs, quiz interactifs et messagerie interne.

🐳 Dockerisation

Le déploiement de la plateforme est entièrement automatisé grâce à Docker.
Trois conteneurs principaux sont utilisés : le backend Spring Boot, MySQL avec un volume persistant, et PhpMyAdmin ou Adminer pour l’administration de la base.
Un fichier docker-compose.yml définit les services, leurs configurations, le réseau interne et les volumes associés. Le tout peut être lancé très simplement grâce à une commande unique.


🧪 Tests & Qualité

Les tests unitaires et d’intégration sont réalisés à l’aide de JUnit 5, MockMvc et Testcontainers.
Les tests API peuvent également être effectués avec Postman.
La documentation automatique de l’API est produite via Swagger/OpenAPI.

🧰 Outils DevOps & Productivité

Le projet s’appuie sur GitHub pour la gestion du code, les branches de développement, un tableau Kanban via GitHub Projects, et éventuellement GitHub Actions pour le CI/CD.
Les images Docker peuvent être hébergées sur Docker Hub.
