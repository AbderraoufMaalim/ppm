import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { order_id } = body;

    if (!order_id) {
      return NextResponse.json({ error: 'order_id est requis' }, { status: 400 });
    }

    // Récupérer l'article
    const [orders] = await pool.query<RowDataPacket[]>(
      `SELECT oi.id, oi.session_id, oi.quantity, p.category 
       FROM order_items oi
       JOIN products p ON oi.product_id = p.id
       WHERE oi.id = ? AND p.category = 'GAME_TIME'`,
      [order_id]
    );

    if (orders.length === 0) {
      return NextResponse.json({ error: 'Article Temps de jeu introuvable' }, { status: 404 });
    }

    const order = orders[0];
    const minutes = order.quantity;
    const sessionId = order.session_id;

    // Transférer au chrono: soustraire 'minutes' au start_time, et incrémenter transferred_minutes
    await pool.query(
      `UPDATE sessions 
       SET start_time = DATE_SUB(start_time, INTERVAL ? MINUTE),
           transferred_minutes = transferred_minutes + ?
       WHERE id = ?`,
      [minutes, minutes, sessionId]
    );

    // Supprimer l'article
    await pool.query(
      `DELETE FROM order_items WHERE id = ?`,
      [order_id]
    );

    return NextResponse.json({ success: true, transferred_minutes: minutes });
  } catch (error) {
    console.error('Error transferring to chrono:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
