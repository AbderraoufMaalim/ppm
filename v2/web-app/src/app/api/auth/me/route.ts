import { NextResponse } from 'next/server';
import { getCurrentUser } from '@/lib/auth';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// GET /api/auth/me — Retourner l'utilisateur connecté
export async function GET() {
  const user = await getCurrentUser();

  if (!user) {
    return NextResponse.json(
      { error: 'Non authentifié' },
      { status: 401 }
    );
  }

  try {
    const [rows] = await pool.query<RowDataPacket[]>('SELECT image_url FROM users WHERE id = ?', [user.userId]);
    const imageUrl = rows.length > 0 ? rows[0].image_url : null;

    return NextResponse.json({
      id: user.userId,
      username: user.username,
      role: user.role,
      image_url: imageUrl,
    });
  } catch (error) {
    return NextResponse.json(
      { error: 'Erreur serveur' },
      { status: 500 }
    );
  }
}
