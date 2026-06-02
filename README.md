# PushPlayManager

Logiciel de gestion interne pour cafétéria gaming (sessions de jeux PlayStation, PC, consommations, et statistiques).

## 🚀 Version 2 (Production & Hybride)

La version 2 utilise une architecture moderne basée sur **Next.js 16** et **MySQL 8.0** s'exécutant dans des conteneurs isolés **Docker**. 

L'accès est configuré de façon hybride :
- **En local** (Wi-Fi de la salle) pour les tablettes serveurs et la caisse (résistant aux coupures internet).
- **À distance** (Web) via un **tunnel Cloudflare** sécurisé pour l'administration.

### 📋 Démarrage Rapide

1. Installez **Docker Desktop** et **Git** sur la machine.
2. Clonez le dépôt et déplacez-vous dans le dossier `v2` :
   ```bash
   cd v2
   ```
3. Créez votre fichier `.env` à partir de `.env.example` et remplissez vos secrets :
   ```bash
   cp .env.example .env
   ```
4. Lancez le projet avec Docker Compose :
   ```bash
   docker compose -f docker-compose.prod.yml up -d --build
   ```

### 📖 Documentation Complète

Pour un guide étape par étape détaillé (installation de Git, Docker, configuration réseau, pare-feu Windows, et installation des outils d'IA comme Cursor ou Windsurf), veuillez vous référer au guide d'installation :
👉 [Guide d'Installation en Production & Configuration IA](.gemini/antigravity/brain/e55d672e-f0d7-4409-8497-33909e390002/artifacts/production_setup_guide.md)
