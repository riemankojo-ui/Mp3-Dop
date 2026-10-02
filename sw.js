const CACHE_NAME = 'mp3-dop-v17';
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon.png',
  './jsmediatags.min.js',
  './browser-id3-writer.min.js',
  './lame.min.js'
];

self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return Promise.allSettled(ASSETS.map(url => cache.add(url)));
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (e) => {
  e.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', (e) => {
  e.respondWith(
    caches.match(e.request).then((response) => {
      return response || fetch(e.request);
    })
  );
});