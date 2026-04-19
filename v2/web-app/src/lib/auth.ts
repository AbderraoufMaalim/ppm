import jwt from 'jsonwebtoken';
import { cookies } from 'next/headers';

const JWT_SECRET = process.env.JWT_SECRET || 'ppm-v2-secret-key-change-me-in-prod';
const COOKIE_NAME = 'ppm_token';
const TOKEN_EXPIRY = '24h';

export interface JwtPayload {
  userId: number;
  username: string;
  role: string;
}

/** Sign a JWT token */
export function signToken(payload: JwtPayload): string {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: TOKEN_EXPIRY });
}

/** Verify and decode a JWT token */
export function verifyToken(token: string): JwtPayload | null {
  try {
    return jwt.verify(token, JWT_SECRET) as JwtPayload;
  } catch {
    return null;
  }
}

/** Get the current user from cookies (server-side) */
export async function getCurrentUser(): Promise<JwtPayload | null> {
  const cookieStore = await cookies();
  const token = cookieStore.get(COOKIE_NAME)?.value;
  if (!token) return null;
  return verifyToken(token);
}

/** Cookie options for setting/clearing the auth cookie */
export function getAuthCookieOptions(maxAge?: number) {
  return {
    name: COOKIE_NAME,
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax' as const,
    path: '/',
    maxAge: maxAge ?? 60 * 60 * 24, // 24 hours
  };
}

/** Check if the current user has ADMIN role */
export async function requireAdmin(): Promise<JwtPayload | null> {
  const user = await getCurrentUser();
  if (!user || user.role !== 'ADMIN') return null;
  return user;
}

export { COOKIE_NAME };
