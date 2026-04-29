import { NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function PUT(req: Request, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const { name, type, default_rate_per_hour } = await req.json();

    if (!name || !type) {
      return NextResponse.json({ error: 'Name and type are required' }, { status: 400 });
    }

    const rate = default_rate_per_hour || 0;

    await pool.query<any>(
      'UPDATE stations SET name = ?, type = ?, default_rate_per_hour = ? WHERE id = ?',
      [name, type, rate, id]
    );

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error updating station:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

export async function DELETE(req: Request, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;

    // Check if there are active sessions before deleting
    const [sessions] = await pool.query<any>(
      "SELECT id FROM sessions WHERE station_id = ? AND status IN ('ACTIVE', 'PAUSED')",
      [id]
    );

    if (sessions.length > 0) {
      return NextResponse.json({ error: 'Cannot delete station with active sessions' }, { status: 400 });
    }

    await pool.query<any>('UPDATE stations SET is_active = false WHERE id = ?', [id]);

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error deleting station:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
