import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const { note, tab_index } = await request.json();
    
    if (typeof note !== 'string' || typeof tab_index !== 'number') {
      return NextResponse.json({ error: 'Données invalides' }, { status: 400 });
    }

    await pool.query(
      'INSERT INTO tab_notes (session_id, tab_index, note) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE note = VALUES(note)',
      [id, tab_index, note]
    );
    
    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error updating session note:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
