const mysql = require('mysql2/promise');

async function migrate() {
  const pool = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: 'rootpassword',
    database: 'pushplay',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
  });

  try {
    await pool.query('ALTER TABLE products ADD COLUMN is_active BOOLEAN DEFAULT TRUE;');
    console.log('Migration successful: is_active column added to products');
  } catch (error) {
    if (error.code === 'ER_DUP_FIELDNAME') {
      console.log('Column already exists.');
    } else {
      console.error('Migration failed:', error);
    }
  } finally {
    await pool.end();
  }
}

migrate();
