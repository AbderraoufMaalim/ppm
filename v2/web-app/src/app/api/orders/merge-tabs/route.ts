import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// POST /api/orders/merge-tabs
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { session_id, target_tab, source_tabs } = body;

    if (!session_id || !target_tab || !source_tabs || !Array.isArray(source_tabs) || source_tabs.length === 0) {
      return NextResponse.json({ error: 'Paramètres manquants ou invalides' }, { status: 400 });
    }

    // 1. Move all items from source_tabs into target_tab
    const placeholders = source_tabs.map(() => '?').join(',');
    await pool.query(
      `UPDATE order_items SET tab_index = ? WHERE session_id = ? AND tab_index IN (${placeholders})`,
      [target_tab, session_id, ...source_tabs]
    );
    // Delete notes for merged tabs (since they are folded into the target tab)
    await pool.query(
      `DELETE FROM tab_notes WHERE session_id = ? AND tab_index IN (${placeholders})`,
      [session_id, ...source_tabs]
    );

    // 2. Re-index all tabs to close any gaps created by the merge
    const [tabs] = await pool.query<RowDataPacket[]>(
      'SELECT DISTINCT tab_index FROM order_items WHERE session_id = ? ORDER BY tab_index ASC',
      [session_id]
    );

    for (let i = 0; i < tabs.length; i++) {
      const currentIdx = tabs[i].tab_index;
      const expectedIdx = i + 1;
      if (currentIdx !== expectedIdx) {
        await pool.query(
          'UPDATE order_items SET tab_index = ? WHERE session_id = ? AND tab_index = ?',
          [expectedIdx, session_id, currentIdx]
        );
        await pool.query(
          'UPDATE tab_notes SET tab_index = ? WHERE session_id = ? AND tab_index = ?',
          [expectedIdx, session_id, currentIdx]
        );
      }
    }

    return NextResponse.json({ message: 'Tabs merged and re-indexed successfully' }, { status: 200 });
  } catch (error) {
    console.error('Error merging tabs:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
