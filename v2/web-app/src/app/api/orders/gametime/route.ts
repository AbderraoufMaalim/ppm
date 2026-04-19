import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';

// POST /api/orders/gametime — Ajouter du temps de jeu comme consommation
// Body: { session_id, minutes, tab_index? }
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { session_id, minutes, tab_index = 1 } = body;

    if (!session_id || !minutes || minutes <= 0) {
      return NextResponse.json({ error: 'session_id et minutes (> 0) sont requis' }, { status: 400 });
    }

    // Récupérer la session + le tarif horaire du poste
    const [sessions] = await pool.query<RowDataPacket[]>(
      `SELECT sess.id, sess.status, st.default_rate_per_hour
       FROM sessions sess
       JOIN stations st ON sess.station_id = st.id
       WHERE sess.id = ? AND sess.status IN ('ACTIVE', 'PAUSED')`,
      [session_id]
    );

    if (sessions.length === 0) {
      return NextResponse.json({ error: 'Session active ou en pause introuvable' }, { status: 404 });
    }

    const ratePerHour = Number(sessions[0].default_rate_per_hour);
    const ratePerMinute = Math.round((ratePerHour / 60) * 100) / 100;

    // Trouver le produit système "Temps de jeu"
    const [gameProducts] = await pool.query<RowDataPacket[]>(
      "SELECT id FROM products WHERE category = 'GAME_TIME' LIMIT 1"
    );

    if (gameProducts.length === 0) {
      return NextResponse.json({ error: 'Produit système "Temps de jeu" introuvable' }, { status: 500 });
    }

    const gameProductId = gameProducts[0].id;

    // Insérer toujours une nouvelle ligne pour le temps de jeu (ne pas additionner)
    const [result] = await pool.query<ResultSetHeader>(
      'INSERT INTO order_items (session_id, product_id, quantity, unit_price, tab_index) VALUES (?, ?, ?, ?, ?)',
      [session_id, gameProductId, minutes, ratePerMinute, tab_index]
    );

    return NextResponse.json({ id: result.insertId, message: 'Game time added', minutes, unit_price: ratePerMinute }, { status: 201 });
  } catch (error) {
    console.error('Error adding game time:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
