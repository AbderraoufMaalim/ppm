import { NextResponse } from 'next/server';
import { getAuthCookieOptions, COOKIE_NAME } from '@/lib/auth';

// POST /api/auth/logout — Déconnexion
export async function POST() {
  const response = NextResponse.json({ message: 'Déconnexion réussie' });

  // Supprimer le cookie en le mettant à expiry 0
  response.cookies.set({
    ...getAuthCookieOptions(0),
    value: '',
  });

  return response;
}
