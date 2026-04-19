import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';
import { writeFile } from 'fs/promises';
import { join } from 'path';

// GET /api/products — Liste de tous les produits actifs
export async function GET() {
  try {
    const [rows] = await pool.query<RowDataPacket[]>(
      'SELECT * FROM products WHERE is_active = TRUE ORDER BY category, name'
    );
    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching products:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// POST /api/products — Créer un produit avec photo optionnelle
export async function POST(request: NextRequest) {
  try {
    const formData = await request.formData();
    const name = formData.get('name')?.toString();
    const category = formData.get('category')?.toString() || 'AUTRE';
    const priceStr = formData.get('price')?.toString();
    const stockStr = formData.get('stock_quantity')?.toString() || '0';
    const image = formData.get('image') as File | null;

    if (!name || !priceStr) {
      return NextResponse.json({ error: 'Nom et prix requis' }, { status: 400 });
    }

    const price = parseFloat(priceStr);
    const stock = parseInt(stockStr);
    let imageUrl = null;

    // Traitement de l'image si fournie
    if (image && image.size > 0) {
      const bytes = await image.arrayBuffer();
      const buffer = Buffer.from(bytes);
      
      const fileExt = image.name.split('.').pop() || 'png';
      const fileName = `prod_${Date.now()}_${Math.random().toString(36).substring(7)}.${fileExt}`;
      const uploadDir = join(process.cwd(), 'public/uploads/products');
      const filePath = join(uploadDir, fileName);
      
      // Assurer que le dossier existe en vrai (Next.js le créera ou il le faut avant)
      await writeFile(filePath, buffer).catch(async (e) => {
        if (e.code === 'ENOENT') {
          const { mkdir } = require('fs/promises');
          await mkdir(uploadDir, { recursive: true });
          await writeFile(filePath, buffer);
        } else throw e;
      });
      imageUrl = `/uploads/products/${fileName}`;
    }

    const [result] = await pool.query<ResultSetHeader>(
      'INSERT INTO products (name, category, price, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?)',
      [name, category, price, stock, imageUrl]
    );

    return NextResponse.json({ id: result.insertId, message: 'Produit créé' }, { status: 201 });
  } catch (error: any) {
    console.error('Error creating product:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
