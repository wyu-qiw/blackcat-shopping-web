const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const src = path.join(root, 'frontend', 'dist');
const dest = path.join(root, 'dist');
const indexFile = path.join(src, 'index.html');

if (!fs.existsSync(indexFile)) {
  throw new Error(`Expected build output not found: ${indexFile}`);
}

fs.rmSync(dest, { recursive: true, force: true });
fs.cpSync(src, dest, { recursive: true });

console.log(`Copied frontend/dist -> dist`);
