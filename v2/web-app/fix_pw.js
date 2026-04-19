const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');

async function fix() {
  const pool = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: 'rootpassword',
    database: 'pushplay',
    port: 3306
  });

  const hash = await bcrypt.hash('admin', 10);
  console.log('Generating new hash:', hash);

  await pool.query('UPDATE users SET password_hash = ? WHERE username = ?', [hash, 'Admin']);
  await pool.query('UPDATE users SET password_hash = ? WHERE username = ?', [hash, 'Caissier']);
  
  const [rows] = await pool.query('SELECT username, password_hash FROM users');
  console.log('Database updated successfully:');
  console.log(rows);
  
  process.exit(0);
}

fix();
