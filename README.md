![CI](https://github.com/MoussaMS/Projet_DevOps_MIAGE/actions/workflows/ci.yml/badge.svg)
![Docs](https://github.com/MoussaMS/Projet_DevOps_MIAGE/actions/workflows/doc.yml/badge.svg)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

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

🧠 User Story — Feature 1 : Transcription et résumé intelligent des cours
📍 Contexte
Nous sommes dans une université utilisant la nouvelle plateforme d’e-learning.
Chaque cours magistral ou TD est capté automatiquement grâce à l’API Gladia, qui enregistre le vocal du cours pendant la séance et le transcrit simultanément.
Ainsi, à la fin du cours, la plateforme dispose à la fois de l’audio original et de la transcription complète.
Une fois la séance terminée, le cours et sa transcription sont automatiquement sauvegardés dans un espace de stockage S3 (Amazon ou équivalent), puis intégrés à la plateforme d’e-learning.
Les étudiants peuvent ensuite y accéder directement, accompagnés d’un résumé intelligent généré par une IA.
De plus, un enseignant a la possibilité de transmettre un cours audio externe (par exemple un enregistrement personnel ou une capsule pédagogique) que l’IA analyse, résume et structure automatiquement pour l’intégrer à la plateforme.
________________________________________
⚙️ Déroulement technique et pédagogique
•	Pendant la séance :
→ Gladia capture le flux audio du professeur et le convertit en texte en direct.
•	À la fin du cours :
→ La plateforme compile l’enregistrement et la transcription,
→ Un modèle d’IA (comme OpenAI GPT ou un modèle local) :
o	génère un résumé synthétique,
o	extrait les mots-clés essentiels,
o	structure le texte (titres, sous-parties, notions clés),
o	et rend le cours consultable en plein texte dans la plateforme.
•	Stockage et diffusion :
→ L’ensemble (audio + transcription + résumé) est automatiquement stocké dans S3 et lié à la fiche du cours.
→ Les étudiants reçoivent une notification d’accès via leur tableau de bord e-learning.
•	Chatbot pédagogique :
→ Un assistant conversationnel intelligent (chatbot) est connecté à la base de transcriptions.
→ Lorsqu’un étudiant pose une question, le chatbot recherche les passages pertinents du cours (approche RAG — Retrieval-Augmented Generation) et formule une réponse claire et contextualisée.
💡 En complément, un professeur peut importer manuellement un fichier audio (ex. “cours enregistrés sur dictaphone” ou “capsule thématique”).
L’IA se charge alors de le transcrire, le résumer et le formater selon la même logique :
ajout d’un titre, d’un sommaire automatique et d’un résumé pédagogique clair.
________________________________________
👩🏫 Exemple vécu
•	Pendant le cours :
→ L’API Gladia capte la voix du professeur et affiche en temps réel la transcription.
→ Les étudiants à distance suivent la séance avec la transcription en direct.
•	Après le cours :
→ Chaque étudiant peut accéder au cours sur la plateforme :
“La transcription et l’enregistrement du cours Apprentissage automatique – Semaine 3 sont disponibles.”
→ Il accède au fichier audio, à la transcription complète et au résumé automatique.
→ Il tape “différence entre apprentissage supervisé et non supervisé” dans la barre de recherche :
la plateforme surligne le passage exact.
→ Il demande au chatbot : « Peux-tu m’expliquer ça avec un exemple ? »
→ Le chatbot répond en s’appuyant sur le contenu du cours transcrit.
•	Le professeur :
→ Peut importer un cours audio complémentaire enregistré à part (ex. “Introduction aux arbres de décision”).
→ L’IA en produit un résumé clair et structuré automatiquement,
avec des sections logiques et une fiche de révision intégrée.
→ Il consulte ensuite un dashboard analytique indiquant les passages les plus consultés ou questionnés.
________________________________________
🎯 Résultat global
👉 Chaque cours (en présentiel ou importé) est enregistré, transcrit, résumé et structuré automatiquement.
👉 Les étudiants disposent d’un accès complet au contenu audio et textuel, même après la séance.
👉 Le professeur peut enrichir la plateforme avec des cours audio complémentaires.
👉 Le chatbot pédagogique renforce la compréhension en offrant un dialogue intelligent basé sur le contenu réel du cours.
👉 Le tout favorise un apprentissage accessible, interactif et intelligent.

🧠 User Story — Feature 2 : Détection et alerte d’inactivité étudiante
Type : Fonctionnalité innovante (automatisation / analyse simple)
Priorité : Haute (suivi pédagogique et prévention du décrochage)
________________________________________
🎯 Intitulé
En tant qu’enseignant, je veux être alerté automatiquement lorsqu’un étudiant n’a pas été actif depuis plusieurs jours (pas ouvert de cours, pas rendu de devoir, ni complété de quiz), afin de prévenir le décrochage et le relancer à temps.
________________________________________
🧩 Contexte et motivation
Dans un environnement d’e-learning, le risque de décrochage silencieux est fréquent : un étudiant peut cesser de se connecter ou de participer sans prévenir.
Les enseignants n’ont pas toujours la visibilité nécessaire pour identifier ces étudiants avant qu’il ne soit trop tard.
Cette fonctionnalité vise donc à automatiser la détection de l’inactivité et à alerter l’enseignant lorsqu’un apprenant s’éloigne du parcours, tout en notifiant l’étudiant pour le réengager.
L’objectif est d’apporter une surveillance intelligente, sans machine learning complexe, mais via des règles simples et automatisées de suivi d’activité.
________________________________________
⚙️ Description fonctionnelle
Déclenchement automatique
•	Une tâche planifiée (scheduler Spring Boot) s’exécute chaque nuit.
•	Elle vérifie pour chaque étudiant :
o	sa dernière activité (consultation de cours, rendu de devoir, quiz réalisé).
o	si le délai depuis la dernière activité ≥ N jours (par défaut 7).
•	Si l’étudiant est inactif :
o	Une alerte d’inactivité est créée et liée à l’enseignant responsable du cours.
o	Une notification est envoyée à l’étudiant (message in-app ou e-mail).
•	Si l’étudiant reprend une activité :
o	L’alerte est automatiquement fermée.
________________________________________
📱 Cas d’usage concret
1.	L’étudiant Martin suit le cours “Programmation Java”.
2.	Il ne se connecte plus pendant 8 jours.
3.	Le système détecte son inactivité et :
o	Crée une alerte visible par le professeur dans son tableau de bord (“Martin Durand — 8 jours d’inactivité”).
o	Envoie à Martin une notification personnalisée :
“Tu n’as pas consulté le cours de Java depuis plus d’une semaine. Souhaites-tu reprendre là où tu t’étais arrêté ?”
4.	L’enseignant peut accuser réception, contacter l’étudiant ou ignorer l’alerte.
5.	Dès que Martin se reconnecte, l’alerte est automatiquement fermée et l’historique mis à jour.
