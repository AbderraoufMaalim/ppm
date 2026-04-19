import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket, ResultSetHeader } from 'mysql2';

// GET /api/orders — Lister les consos d'une session
export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const sessionId = searchParams.get('session_id');

    if (!sessionId) {
      return NextResponse.json({ error: 'session_id requis' }, { status: 400 });
    }

    const [rows] = await pool.query<RowDataPacket[]>(
      `SELECT oi.id, oi.product_id, oi.quantity, oi.unit_price, oi.tab_index, p.name as product_name, p.category, p.image_url 
       FROM order_items oi
       JOIN products p ON oi.product_id = p.id
       WHERE oi.session_id = ?
       ORDER BY oi.tab_index ASC, oi.id ASC`,
      [sessionId]
    );

    return NextResponse.json(rows);
  } catch (error) {
    console.error('Error fetching orders:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// POST /api/orders — Ajouter un produit consommé à une session active
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { session_id, product_id, quantity = 1, tab_index = 1 } = body;

    if (!session_id || !product_id) {
      return NextResponse.json({ error: 'session_id and product_id are required' }, { status: 400 });
    }

    // Vérifier que la session est active ou en pause
    const [sessions] = await pool.query<RowDataPacket[]>(
      'SELECT id FROM sessions WHERE id = ? AND status IN (?, ?)',
      [session_id, 'ACTIVE', 'PAUSED']
    );
    if (sessions.length === 0) {
      return NextResponse.json({ error: 'Active or Paused session not found' }, { status: 404 });
    }

    // Récupérer le prix actuel du produit
    const [products] = await pool.query<RowDataPacket[]>(
      'SELECT price FROM products WHERE id = ?',
      [product_id]
    );
    if (products.length === 0) {
      return NextResponse.json({ error: 'Product not found' }, { status: 404 });
    }

    const unitPrice = products[0].price;

    // Vérifier si le produit existe déjà pour cette session ET ce tab
    const [existingOrders] = await pool.query<RowDataPacket[]>(
      'SELECT id, quantity FROM order_items WHERE session_id = ? AND product_id = ? AND tab_index = ?',
      [session_id, product_id, tab_index]
    );

    if (existingOrders.length > 0) {
      // Mettre à jour la quantité
      const existingId = existingOrders[0].id;
      const newQuantity = existingOrders[0].quantity + quantity;
      
      await pool.query(
        'UPDATE order_items SET quantity = ? WHERE id = ?',
        [newQuantity, existingId]
      );
      
      return NextResponse.json({ id: existingId, message: 'Order quantity updated', unit_price: unitPrice }, { status: 200 });
    }

    // Sinon, insérer une nouvelle ligne
    const [result] = await pool.query<ResultSetHeader>(
      'INSERT INTO order_items (session_id, product_id, quantity, unit_price, tab_index) VALUES (?, ?, ?, ?, ?)',
      [session_id, product_id, quantity, unitPrice, tab_index]
    );

    return NextResponse.json({ id: result.insertId, message: 'Order added', unit_price: unitPrice }, { status: 201 });
  } catch (error) {
    console.error('Error adding order:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
