import { NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// GET /api/stations — Liste toutes les stations avec leur session active éventuelle
export async function GET() {
  try {
    const [rows] = await pool.query<RowDataPacket[]>(`
      SELECT 
        s.id, s.name, s.type, s.default_rate_per_hour, s.is_active, s.display_order,
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
      WHERE s.is_active = true
      ORDER BY 
        CASE 
          WHEN s.type IN ('PS_NORMAL', 'PS_MULTI') THEN 1
          WHEN s.type = 'TABLE' THEN 2
          WHEN s.type = 'CHAIR' THEN 3
          ELSE 4
        END,
        s.display_order ASC, 
        s.name ASC
    `);
    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching stations:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// POST /api/stations — Create a new station
export async function POST(req: Request) {
  try {
    const { name, type, default_rate_per_hour } = await req.json();

    if (!name || !type) {
      return NextResponse.json({ error: 'Name and type are required' }, { status: 400 });
    }

    const rate = default_rate_per_hour || 0;

    const [result] = await pool.query<any>(
      'INSERT INTO stations (name, type, default_rate_per_hour) VALUES (?, ?, ?)',
      [name, type, rate]
    );

    return NextResponse.json({ id: result.insertId, name, type, default_rate_per_hour: rate }, { status: 201 });
  } catch (error) {
    console.error('Error creating station:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
