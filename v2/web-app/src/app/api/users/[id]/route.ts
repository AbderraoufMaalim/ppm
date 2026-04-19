import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';
import bcrypt from 'bcryptjs';
import { requireAdmin } from '@/lib/auth';

// DELETE /api/users/[id] — Supprimer un utilisateur (nécessite le mot de passe admin)
export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> } // Next.js 15+ needs await on params or treat it like this
) {
  const admin = await requireAdmin();
  const { id } = await params;
  if (!admin) return NextResponse.json({ error: 'Accès non autorisé' }, { status: 403 });

  if (admin.userId.toString() === id) {
    return NextResponse.json({ error: 'Vous ne pouvez pas vous supprimer vous-même !' }, { status: 400 });
  }

  try {
    const body = await request.json();
    const { adminPassword } = body;

    if (!adminPassword) {
      return NextResponse.json({ error: 'Mot de passe requis' }, { status: 400 });
    }

    // Vérifier le mot de passe de l'admin
    const [adminRows] = await pool.query<RowDataPacket[]>('SELECT password_hash FROM users WHERE id = ?', [admin.userId]);
    if (adminRows.length === 0) return NextResponse.json({ error: 'Admin introuvable' }, { status: 404 });

    const passwordValid = await bcrypt.compare(adminPassword, adminRows[0].password_hash);
    if (!passwordValid) {
      return NextResponse.json({ error: 'Mot de passe incorrect' }, { status: 401 });
    }

    await pool.query('DELETE FROM users WHERE id = ?', [id]);
    
    return NextResponse.json({ message: 'Utilisateur supprimé' });
  } catch (error) {
    console.error('Error deleting user:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// PATCH /api/users/[id] — Modifier un utilisateur
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const admin = await requireAdmin();
  const { id } = await params;
  if (!admin) return NextResponse.json({ error: 'Accès non autorisé' }, { status: 403 });

  try {
    const formData = await request.formData();
    const username = formData.get('username')?.toString();
    const role = formData.get('role')?.toString();
    const password = formData.get('password')?.toString();
    const adminPassword = formData.get('adminPassword')?.toString();
    const image = formData.get('image') as File | null;

    if (!username || !role || !adminPassword) {
      return NextResponse.json({ error: 'Username, rôle et mot de passe admin requis' }, { status: 400 });
    }

    // Vérifier le mot de passe de l'admin
    const [adminRows] = await pool.query<RowDataPacket[]>('SELECT password_hash FROM users WHERE id = ?', [admin.userId]);
    if (adminRows.length === 0) return NextResponse.json({ error: 'Admin introuvable' }, { status: 404 });

    const passwordValid = await bcrypt.compare(adminPassword, adminRows[0].password_hash);
    if (!passwordValid) {
      return NextResponse.json({ error: 'Mot de passe administrateur incorrect' }, { status: 401 });
    }

    // Gérer l'upload d'image si fournie
    let imageUrl: string | null = null;
    if (image && image.size > 0) {
      const { writeFile } = await import('fs/promises');
      const { join } = await import('path');
      const bytes = await image.arrayBuffer();
      const buffer = Buffer.from(bytes);
      
      const fileExt = image.name.split('.').pop() || 'png';
      const fileName = `${Date.now()}-${Math.random().toString(36).substring(7)}.${fileExt}`;
      const uploadDir = join(process.cwd(), 'public/uploads/profiles');
      const filePath = join(uploadDir, fileName);
      
      await writeFile(filePath, buffer);
      imageUrl = `/uploads/profiles/${fileName}`;
    }

    let query = 'UPDATE users SET username = ?, role = ?';
    const values: any[] = [username, role];

    if (password && password.trim() !== '') {
      const passwordHash = await bcrypt.hash(password, 10);
      query += ', password_hash = ?';
      values.push(passwordHash);
    }

    if (imageUrl) {
      query += ', image_url = ?';
      values.push(imageUrl);
    }

    query += ' WHERE id = ?';
    values.push(id);

    await pool.query(query, values);

    return NextResponse.json({ message: 'Utilisateur mis à jour' });
  } catch (error: any) {
    console.error('Error updating user:', error);
    if (error.code === 'ER_DUP_ENTRY') {
      return NextResponse.json({ error: 'Ce nom d\'utilisateur existe déjà' }, { status: 409 });
    }
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
