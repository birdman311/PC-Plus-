// PC Plus+ presence backend
// Zero-dependency Node service. Run with: node server.js
//
// Each PC Plus+ client sends a heartbeat saying "I'm <uuid> on server <address>",
// and the backend replies with the other PC Plus+ users on that same address.
// Open the root URL in a browser for a live view of who is currently checked in.

const http = require('http');

const PORT = process.env.PORT || 3000;
const PRESENCE_TTL_MS = 90 * 1000; // a client counts as present if seen within 90s

// uuid -> { server, lastSeen }
const presence = new Map();

function purge() {
  const now = Date.now();
  for (const [uuid, info] of presence) {
    if (now - info.lastSeen > PRESENCE_TTL_MS) presence.delete(uuid);
  }
}

function readBody(req) {
  return new Promise((resolve) => {
    let data = '';
    req.on('data', (c) => {
      data += c;
      if (data.length > 1e6) req.destroy();
    });
    req.on('end', () => resolve(data));
  });
}

function currentList() {
  purge();
  const now = Date.now();
  const list = [];
  for (const [uuid, info] of presence) {
    list.push({ uuid, name: info.name || '', server: info.server, serverName: info.serverName || info.server, secondsAgo: Math.round((now - info.lastSeen) / 1000) });
  }
  return list;
}

const server = http.createServer(async (req, res) => {
  // Heartbeat from a game client
  if (req.method === 'POST' && req.url === '/heartbeat') {
    res.setHeader('Content-Type', 'application/json');
    try {
      const body = JSON.parse(await readBody(req));
      const uuid = String(body.uuid || '');
      const name = String(body.name || '').slice(0, 32);
      const srv = String(body.server || '').toLowerCase();
      const serverName = String(body.serverName || srv).slice(0, 80);

      if (!uuid || !srv) {
        res.statusCode = 400;
        res.end('{"error":"bad request"}');
        return;
      }

      presence.set(uuid, { name, server: srv, serverName, lastSeen: Date.now() });
      console.log('heartbeat: ' + (name || uuid) + ' on ' + srv);
      purge();

      const users = [];
      for (const [id, info] of presence) {
        if (info.server === srv) users.push(id);
      }
      res.end(JSON.stringify({ users }));
    } catch (e) {
      res.statusCode = 400;
      res.end('{"error":"invalid json"}');
    }
    return;
  }

  // Explicit leave from a game client (clean disconnect)
  if (req.method === 'POST' && req.url === '/leave') {
    res.setHeader('Content-Type', 'application/json');
    try {
      const body = JSON.parse(await readBody(req));
      const uuid = String(body.uuid || '');
      if (uuid && presence.delete(uuid)) {
        console.log('leave: ' + uuid);
      }
      res.end('{"ok":true}');
    } catch (e) {
      res.statusCode = 400;
      res.end('{"error":"invalid json"}');
    }
    return;
  }

  // Machine-readable list
  if (req.method === 'GET' && req.url === '/players') {
    const list = currentList();
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify({ online: list.length, players: list }, null, 2));
    return;
  }

  // Human-friendly live dashboard
  if (req.method === 'GET' && req.url === '/') {
    const list = currentList();
    let rows = list
      .map((p) => '<tr><td>' + (p.name || p.uuid) + '</td><td>' + (p.serverName || p.server) + '</td><td>' + p.secondsAgo + 's ago</td></tr>')
      .join('');
    if (!rows) rows = '<tr><td colspan="3">Nobody is sending heartbeats right now.</td></tr>';

    res.setHeader('Content-Type', 'text/html');
    res.end(
      '<!doctype html><html><head><meta charset="utf-8">' +
      '<title>PC Plus+ presence</title>' +
      '<meta http-equiv="refresh" content="10">' +
      '<style>body{font-family:sans-serif;background:#111;color:#eee;padding:24px}' +
      'h1{color:#4caf50;margin-bottom:4px}p{color:#aaa}' +
      'table{border-collapse:collapse;width:100%;margin-top:16px}' +
      'td,th{border:1px solid #333;padding:8px;text-align:left}th{background:#1b1b1b}</style>' +
      '</head><body>' +
      '<h1>PC Plus+ users online: ' + list.length + '</h1>' +
      '<p>Auto-refreshes every 10 seconds. A player shows here while their game is sending heartbeats.</p>' +
      '<table><tr><th>Player</th><th>Server</th><th>Last seen</th></tr>' + rows + '</table>' +
      '</body></html>'
    );
    return;
  }

  res.setHeader('Content-Type', 'application/json');
  res.statusCode = 404;
  res.end('{"error":"not found"}');
});

server.listen(PORT, () => console.log('PC Plus+ presence backend listening on port ' + PORT));
