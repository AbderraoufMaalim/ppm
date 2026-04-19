import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    
    const [rows] = await pool.query('SELECT tab_index, note FROM tab_notes WHERE session_id = ?', [id]);
    
    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching session notes:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
