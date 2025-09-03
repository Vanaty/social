# Configuration de Sécurité Séparée MVC/API

## Vue d'ensemble

Cette configuration sépare la sécurité entre les services MVC (vues Thymeleaf) et les API REST :

### 1. Sécurité API (JWT) - Ordre 1
- **Chemin :** `/api/**`, `/v3/api-docs/**`, `/swagger-ui/**`
- **Authentification :** JWT Token via filter `JwtRequestFilter`
- **Session :** STATELESS (pas de session)
- **Endpoints publics :**
  - `/api/auth/**` - Authentification API
  - `/api/files/download/**` - Téléchargement de fichiers
  - Documentation Swagger

### 2. Sécurité MVC (Session) - Ordre 2
- **Chemin :** `/**` (tous les autres chemins)
- **Authentification :** Form-based avec sessions
- **Session :** IF_REQUIRED (création de session au besoin)
- **Endpoints publics :**
  - `/`, `/sign-in`, `/sign-up`, `/disconnect`
  - `/assets/**`, `/icons/**` - Ressources statiques
  - `/ws/**` - WebSocket connections

## Fonctionnalités MVC

### Connexion
- **URL de connexion :** `/mvc-login` (POST)
- **Page de connexion :** `/sign-in`
- **Succès :** Redirection vers `/dashboard`
- **Échec :** Retour à `/sign-in?error=true`

### Déconnexion
- **URL de déconnexion :** `/logout` (POST)
- **Succès :** Redirection vers `/sign-in?logout=true`
- **Actions :** Invalidation session, suppression cookies

### Gestion des erreurs
- Les erreurs d'authentification sont affichées via les paramètres URL
- Support des messages Thymeleaf avec `th:if="${param.error}"`

## Avantages de cette approche

1. **Séparation claire** : API et MVC ont des mécanismes d'authentification distincts
2. **Flexibilité** : Les API peuvent être consommées par des clients externes avec JWT
3. **UX optimisée** : Les vues utilisent l'authentification traditionnelle avec sessions
4. **Sécurité** : Chaque contexte a sa propre configuration adaptée

## Utilisation

### Pour les API (clients externes)
```bash
# 1. Authentification
POST /api/auth/login
{
  "username": "user",
  "password": "password"
}

# 2. Utilisation du token
GET /api/chats
Authorization: Bearer <jwt-token>
```

### Pour les vues (navigateur)
1. Accéder à `/sign-in`
2. Remplir le formulaire
3. Soumission automatique vers `/mvc-login`
4. Redirection vers `/dashboard` si succès

## Notes techniques

- Le filter JWT ne s'applique qu'aux endpoints `/api/**`
- Les sessions sont gérées automatiquement pour les vues MVC
- CSRF désactivé pour simplifier (peut être réactivé si nécessaire)
- Remember-me disponible dans le formulaire de connexion
