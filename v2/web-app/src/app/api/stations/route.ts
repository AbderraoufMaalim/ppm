import { NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// GET /api/stations — Liste toutes les stations avec leur session active éventuelle
export async function GET() {
  try {
    const [rows] = await pool.query<RowDataPacket[]>(`
      SELECT 
        s.id, s.name, s.type, s.default_rate_per_hour, s.is_active,
        sess.id AS active_session_id,
        sess.start_time AS session_start,
        sess.client_id,
        sess.status AS session_status,
        sess.paused_at,
        sess.pause_duration_seconds,
        CASE 
          WHEN sess.status = 'PAUSED' THEN TIMESTAMPDIFF(SECOND, sess.start_time, sess.paused_at) - COALESCE(sess.pause_duration_seconds, 0)
          ELSE TIMESTAMPDIFF(SECOND, sess.start_time, NOW()) - COALESCE(sess.pause_duration_seconds, 0)
        END AS live_active_seconds,
        sess.transferred_minutes,
        sess.note AS session_note,
        (SELECT COALESCE(SUM(quantity * unit_price), 0) FROM order_items WHERE session_id = sess.id) AS orders_total_cost
      FROM stations s
      LEFT JOIN sessions sess ON sess.station_id = s.id AND sess.status IN ('ACTIVE', 'PAUSED')
      ORDER BY s.type, s.name
    `);
    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching stations:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
