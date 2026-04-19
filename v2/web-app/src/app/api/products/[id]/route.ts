import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { ResultSetHeader, RowDataPacket } from 'mysql2';
import { writeFile } from 'fs/promises';
import { join } from 'path';

// PATCH /api/products/[id] — Mettre à jour un produit
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const productId = parseInt(id);

    // Vérifier l'existence
    const [existing] = await pool.query<RowDataPacket[]>('SELECT * FROM products WHERE id = ? AND is_active = TRUE', [productId]);
    if (existing.length === 0) return NextResponse.json({ error: 'Produit introuvable' }, { status: 404 });

    const formData = await request.formData();
    
    // Construire dynamiquement les champs à mettre à jour
    const updates: string[] = [];
    const values: any[] = [];

    const name = formData.get('name')?.toString();
    if (name) { updates.push('name = ?'); values.push(name); }

    const category = formData.get('category')?.toString();
    if (category) { updates.push('category = ?'); values.push(category); }

    const priceStr = formData.get('price')?.toString();
    if (priceStr) { updates.push('price = ?'); values.push(parseFloat(priceStr)); }

    const stockStr = formData.get('stock_quantity')?.toString();
    if (stockStr) { updates.push('stock_quantity = ?'); values.push(parseInt(stockStr)); }

    const image = formData.get('image') as File | null;
    if (image && image.size > 0) {
      const bytes = await image.arrayBuffer();
      const buffer = Buffer.from(bytes);
      
      const fileExt = image.name.split('.').pop() || 'png';
      const fileName = `prod_${Date.now()}_${Math.random().toString(36).substring(7)}.${fileExt}`;
      const uploadDir = join(process.cwd(), 'public/uploads/products');
      const filePath = join(uploadDir, fileName);
      
      await writeFile(filePath, buffer).catch(async (e) => {
        if (e.code === 'ENOENT') {
          const { mkdir } = require('fs/promises');
          await mkdir(uploadDir, { recursive: true });
          await writeFile(filePath, buffer);
        } else throw e;
      });
      
      updates.push('image_url = ?');
      values.push(`/uploads/products/${fileName}`);
    }

    if (updates.length > 0) {
      values.push(productId);
      const query = `UPDATE products SET ${updates.join(', ')} WHERE id = ?`;
      await pool.query<ResultSetHeader>(query, values);
    }

    return NextResponse.json({ message: 'Produit mis à jour' });
  } catch (error) {
    console.error('Error updating product:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// DELETE /api/products/[id] — Suppression douce (soft delete)
export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const productId = parseInt(id);

    const [result] = await pool.query<ResultSetHeader>(
      'UPDATE products SET is_active = FALSE WHERE id = ?',
      [productId]
    );

    if (result.affectedRows === 0) {
      return NextResponse.json({ error: 'Produit introuvable' }, { status: 404 });
    }

    return NextResponse.json({ message: 'Produit supprimé du catalogue' });
  } catch (error) {
    console.error('Error deleting product:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
