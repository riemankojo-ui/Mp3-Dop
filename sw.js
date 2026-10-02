const CACHE_NAME = 'mp3-dop-v3';
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon.png',
  'https://cdnjs.cloudflare.com/ajax/libs/jsmediatags/3.9.5/jsmediatags.min.js',
  'https://cdn.jsdelivr.net/npm/browser-id3-writer@6.1.0/dist/browser-id3-writer.min.js',
  'https://cdnjs.cloudflare.com/ajax/libs/lamejs/1.2.0/lame.min.js'
];

self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      // Use allSettled so a single failed CDN link doesn't break the whole app
      return Promise.allSettled(
        ASSETS.map(url => cache.add(url))
      );
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (e) => {
  e.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', (e) => {
  // Cache-First strategy: serve from cache immediately, fallback to network
  e.respondWith(
    caches.match(e.request).then((response) => {
      return response || fetch(e.request);
    })
  );
});