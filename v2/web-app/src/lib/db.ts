import mysql from 'mysql2/promise';

const poolConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '3306'),
  user: process.env.DB_USER || 'ppm_user',
  password: process.env.DB_PASSWORD || 'ppm_password',
  database: process.env.DB_NAME || 'pushplay',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
};

let pool: mysql.Pool;

if (process.env.NODE_ENV === 'production') {
  pool = mysql.createPool(poolConfig);
} else {
  let globalWithMysql = global as typeof globalThis & {
    _mysqlPool?: mysql.Pool;
  };
  if (!globalWithMysql._mysqlPool) {
    globalWithMysql._mysqlPool = mysql.createPool(poolConfig);
  }
  pool = globalWithMysql._mysqlPool;
}

export default pool;
