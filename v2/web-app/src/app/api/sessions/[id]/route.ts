import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';

// PATCH /api/sessions/:id — Arrêter une session et calculer le coût
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const sessionId = parseInt(id);

    // 1. Récupérer les infos de la session avec le calcul du temps actif par le moteur MySQL
    const [sessions] = await pool.query<RowDataPacket[]>(
      `SELECT sess.start_time, sess.station_id, sess.status, sess.paused_at, sess.pause_duration_seconds, st.default_rate_per_hour,
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
    if (session.status === 'CLOSED') return NextResponse.json({ error: 'Session déjà fermée' }, { status: 400 });

    const activeSeconds = Math.max(0, session.live_active_seconds);
    const durationMinutes = activeSeconds / 60;
    const baseCost = (durationMinutes / 60) * session.default_rate_per_hour;

    // Calculer le total des consommations
    const [orderRows] = await pool.query<RowDataPacket[]>(
      'SELECT COALESCE(SUM(quantity * unit_price), 0) as orders_total FROM order_items WHERE session_id = ?',
      [sessionId]
    );
    const ordersTotal = Number(orderRows[0].orders_total);
    const totalCost = baseCost + ordersTotal;

    // Fermer la session
    await pool.query<ResultSetHeader>(
      `UPDATE sessions SET end_time = NOW(), status = 'CLOSED', base_cost = ?, total_cost = ? WHERE id = ?`,
      [Math.round(baseCost * 100) / 100, Math.round(totalCost * 100) / 100, sessionId]
    );

    return NextResponse.json({
      message: 'Session closed',
      duration_minutes: Math.round(durationMinutes),
      base_cost: Math.round(baseCost * 100) / 100,
      orders_total: ordersTotal,
      total_cost: Math.round(totalCost * 100) / 100,
    });
  } catch (error) {
    console.error('Error closing session:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// DELETE /api/sessions/:id — Annuler une session ou remettre à zéro
export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const sessionId = parseInt(id);
    
    let keepOrders = false;
    let transferredMinutes = 0;
    let body: any = {};
    try {
      body = await request.json();
      keepOrders = body.keepOrders === true;
      transferredMinutes = body.transferredMinutes || 0;
    } catch {
      // Body empty or invalid
    }

    if (keepOrders) {
      if (transferredMinutes > 0) {
        const [sessions] = await pool.query<RowDataPacket[]>(
          `SELECT st.default_rate_per_hour
           FROM sessions sess
           JOIN stations st ON sess.station_id = st.id
           WHERE sess.id = ?`,
          [sessionId]
        );

        if (sessions.length > 0) {
          const ratePerHour = Number(sessions[0].default_rate_per_hour);
          const ratePerMinute = Math.round((ratePerHour / 60) * 100) / 100;

          // Trouver le produit système "Temps de jeu"
          const [gameProducts] = await pool.query<RowDataPacket[]>(
            "SELECT id FROM products WHERE category = 'GAME_TIME' LIMIT 1"
          );

          if (gameProducts.length > 0) {
            const gameProductId = gameProducts[0].id;

            // Utiliser l'onglet actif sélectionné par l'utilisateur, ou 1 par défaut
            const tabIndex = body.activeTab || 1;

            // Insérer toujours une nouvelle ligne sans vérifier si une existe déjà
            await pool.query(
              'INSERT INTO order_items (session_id, product_id, quantity, unit_price, tab_index) VALUES (?, ?, ?, ?, ?)',
              [sessionId, gameProductId, transferredMinutes, ratePerMinute, tabIndex]
            );
          }
        }
      }

      // Remise à zéro du chrono et mise en pause
      await pool.query(
        "UPDATE sessions SET start_time = NOW(), paused_at = NOW(), pause_duration_seconds = 0, status = 'PAUSED' WHERE id = ?",
        [sessionId]
      );

      return NextResponse.json({ message: 'Timer reset to 0, game time transferred', minutes_transferred: transferredMinutes });
    } else {
      // Supprimer la session. Grâce au ON DELETE CASCADE, les order_items seront purgés.
      await pool.query('DELETE FROM sessions WHERE id = ?', [sessionId]);
      return NextResponse.json({ message: 'Session cancelled' });
    }
  } catch (error) {
    console.error('Error cancelling session:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
