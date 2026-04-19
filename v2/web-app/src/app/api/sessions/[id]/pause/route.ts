import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { ResultSetHeader, RowDataPacket } from 'mysql2';

// PATCH /api/sessions/[id]/pause — Met la session en pause
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;

  try {
    // Vérifier que la session est bien ACTIVE
    const [rows] = await pool.query<RowDataPacket[]>('SELECT status FROM sessions WHERE id = ?', [id]);
    if (rows.length === 0) return NextResponse.json({ error: 'Session introuvable' }, { status: 404 });
    if (rows[0].status !== 'ACTIVE') return NextResponse.json({ error: 'Session non active' }, { status: 400 });

    const [result] = await pool.query<ResultSetHeader>(
      `UPDATE sessions SET status = 'PAUSED', paused_at = NOW() WHERE id = ?`,
      [id]
    );

    if (result.affectedRows === 0) {
      return NextResponse.json({ error: 'Session introuvable' }, { status: 404 });
    }

    return NextResponse.json({ message: 'Session en pause' });
  } catch (error) {
    console.error('Error pausing session:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
