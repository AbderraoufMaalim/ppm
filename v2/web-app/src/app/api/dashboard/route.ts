import { NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// GET /api/dashboard — Statistiques du jour
export async function GET() {
  try {
    // Chiffre d'affaires du jour
    const [revenueRows] = await pool.query<RowDataPacket[]>(`
      SELECT COALESCE(SUM(total_cost), 0) as daily_revenue
      FROM sessions 
      WHERE status = 'CLOSED' AND DATE(end_time) = CURDATE()
    `);

    // Nombre de sessions du jour (fermées)
    const [sessionCountRows] = await pool.query<RowDataPacket[]>(`
      SELECT COUNT(*) as sessions_today
      FROM sessions 
      WHERE DATE(start_time) = CURDATE()
    `);

    // Sessions actives en ce moment
    const [activeRows] = await pool.query<RowDataPacket[]>(`
      SELECT COUNT(*) as active_sessions
      FROM sessions 
      WHERE status = 'ACTIVE'
    `);

    // Nombre total de stations
    const [stationRows] = await pool.query<RowDataPacket[]>(`
      SELECT COUNT(*) as total_stations
      FROM stations WHERE is_active = TRUE
    `);

    // Répartition du CA par type de station (aujourd'hui)
    const [revenueByType] = await pool.query<RowDataPacket[]>(`
      SELECT st.type, COALESCE(SUM(sess.total_cost), 0) as revenue
      FROM sessions sess
      JOIN stations st ON st.id = sess.station_id
      WHERE sess.status = 'CLOSED' AND DATE(sess.end_time) = CURDATE()
      GROUP BY st.type
    `);

    // CA des 7 derniers jours
    const [weeklyRevenue] = await pool.query<RowDataPacket[]>(`
      SELECT DATE(end_time) as day, COALESCE(SUM(total_cost), 0) as revenue
      FROM sessions 
      WHERE status = 'CLOSED' AND end_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
      GROUP BY DATE(end_time)
      ORDER BY day ASC
    `);

    return NextResponse.json({
      daily_revenue: revenueRows[0].daily_revenue,
      sessions_today: sessionCountRows[0].sessions_today,
      active_sessions: activeRows[0].active_sessions,
      total_stations: stationRows[0].total_stations,
      revenue_by_type: revenueByType,
      weekly_revenue: weeklyRevenue,
    });
  } catch (error) {
    console.error('Error fetching dashboard:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
