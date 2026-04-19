import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';
import bcrypt from 'bcryptjs';
import { requireAdmin } from '@/lib/auth';
import { writeFile } from 'fs/promises';
import { join } from 'path';

// GET /api/users — Liste tous les utilsateurs (Admin uniquement)
export async function GET() {
  const admin = await requireAdmin();
  if (!admin) return NextResponse.json({ error: 'Accès non autorisé' }, { status: 403 });

  try {
    const [rows] = await pool.query<RowDataPacket[]>('SELECT id, username, role, image_url FROM users');
    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching users:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// POST /api/users — Créer un nouvel utilisateur avec photo optionnelle
export async function POST(request: NextRequest) {
  const admin = await requireAdmin();
  if (!admin) return NextResponse.json({ error: 'Accès non autorisé' }, { status: 403 });

  try {
    const formData = await request.formData();
    const username = formData.get('username')?.toString();
    const password = formData.get('password')?.toString();
    const role = formData.get('role')?.toString() || 'CAISSIER';
    const image = formData.get('image') as File | null;

    if (!username || !password) {
      return NextResponse.json({ error: 'Username et password requis' }, { status: 400 });
    }

    // Hash password
    const passwordHash = await bcrypt.hash(password, 10);
    let imageUrl = null;

    // Handle image upload if provided
    if (image && image.size > 0) {
      const bytes = await image.arrayBuffer();
      const buffer = Buffer.from(bytes);
      
      const fileExt = image.name.split('.').pop() || 'png';
      const fileName = `${Date.now()}-${Math.random().toString(36).substring(7)}.${fileExt}`;
      const uploadDir = join(process.cwd(), 'public/uploads/profiles');
      const filePath = join(uploadDir, fileName);
      
      await writeFile(filePath, buffer);
      imageUrl = `/uploads/profiles/${fileName}`;
    }

    const [result] = await pool.query<ResultSetHeader>(
      'INSERT INTO users (username, password_hash, role, image_url) VALUES (?, ?, ?, ?)',
      [username, passwordHash, role, imageUrl]
    );

    return NextResponse.json({ id: result.insertId, message: 'Utilisateur créé' }, { status: 201 });
  } catch (error: any) {
    console.error('Error creating user:', error);
    if (error.code === 'ER_DUP_ENTRY') {
      return NextResponse.json({ error: 'Ce nom d\'utilisateur existe déjà' }, { status: 409 });
    }
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
