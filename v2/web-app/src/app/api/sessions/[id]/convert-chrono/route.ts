import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const sessionId = parseInt(id);
    const { tab_index } = await request.json();

    const [sessions] = await pool.query<RowDataPacket[]>(
      `SELECT sess.start_time, sess.status, sess.paused_at, sess.pause_duration_seconds, st.default_rate_per_hour,
         CASE 
           WHEN sess.status = 'PAUSED' THEN TIMESTAMPDIFF(SECOND, sess.start_time, sess.paused_at) - COALESCE(sess.pause_duration_seconds, 0)
           ELSE TIMESTAMPDIFF(SECOND, sess.start_time, NOW()) - COALESCE(sess.pause_duration_seconds, 0)
         END AS live_active_seconds
       FROM sessions sess
       JOIN stations st ON sess.station_id = st.id
       WHERE sess.id = ?`,
      [sessionId]
    );

    if (sessions.length === 0) return NextResponse.json({ error: 'Session introuvable' }, { status: 404 });
    const session = sessions[0];
    
    const activeSeconds = Math.max(0, session.live_active_seconds);
    const durationMinutes = Math.round(activeSeconds / 60);
    const baseCost = (durationMinutes / 60) * session.default_rate_per_hour;

    if (durationMinutes > 0 && baseCost > 0) {
      // Find GAME_TIME product
      const [gameProducts] = await pool.query<RowDataPacket[]>(
        "SELECT id FROM products WHERE category = 'GAME_TIME' LIMIT 1"
      );

      if (gameProducts.length > 0) {
        const gameProductId = gameProducts[0].id;
        
        // 1. Ajouter l'article dans la liste
        await pool.query(
          `INSERT INTO order_items (session_id, product_id, quantity, unit_price, tab_index) 
           VALUES (?, ?, ?, ?, ?)`,
          [sessionId, gameProductId, durationMinutes, session.default_rate_per_hour / 60, tab_index || 1]
        );
      }
    }

    // 2. Remettre à zéro le chrono et le mettre en pause
    await pool.query(
      `UPDATE sessions 
       SET start_time = NOW(), 
           status = 'PAUSED',
           paused_at = NOW(), 
           pause_duration_seconds = 0,
           transferred_minutes = 0
       WHERE id = ?`,
      [sessionId]
    );

    return NextResponse.json({ success: true, durationMinutes, baseCost });
  } catch (error) {
    console.error('Error converting chrono:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
