const mysql = require('mysql2/promise');

async function migrate() {
  const pool = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: 'rootpassword',
    database: 'pushplay',
    port: 3306
  });

  try {
    // 1. Add columns to sessions
    console.log('Adding columns to sessions...');
    await pool.query(`ALTER TABLE sessions ADD COLUMN paused_at DATETIME NULL;`).catch(() => console.log('paused_at already exists'));
    await pool.query(`ALTER TABLE sessions ADD COLUMN pause_duration_seconds INT DEFAULT 0;`).catch(() => console.log('pause_duration_seconds already exists'));
    
    // We should allow status to be 'PAUSED'
    // Status is VARCHAR(50) so it's fine.

    // 2. Clear old data to cleanly insert PS 1..6
    console.log('Clearing old sessions and stations...');
    await pool.query('DELETE FROM order_items');
    await pool.query('DELETE FROM sessions');
    await pool.query('DELETE FROM stations');
    
    // Reset auto-increments
    await pool.query('ALTER TABLE stations AUTO_INCREMENT = 1');
    await pool.query('ALTER TABLE sessions AUTO_INCREMENT = 1');
    await pool.query('ALTER TABLE order_items AUTO_INCREMENT = 1');

    // 3. Insert new PlayStation Stations
    console.log('Inserting 6 PS stations...');
    await pool.query("INSERT INTO stations (name, type, default_rate_per_hour) VALUES ('PS 1', 'PS_NORMAL', 370.00)");
    await pool.query("INSERT INTO stations (name, type, default_rate_per_hour) VALUES ('PS 2', 'PS_NORMAL', 370.00)");
    await pool.query("INSERT INTO stations (name, type, default_rate_per_hour) VALUES ('PS 3', 'PS_NORMAL', 370.00)");
    await pool.query("INSERT INTO stations (name, type, default_rate_per_hour) VALUES ('PS 4', 'PS_NORMAL', 370.00)");
    await pool.query("INSERT INTO stations (name, type, default_rate_per_hour) VALUES ('PS 5', 'PS_MULTI', 520.00)");
    await pool.query("INSERT INTO stations (name, type, default_rate_per_hour) VALUES ('PS 6 VIP', 'PS_MULTI', 520.00)");

    console.log('Migration complete!');
    process.exit(0);
  } catch (error) {
    console.error('Migration failed:', error);
    process.exit(1);
  }
}

migrate();
