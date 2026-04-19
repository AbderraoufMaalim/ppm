import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const sessionId = parseInt(id);

    // Ajouter 1 minute à start_time équivaut à ENLEVER 1 minute au chrono en direct
    await pool.query(
      `UPDATE sessions SET start_time = DATE_ADD(start_time, INTERVAL 1 MINUTE) WHERE id = ?`,
      [sessionId]
    );

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error removing minute:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
