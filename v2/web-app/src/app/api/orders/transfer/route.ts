import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// POST /api/orders/transfer
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { order_id, transfer_quantity, target_tab } = body;
    
    if (!order_id || !transfer_quantity || !target_tab || transfer_quantity <= 0) {
      return NextResponse.json({ error: 'Paramètres invalides' }, { status: 400 });
    }
    
    // 1. Get the source order
    const [orders] = await pool.query<RowDataPacket[]>('SELECT * FROM order_items WHERE id = ?', [order_id]);
    if (orders.length === 0) return NextResponse.json({ error: 'Order not found' }, { status: 404 });
    const sourceOrder = orders[0];
    
    if (transfer_quantity > sourceOrder.quantity) {
      return NextResponse.json({ error: 'Quantité à transférer supérieure à la quantité existante' }, { status: 400 });
    }
    
    const isFullTransfer = (transfer_quantity === sourceOrder.quantity);
    
    const targetSessionId = body.target_session_id || sourceOrder.session_id;

    // 2. Check if target tab already has this product (we don't merge GAME_TIME, keep them separate)
    let targetOrderId = null;
    const [gameProducts] = await pool.query<RowDataPacket[]>("SELECT id FROM products WHERE category = 'GAME_TIME' LIMIT 1");
    const gameProductId = gameProducts.length > 0 ? gameProducts[0].id : null;

    if (sourceOrder.product_id !== gameProductId) {
      const [existingTarget] = await pool.query<RowDataPacket[]>(
        'SELECT id, quantity FROM order_items WHERE session_id = ? AND product_id = ? AND tab_index = ?',
        [targetSessionId, sourceOrder.product_id, target_tab]
      );
      if (existingTarget.length > 0) {
        targetOrderId = existingTarget[0].id;
      }
    }
    
    if (targetOrderId) {
      // Merge into existing target item
      await pool.query('UPDATE order_items SET quantity = quantity + ? WHERE id = ?', [transfer_quantity, targetOrderId]);
      if (isFullTransfer) {
        await pool.query('DELETE FROM order_items WHERE id = ?', [order_id]);
      } else {
        await pool.query('UPDATE order_items SET quantity = quantity - ? WHERE id = ?', [transfer_quantity, order_id]);
      }
    } else {
      // No existing target item
      if (isFullTransfer && targetSessionId === sourceOrder.session_id) {
        // Just move the item to the new tab within the same session
        await pool.query('UPDATE order_items SET tab_index = ? WHERE id = ?', [target_tab, order_id]);
      } else {
        // Insert new row for target tab, decrement/delete source
        await pool.query(
          'INSERT INTO order_items (session_id, product_id, quantity, unit_price, tab_index) VALUES (?, ?, ?, ?, ?)',
          [targetSessionId, sourceOrder.product_id, transfer_quantity, sourceOrder.unit_price, target_tab]
        );
        if (isFullTransfer) {
           await pool.query('DELETE FROM order_items WHERE id = ?', [order_id]);
        } else {
           await pool.query('UPDATE order_items SET quantity = quantity - ? WHERE id = ?', [transfer_quantity, order_id]);
        }
      }
    }
    
    return NextResponse.json({ success: true }, { status: 200 });
  } catch (error) {
    console.error('Error transferring order:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
