import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';

export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const sessionId = parseInt(id);
    const { tabsToPay, close_session } = await request.json();

    if (!tabsToPay || !Array.isArray(tabsToPay)) {
      return NextResponse.json({ error: 'tabsToPay manquant' }, { status: 400 });
    }

    if (tabsToPay.length > 0) {
      const placeholders = tabsToPay.map(() => '?').join(',');
      
      await pool.query(`DELETE FROM order_items WHERE session_id = ? AND tab_index IN (${placeholders})`, [sessionId, ...tabsToPay]);
      await pool.query(`DELETE FROM tab_notes WHERE session_id = ? AND tab_index IN (${placeholders})`, [sessionId, ...tabsToPay]);

      // Re-index remaining tabs
      const [remainingTabs] = await pool.query<RowDataPacket[]>(
        'SELECT DISTINCT tab_index FROM order_items WHERE session_id = ? ORDER BY tab_index ASC',
        [sessionId]
      );
      
      for (let i = 0; i < remainingTabs.length; i++) {
        const currentIdx = remainingTabs[i].tab_index;
        const expectedIdx = i + 1;
        if (currentIdx !== expectedIdx) {
          await pool.query('UPDATE order_items SET tab_index = ? WHERE session_id = ? AND tab_index = ?', [expectedIdx, sessionId, currentIdx]);
          await pool.query('UPDATE tab_notes SET tab_index = ? WHERE session_id = ? AND tab_index = ?', [expectedIdx, sessionId, currentIdx]);
        }
      }
    }

    if (close_session) {
      await pool.query<ResultSetHeader>(
        `UPDATE sessions SET end_time = NOW(), status = 'CLOSED' WHERE id = ?`,
        [sessionId]
      );
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error during checkout:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
