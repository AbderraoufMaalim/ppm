import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';

// DELETE /api/orders/tab — Supprime un onglet et décale les onglets suivants
export async function DELETE(request: NextRequest) {
  try {
    const body = await request.json();
    const { session_id, tab_index } = body;

    if (!session_id || !tab_index) {
      return NextResponse.json({ error: 'Paramètres manquants' }, { status: 400 });
    }

    // 1. Supprimer tous les articles et la note de cet onglet
    await pool.query('DELETE FROM order_items WHERE session_id = ? AND tab_index = ?', [session_id, tab_index]);
    await pool.query('DELETE FROM tab_notes WHERE session_id = ? AND tab_index = ?', [session_id, tab_index]);

    // 2. Décaler tous les onglets supérieurs pour combler le trou
    await pool.query('UPDATE order_items SET tab_index = tab_index - 1 WHERE session_id = ? AND tab_index > ?', [session_id, tab_index]);
    await pool.query('UPDATE tab_notes SET tab_index = tab_index - 1 WHERE session_id = ? AND tab_index > ?', [session_id, tab_index]);

    return NextResponse.json({ message: 'Tab deleted and shifted' }, { status: 200 });
  } catch (error) {
    console.error('Error deleting tab:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
