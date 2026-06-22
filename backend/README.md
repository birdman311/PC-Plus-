# PC Plus+ Presence Backend

This little server is the "middleman" that lets PC Plus+ users see each other — both in
the tab list and as a green `+` floating above a nearby player's head — on **any**
Minecraft server, even ones that don't have the mod installed.

A normal Minecraft server won't pass mod messages between players, so the clients can't
find each other on their own. Instead, each PC Plus+ client quietly sends a heartbeat
here every 30 seconds saying "I'm `<uuid>` and I'm on server `<address>`." The backend
replies with the other PC Plus+ users on that same address, and the mod draws the green
`+` next to them.

That's all it does. No accounts, no commands, no long-term storage — presence entries
expire 90 seconds after a client stops sending heartbeats.

## What you need to do

1. Deploy `server.js` somewhere that stays online 24/7 (see options below).
2. Copy the public URL it gives you (e.g. `https://pcplus.onrender.com`).
3. In the mod, open `PresenceClient.java` and replace this line:
   ```java
   private static final String BACKEND_URL = "https://YOUR-BACKEND-URL";
   ```
   with your real URL (no trailing slash). Then rebuild the mod.

Until you set a real URL, the marker feature simply stays off and the rest of the mod
works normally.

## Running it

Plain Node.js with **no dependencies** — no `npm install` needed.

```
node server.js
```

It listens on port 3000 by default, or whatever `PORT` the host provides.

## Free / cheap hosting options

Render and Railway are the easiest if you've never done this before:

- **Render** (free tier): New > Web Service, point it at this folder, leave the build
  command empty, start command `node server.js`.
- **Railway**: New Project > Deploy, start command `node server.js`.
- **Replit**: create a Node repl, paste `server.js`, click Run.
- **A VPS you own**: `node server.js` (use `pm2` or a systemd service to keep it alive).

## Privacy note

Clients send their Minecraft UUID and the address of the server they're on — nothing
else, and nothing is saved to disk. If you distribute the mod publicly, it's good
practice to mention this in your description.
