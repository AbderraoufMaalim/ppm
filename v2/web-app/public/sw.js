// ============================================
// PushPlayManager V2 — Service Worker
// Cache le shell de l'app, laisse les API en network-first
// ============================================

const CACHE_NAME = 'ppm-v2-cache-v1';

// Assets statiques à pré-cacher
const PRECACHE_ASSETS = [
  '/',
  '/manifest.json',
];

// Installation : pré-cache des assets statiques
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(PRECACHE_ASSETS))
      .then(() => self.skipWaiting())
  );
});

// Activation : nettoyer les anciens caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then(keys => {
      return Promise.all(
        keys
          .filter(key => key !== CACHE_NAME)
          .map(key => caches.delete(key))
      );
    }).then(() => self.clients.claim())
  );
});

// Fetch : Network-first pour les API, Cache-first pour les assets
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // Ne jamais cacher les appels API — toujours réseau
  if (url.pathname.startsWith('/api/')) {
    return;
  }

  // Pour les assets statiques : essayer le cache d'abord, sinon le réseau
  event.respondWith(
    caches.match(event.request).then(cached => {
      if (cached) return cached;
      return fetch(event.request).then(response => {
        // Cacher les nouvelles ressources statiques
        if (response.ok && event.request.method === 'GET') {
          const responseClone = response.clone();
          caches.open(CACHE_NAME).then(cache => {
            cache.put(event.request, responseClone);
          });
        }
        return response;
      });
    }).catch(() => {
      // En cas de panne réseau, afficher la page cachée
      return caches.match('/');
    })
  );
});
