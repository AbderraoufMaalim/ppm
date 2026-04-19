import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';

// GET /api/sessions — Toutes les sessions actives
export async function GET() {
  try {
    const [rows] = await pool.query<RowDataPacket[]>(`
      SELECT 
        sess.*,
        st.name AS station_name,
        st.type AS station_type,
        st.default_rate_per_hour
      FROM sessions sess
      JOIN stations st ON st.id = sess.station_id
      WHERE sess.status = 'ACTIVE'
      ORDER BY sess.start_time ASC
    `);
    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching sessions:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// POST /api/sessions — Démarrer une nouvelle session
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { station_id, user_id = 1, client_id = null } = body;

    if (!station_id) {
      return NextResponse.json({ error: 'station_id is required' }, { status: 400 });
    }

    // Vérifier qu'il n'y a pas déjà une session active sur ce poste
    const [existing] = await pool.query<RowDataPacket[]>(
      'SELECT id FROM sessions WHERE station_id = ? AND status = ?',
      [station_id, 'ACTIVE']
    );
    if (existing.length > 0) {
      return NextResponse.json({ error: 'Station already has an active session' }, { status: 409 });
    }

    const [result] = await pool.query<ResultSetHeader>(
      `INSERT INTO sessions (station_id, user_id, client_id, start_time, status) 
       VALUES (?, ?, ?, NOW(), 'ACTIVE')`,
      [station_id, user_id, client_id]
    );

    return NextResponse.json({ id: result.insertId, message: 'Session started' }, { status: 201 });
  } catch (error) {
    console.error('Error creating session:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
