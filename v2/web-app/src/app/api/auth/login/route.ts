import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';
import bcrypt from 'bcryptjs';
import { signToken, getAuthCookieOptions } from '@/lib/auth';

// POST /api/auth/login — Connexion utilisateur
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { username, password } = body;

    if (!username || !password) {
      return NextResponse.json(
        { error: 'Nom d\'utilisateur et mot de passe requis' },
        { status: 400 }
      );
    }

    // Trouver l'utilisateur
    const [users] = await pool.query<RowDataPacket[]>(
      'SELECT id, username, password_hash, role FROM users WHERE username = ?',
      [username]
    );

    if (users.length === 0) {
      return NextResponse.json(
        { error: 'Identifiants incorrects' },
        { status: 401 }
      );
    }

    const user = users[0];

    // Vérifier le mot de passe
    const passwordValid = await bcrypt.compare(password, user.password_hash);
    if (!passwordValid) {
      return NextResponse.json(
        { error: 'Identifiants incorrects' },
        { status: 401 }
      );
    }

    // Générer le JWT
    const token = signToken({
      userId: user.id,
      username: user.username,
      role: user.role,
    });

    // Retourner le token dans un cookie httpOnly
    const response = NextResponse.json({
      message: 'Connexion réussie',
      user: {
        id: user.id,
        username: user.username,
        role: user.role,
      },
    });

    const cookieOptions = getAuthCookieOptions();
    response.cookies.set({
      ...cookieOptions,
      value: token,
    });

    return response;
  } catch (error) {
    console.error('Login error:', error);
    return NextResponse.json(
      { error: 'Erreur interne du serveur' },
      { status: 500 }
    );
  }
}
