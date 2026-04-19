import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { RowDataPacket } from 'mysql2';

// PATCH /api/orders/[id] — Mettre à jour la quantité
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const body = await request.json();
    const { quantity } = body;

    // Si la quantité est de 0 ou moins, on supprime carrément
    if (quantity <= 0) {
      await pool.query('DELETE FROM order_items WHERE id = ?', [id]);
      return NextResponse.json({ message: 'Order item deleted' }, { status: 200 });
    }

    const [result] = await pool.query(
      'UPDATE order_items SET quantity = ? WHERE id = ?',
      [quantity, id]
    );

    return NextResponse.json({ message: 'Quantity updated' }, { status: 200 });
  } catch (error) {
    console.error('Error updating order:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}

// DELETE /api/orders/[id] — Supprimer la ligne de commande
export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    await pool.query('DELETE FROM order_items WHERE id = ?', [id]);
    return NextResponse.json({ message: 'Order item deleted' }, { status: 200 });
  } catch (error) {
    console.error('Error deleting order:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
