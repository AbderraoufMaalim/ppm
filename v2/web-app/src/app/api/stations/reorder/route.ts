import { NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function POST(req: Request) {
  try {
    const { updates } = await req.json(); // expected array: [{ id, display_order }]

    if (!Array.isArray(updates)) {
      return NextResponse.json({ error: 'Expected an array of updates' }, { status: 400 });
    }

    // Using a transaction for safety
    const connection = await pool.getConnection();
    try {
      await connection.beginTransaction();
      for (const update of updates) {
        await connection.query('UPDATE stations SET display_order = ? WHERE id = ?', [update.display_order, update.id]);
      }
      await connection.commit();
    } catch (e) {
      await connection.rollback();
      throw e;
    } finally {
      connection.release();
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error updating stations order:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
