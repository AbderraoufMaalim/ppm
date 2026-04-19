import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { ResultSetHeader, RowDataPacket } from 'mysql2';

// PATCH /api/sessions/[id]/resume — Reprend une session en pause
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;

  try {
    // Vérifier que la session est bien PAUSED
    const [rows] = await pool.query<RowDataPacket[]>('SELECT status, paused_at FROM sessions WHERE id = ?', [id]);
    if (rows.length === 0) return NextResponse.json({ error: 'Session introuvable' }, { status: 404 });
    if (rows[0].status !== 'PAUSED' || !rows[0].paused_at) return NextResponse.json({ error: 'Session non en pause' }, { status: 400 });

    // Calculer la durée de la pause en secondes et l'ajouter accumulée
    const [result] = await pool.query<ResultSetHeader>(
      `UPDATE sessions 
       SET 
        status = 'ACTIVE', 
        pause_duration_seconds = pause_duration_seconds + TIMESTAMPDIFF(SECOND, paused_at, NOW()), 
        paused_at = NULL 
       WHERE id = ?`,
      [id]
    );

    if (result.affectedRows === 0) {
      return NextResponse.json({ error: 'Session introuvable' }, { status: 404 });
    }

    return NextResponse.json({ message: 'Session reprise' });
  } catch (error) {
    console.error('Error resuming session:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
