'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';

// ─── Types ───────────────────────────────────────────
interface Station {
  id: number;
  name: string;
  type: string;
  default_rate_per_hour: number;
  is_active: boolean;
  active_session_id: number | null;
  session_start: string | null;
  client_id: number | null;
  session_status?: string | null;
  session_note?: string | null;
  paused_at?: string | null;
  pause_duration_seconds?: number;
  live_active_seconds?: number;
  fetched_at?: number;
  orders_total_cost?: number;
  transferred_minutes?: number;
}

interface DashboardStats {
  daily_revenue: number;
  sessions_today: number;
  active_sessions: number;
  total_stations: number;
  revenue_by_type: { type: string; revenue: number }[];
  weekly_revenue: { day: string; revenue: number }[];
}

interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  stock_quantity: number;
  image_url: string | null;
  is_active: boolean;
}

interface OrderItem {
  id: number;
  product_id: number;
  quantity: number;
  unit_price: number;
  product_name: string;
  category: string;
  image_url: string | null;
  tab_index: number;
}

interface UserAccount {
  id: number;
  username: string;
  role: string;
  image_url: string | null;
}

interface Toast {
  message: string;
  type: 'success' | 'error';
}

// ─── Helpers ─────────────────────────────────────────
function getStationIcon(type: string): React.ReactNode {
  const imgStyle: React.CSSProperties = { width: '1.2em', height: '1.2em', display: 'inline-block', verticalAlign: 'middle', marginTop: '-0.2em' };
  switch (type) {
    case 'PC': return '🖥️';
    case 'PS_NORMAL': return <img src="/icons/ps_normal.png" alt="PS" style={imgStyle} />;
    case 'PS_MULTI': return <img src="/icons/ps_multi.png" alt="PS Multi" style={imgStyle} />;
    case 'TABLE': return '🪑';
    case 'CHAIR': return '☕';
    default: return '📺';
  }
}

function getStationLabel(type: string): string {
  switch (type) {
    case 'PC': return 'PC Gamer';
    case 'PS_NORMAL': return 'PlayStation';
    case 'PS_MULTI': return 'PS Multi';
    case 'TABLE': return 'Table';
    case 'CHAIR': return 'Chaise';
    default: return type;
  }
}

function getActiveMs(station: Station): number {
  if (!station.active_session_id || station.live_active_seconds == null) return 0;
  
  let activeMs = Number(station.live_active_seconds) * 1000;
  
  // If the session is currently active, we add the time passed since we fetched the data
  if (station.session_status === 'ACTIVE' && station.fetched_at) {
    const driftMs = Math.max(0, Date.now() - station.fetched_at);
    activeMs += driftMs;
  }
  
  return Math.max(0, activeMs);
}

function formatDuration(station: Station): string {
  if (!station.active_session_id) return "00:00:00";
  const diff = getActiveMs(station);
  const hours = Math.floor(diff / 3600000);
  const minutes = Math.floor((diff % 3600000) / 60000);
  const seconds = Math.floor((diff % 60000) / 1000);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

function calculateCost(station: Station): number {
  if (!station.active_session_id) return 0;
  const diffMinutes = getActiveMs(station) / 60000;
  return Math.round((diffMinutes / 60) * station.default_rate_per_hour * 100) / 100;
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('fr-DZ', { 
    minimumFractionDigits: 0,
    maximumFractionDigits: 0 
  }).format(Math.round(amount));
}

// ─── Main App Component ──────────────────────────────
export default function Home() {
  const router = useRouter();
  const [user, setUser] = useState<{username: string, role: string, image_url?: string} | null>(null);
  const [currentPage, setCurrentPage] = useState<'dashboard' | 'products' | 'team'>('dashboard');
  const [stations, setStations] = useState<Station[]>([]);
  const [dashboardStats, setDashboardStats] = useState<DashboardStats | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [teamUsers, setTeamUsers] = useState<UserAccount[]>([]);
  const [clock, setClock] = useState('');
  const [toast, setToast] = useState<Toast | null>(null);
  const [loading, setLoading] = useState(true);
  const [dbConnected, setDbConnected] = useState(true);
  
  // Modal states
  const [selectedStation, setSelectedStation] = useState<Station | null>(null);
  const [stationOrders, setStationOrders] = useState<OrderItem[]>([]);
  const [showStopModal, setShowStopModal] = useState(false); // Checkout
  const [showCancelModal, setShowCancelModal] = useState(false); // Cancel
  const [showProductModal, setShowProductModal] = useState(false); // Modal for adding a consumption
  const [productSearchQuery, setProductSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState(1);
  const [tabCount, setTabCount] = useState(1);
  const [showGameTimeModal, setShowGameTimeModal] = useState(false);
  const [gameTimeMinutes, setGameTimeMinutes] = useState(30);
  
  // Merge tabs state
  const [isMergeMode, setIsMergeMode] = useState(false);
  const [tabsToMerge, setTabsToMerge] = useState<number[]>([]);
  
  // Transfer state
  const [transferList, setTransferList] = useState<{ order: OrderItem, quantity: number }[]>([]);
  const [showTransferQtyModal, setShowTransferQtyModal] = useState<{ order: OrderItem } | null>(null);
  const [transferQtyInput, setTransferQtyInput] = useState(1);
  const [showExternalTransferModal, setShowExternalTransferModal] = useState(false);
  const [externalTargetStation, setExternalTargetStation] = useState<Station | null>(null);
  const [externalTargetTabs, setExternalTargetTabs] = useState<number[]>([]);
  
  // Note state
  const [showNoteModal, setShowNoteModal] = useState(false);
  const [noteInput, setNoteInput] = useState('');
  const [sessionNotes, setSessionNotes] = useState<Record<number, string>>({});
  const [isNoteHovered, setIsNoteHovered] = useState(false);
  
  // Backoffice users
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const [showEditUserModal, setShowEditUserModal] = useState(false);
  const [showDeleteUserModal, setShowDeleteUserModal] = useState(false);
  const [selectedTeamUser, setSelectedTeamUser] = useState<UserAccount | null>(null);

  // Backoffice products
  const [showAddProductCatalogModal, setShowAddProductCatalogModal] = useState(false);
  const [showEditProductCatalogModal, setShowEditProductCatalogModal] = useState(false);
  const [showDeleteProductCatalogModal, setShowDeleteProductCatalogModal] = useState(false);
  const [selectedCatalogProduct, setSelectedCatalogProduct] = useState<Product | null>(null);

  // Checkout state
  const [showChronoAlert, setShowChronoAlert] = useState(false);
  const [tabsToPay, setTabsToPay] = useState<number[]>([]);

  const [stopResult, setStopResult] = useState<{
    is_partial?: boolean;
    station_name?: string;
    paid_tabs?: number[];
    duration_minutes?: number;
    base_cost?: number;
    orders_total: number;
    total_cost: number;
    consos: { name: string; quantity: number; unit_price: number }[];
    gameTimes: { quantity: number; unit_price: number }[];
    rate_per_hour?: number;
  } | null>(null);

  // Timer tick (updates every second for live timers)
  const [, setTick] = useState(0);

  const showToast = useCallback((message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  }, []);

  // Fetch data
  const fetchStations = useCallback(async () => {
    try {
      const res = await fetch('/api/stations');
      if (!res.ok) throw new Error('Failed');
      const data = await res.json();
      const now = Date.now();
      setStations(data.map((s: any) => ({ ...s, fetched_at: now })));
      setDbConnected(true);
    } catch {
      setDbConnected(false);
    }
  }, []);

  const fetchDashboard = useCallback(async () => {
    try {
      const res = await fetch('/api/dashboard');
      if (!res.ok) throw new Error('Failed');
      const data = await res.json();
      setDashboardStats(data);
    } catch {
      // silently fail
    }
  }, []);

  const fetchProducts = useCallback(async () => {
    try {
      const res = await fetch('/api/products');
      if (!res.ok) throw new Error('Failed');
      const data = await res.json();
      setProducts(data);
    } catch {
      // silently fail
    }
  }, []);

  const fetchTeam = useCallback(async () => {
    try {
      const res = await fetch('/api/users');
      if (res.ok) {
        const data = await res.json();
        setTeamUsers(data);
      }
    } catch {
      // Auth error or network
    }
  }, []);

  const fetchOrders = useCallback(async (sessionId: number) => {
    try {
      const [resOrders, resNotes] = await Promise.all([
        fetch(`/api/orders?session_id=${sessionId}`),
        fetch(`/api/sessions/${sessionId}/notes`)
      ]);
      if (resOrders.ok) {
        const data: OrderItem[] = await resOrders.json();
        setStationOrders(data);
        // Auto-detect tab count from existing orders
        const maxTab = data.reduce((max, o) => Math.max(max, o.tab_index || 1), 1);
        setTabCount(prev => Math.max(prev, maxTab));
      }
      if (resNotes.ok) {
        const notesData = await resNotes.json();
        const notesMap: Record<number, string> = {};
        if (Array.isArray(notesData)) {
          notesData.forEach((n: any) => notesMap[n.tab_index] = n.note);
        }
        setSessionNotes(notesMap);
      }
    } catch {
      setStationOrders([]);
    }
  }, []);

  const fetchUser = useCallback(async () => {
    try {
      const res = await fetch('/api/auth/me');
      if (!res.ok) {
        if (res.status === 401) {
          router.push('/login');
        }
        throw new Error('Failed');
      }
      const data = await res.json();
      setUser(data);
    } catch {
      // silently fail
    }
  }, [router]);

  // Initial load
  useEffect(() => {
    const init = async () => {
      setLoading(true);
      await Promise.all([fetchUser(), fetchStations(), fetchDashboard(), fetchProducts()]);
      setLoading(false);
    };
    init();
  }, [fetchUser, fetchStations, fetchDashboard, fetchProducts]);

  // If user is ADMIN, also fetch team
  useEffect(() => {
    if (user?.role === 'ADMIN') {
      fetchTeam();
    }
  }, [user, fetchTeam]);

  // Auto-refresh every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      fetchStations();
      fetchDashboard();
    }, 30000);
    return () => clearInterval(interval);
  }, [fetchStations, fetchDashboard]);

  // Fetch orders when a station with an active session is selected
  useEffect(() => {
    if (selectedStation?.active_session_id) {
      fetchOrders(selectedStation.active_session_id);
    } else {
      setStationOrders([]);
    }
  }, [selectedStation?.active_session_id, fetchOrders]);

  // Keep selectedStation in sync with stations updates (like status changes, timer resets)
  useEffect(() => {
    if (selectedStation) {
      const updated = stations.find(s => s.id === selectedStation.id);
      if (updated && updated.fetched_at !== selectedStation.fetched_at) {
        setSelectedStation(updated);
      }
    } else {
      // Reset tabs when modal closes
      setActiveTab(1);
      setTabCount(1);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [stations, selectedStation]);

  // Live clock
  useEffect(() => {
    const updateClock = () => {
      const now = new Date();
      setClock(now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    };
    updateClock();
    const interval = setInterval(updateClock, 1000);
    return () => clearInterval(interval);
  }, []);

  // Tick for live session timers (10 times a second for flawless visual smoothness)
  useEffect(() => {
    const interval = setInterval(() => setTick(t => t + 1), 100);
    return () => clearInterval(interval);
  }, []);

  // ─── Actions ─────────────────────────────────────
  const startSession = async (stationId: number) => {
    try {
      const res = await fetch('/api/sessions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ station_id: stationId }),
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.error);
      }
      showToast('Session démarrée ! ⏱️', 'success');
      setSelectedStation(null);
      fetchStations();
      fetchDashboard();
    } catch (error) {
      showToast(`Erreur : ${error instanceof Error ? error.message : 'Inconnue'}`, 'error');
    }
  };

  const convertChrono = async (sessionId: number) => {
    try {
      const res = await fetch(`/api/sessions/${sessionId}/convert-chrono`, { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tab_index: activeTab })
      });
      if (!res.ok) throw new Error('Failed to convert chrono');
      await fetchOrders(sessionId);
      await fetchStations();
      setTabsToPay(Array.from({ length: tabCount }).map((_, i) => i + 1));
      setShowChronoAlert(false);
      setShowStopModal(true);
    } catch {
      showToast('Erreur lors de la conversion du chronomètre', 'error');
    }
  };

  const handleValiderClick = () => {
    if (!selectedStation) return;
    const isChronoRunning = selectedStation.session_status === 'ACTIVE' || selectedStation.session_status === 'PAUSED';
    const activeChronoCost = calculateCost(selectedStation);
    
    if (isChronoRunning && activeChronoCost > 0) {
      setShowChronoAlert(true);
    } else {
      setTabsToPay(Array.from({ length: tabCount }).map((_, i) => i + 1));
      setShowStopModal(true);
    }
  };

  const executeCheckout = async (sessionId: number, selectedTabs: number[]) => {
    try {
      // Calculate totals for the receipt before deleting
      const ordersToPay = stationOrders.filter(o => selectedTabs.includes(o.tab_index));
      const ordersTotal = ordersToPay.reduce((sum, o) => sum + Number(o.unit_price) * o.quantity, 0);

      // A session is closed ONLY if ALL tabs are paid AND the chrono is NOT running (cost=0)
      const isChronoRunning = selectedStation?.session_status === 'ACTIVE' || selectedStation?.session_status === 'PAUSED';
      const activeChronoCost = selectedStation ? calculateCost(selectedStation) : 0;
      const close_session = selectedTabs.length === tabCount && (!isChronoRunning || activeChronoCost === 0);

      const res = await fetch(`/api/sessions/${sessionId}/checkout`, { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tabsToPay: selectedTabs, close_session })
      });
      
      if (!res.ok) throw new Error('Failed');

      fetchStations();
      fetchDashboard();
      
      if (close_session) {
        // Session fully closed → show receipt then go to dashboard
        const chronoMinutes = selectedStation ? Math.round(getActiveMs(selectedStation) / 60000) : 0;
        const chronoCost = selectedStation ? calculateCost(selectedStation) : 0;
        const consos = ordersToPay.filter(o => o.category !== 'GAME_TIME').map(o => ({ name: o.product_name, quantity: o.quantity, unit_price: Number(o.unit_price) }));
        const gameTimes = ordersToPay.filter(o => o.category === 'GAME_TIME').map(o => ({ quantity: o.quantity, unit_price: Number(o.unit_price) }));
        const consosTotal = consos.reduce((s, o) => s + o.quantity * o.unit_price, 0);
        const gameTimesTotal = gameTimes.reduce((s, o) => s + o.quantity * o.unit_price, 0);
        setStopResult({
          is_partial: false,
          station_name: selectedStation?.name,
          paid_tabs: selectedTabs,
          duration_minutes: chronoMinutes,
          base_cost: chronoCost,
          orders_total: consosTotal,
          total_cost: consosTotal + gameTimesTotal + chronoCost,
          consos,
          gameTimes,
          rate_per_hour: selectedStation?.default_rate_per_hour,
        });
        showToast('Session terminée avec succès ✅', 'success');
      } else {
        // Partial validation → stay in session, show receipt overlay, refresh
        const consos = ordersToPay.filter(o => o.category !== 'GAME_TIME').map(o => ({ name: o.product_name, quantity: o.quantity, unit_price: Number(o.unit_price) }));
        const gameTimes = ordersToPay.filter(o => o.category === 'GAME_TIME').map(o => ({ quantity: o.quantity, unit_price: Number(o.unit_price) }));
        const consosTotal = consos.reduce((s, o) => s + o.quantity * o.unit_price, 0);
        const gameTimesTotal = gameTimes.reduce((s, o) => s + o.quantity * o.unit_price, 0);
        const remaining = tabCount - selectedTabs.length;
        setTabCount(Math.max(1, remaining));
        setActiveTab(1);
        await fetchOrders(sessionId);
        setStopResult({
          is_partial: true,
          station_name: selectedStation?.name,
          paid_tabs: selectedTabs,
          duration_minutes: 0, // Not paying live chrono in partial validation
          base_cost: 0,
          orders_total: consosTotal,
          total_cost: consosTotal + gameTimesTotal,
          consos,
          gameTimes,
          rate_per_hour: selectedStation?.default_rate_per_hour,
        });
        showToast(`💸 ${(consosTotal + gameTimesTotal).toLocaleString('fr-DZ')} DA encaissés`, 'success');
      }
    } catch {
      showToast('Erreur lors de la validation', 'error');
    }
  };


  const cancelSession = async (sessionId: number, keepOrders: boolean) => {
    try {
      let transferredMinutes = 0;
      if (keepOrders && selectedStation) {
        transferredMinutes = Math.max(0, Math.round(getActiveMs(selectedStation) / 60000));
      }

      const res = await fetch(`/api/sessions/${sessionId}`, { 
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ keepOrders, transferredMinutes, activeTab })
      });
      if (!res.ok) throw new Error('Failed');
      
      showToast(keepOrders ? 'Chrono réinitialisé ⏱️' : 'Session effacée 🗑️', 'success');
      fetchStations();
      fetchDashboard();
      if (keepOrders) {
        fetchOrders(sessionId);
      } else {
        setSelectedStation(null);
      }
    } catch {
      showToast('Erreur lors de l\'opération', 'error');
    }
  };

  const pauseSession = async (sessionId: number) => {
    try {
      const res = await fetch(`/api/sessions/${sessionId}/pause`, { method: 'PATCH' });
      if (!res.ok) throw new Error('Failed');
      showToast('Session en pause ⏸️', 'success');
      fetchStations();
    } catch {
      showToast('Erreur lors de la mise en pause', 'error');
    }
  };

  const resumeSession = async (sessionId: number) => {
    try {
      const res = await fetch(`/api/sessions/${sessionId}/resume`, { method: 'PATCH' });
      if (!res.ok) throw new Error('Failed');
      showToast('Reprise de la session ▶️', 'success');
      fetchStations();
    } catch {
      showToast('Erreur lors de la reprise', 'error');
    }
  };

  const addProduct = async (sessionId: number, productId: number) => {
    try {
      const res = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_id: sessionId, product_id: productId, tab_index: activeTab }),
      });
      if (!res.ok) throw new Error('Failed');
      showToast('Produit ajouté ! ✅', 'success');
      setShowProductModal(false);
      fetchOrders(sessionId); // Refresh Orders!
      fetchStations(); // Update dashboard total
    } catch {
      showToast('Erreur lors de l\'ajout du produit', 'error');
    }
  };

  const updateOrderQuantity = async (orderId: number, newQuantity: number) => {
    try {
      if (!selectedStation) return;
      const res = await fetch(`/api/orders/${orderId}`, {
        method: newQuantity > 0 ? 'PATCH' : 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: newQuantity > 0 ? JSON.stringify({ quantity: newQuantity }) : undefined,
      });
      if (!res.ok) throw new Error('Failed');
      fetchOrders(selectedStation.active_session_id!);
      fetchStations(); // Update dashboard total
    } catch {
      showToast('Erreur modification quantité', 'error');
    }
  };

  const deleteTab = async (tabIdx: number) => {
    if (!selectedStation?.active_session_id) return;
    if (!confirm(`Supprimer complètement la liste ${tabIdx} et son contenu ?`)) return;
    
    try {
      const res = await fetch('/api/orders/tab', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_id: selectedStation.active_session_id, tab_index: tabIdx })
      });
      
      if (!res.ok) throw new Error('Failed to delete tab');
      
      showToast(`Liste ${tabIdx} supprimée`, 'success');
      
      setActiveTab(1);
      setTabCount(c => Math.max(1, c - 1));
      fetchOrders(selectedStation.active_session_id);
      fetchStations();
    } catch {
      showToast('Erreur lors de la suppression de la liste', 'error');
    }
  };

  const mergeTabs = async () => {
    if (!selectedStation?.active_session_id) return;
    if (tabsToMerge.length < 2) return;
    
    // Sort to find the lowest tab index to merge INTO
    const sortedTabs = [...tabsToMerge].sort((a, b) => a - b);
    const targetTab = sortedTabs[0];
    const sourceTabs = sortedTabs.slice(1);
    
    try {
      const res = await fetch('/api/orders/merge-tabs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          session_id: selectedStation.active_session_id, 
          target_tab: targetTab, 
          source_tabs: sourceTabs 
        })
      });
      
      if (!res.ok) throw new Error('Failed to merge tabs');
      
      showToast('Listes fusionnées', 'success');
      setIsMergeMode(false);
      setTabsToMerge([]);
      setActiveTab(targetTab);
      // We merged sourceTabs.length tabs, so max tabs decreases
      setTabCount(c => Math.max(1, c - sourceTabs.length));
      fetchOrders(selectedStation.active_session_id);
      fetchStations();
    } catch {
      showToast('Erreur lors de la fusion', 'error');
    }
  };

  const executeTransfer = async (targetTab: number) => {
    if (transferList.length === 0 || !selectedStation?.active_session_id) return;
    
    try {
      await Promise.all(transferList.map(async (item) => {
        const res = await fetch('/api/orders/transfer', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ 
            order_id: item.order.id,
            transfer_quantity: item.quantity,
            target_tab: targetTab
          })
        });
        if (!res.ok) throw new Error('Failed to transfer item');
      }));
      
      showToast('Articles transférés', 'success');
      setTransferList([]);
      // Auto-increase tab count if they transfer to a newly created tab index that is beyond current
      if (targetTab > tabCount) {
        setTabCount(targetTab);
      }
      fetchOrders(selectedStation.active_session_id);
      fetchStations();
    } catch {
      showToast('Erreur lors du transfert', 'error');
    }
  };

  const executeExternalTransfer = async (targetSessionId: number, targetTab: number) => {
    if (transferList.length === 0) return;
    
    try {
      await Promise.all(transferList.map(async (item) => {
        const res = await fetch('/api/orders/transfer', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ 
            order_id: item.order.id,
            transfer_quantity: item.quantity,
            target_tab: targetTab,
            target_session_id: targetSessionId
          })
        });
        if (!res.ok) throw new Error('Failed to transfer item externally');
      }));
      
      showToast('Articles transférés vers le poste', 'success');
      setTransferList([]);
      setShowExternalTransferModal(false);
      setExternalTargetStation(null);
      
      if (selectedStation?.active_session_id) {
         fetchOrders(selectedStation.active_session_id);
      }
      fetchStations();
    } catch {
      showToast('Erreur lors du transfert externe', 'error');
    }
  };

  const transferGameTimeToChrono = async (orderId: number) => {
    if (!selectedStation?.active_session_id) return;
    try {
      const res = await fetch('/api/orders/transfer-to-chrono', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ order_id: orderId })
      });
      if (!res.ok) throw new Error('Failed');
      showToast('⏱️ Temps transféré vers le compteur', 'success');
      fetchOrders(selectedStation.active_session_id);
      fetchStations();
    } catch {
      showToast('Erreur lors du transfert vers le compteur', 'error');
    }
  };

  const saveSessionNote = async () => {
    if (!selectedStation?.active_session_id) return;
    try {
      const res = await fetch(`/api/sessions/${selectedStation.active_session_id}/note`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ note: noteInput, tab_index: activeTab })
      });
      if (!res.ok) throw new Error('Failed to save note');
      
      setSessionNotes(prev => ({ ...prev, [activeTab]: noteInput }));
      setShowNoteModal(false);
      showToast('Note sauvegardée', 'success');
    } catch {
      showToast('Erreur lors de la sauvegarde de la note', 'error');
    }
  };

  const addGameTime = async (sessionId: number, minutes: number) => {
    try {
      const res = await fetch('/api/orders/gametime', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_id: sessionId, minutes, tab_index: activeTab }),
      });
      if (!res.ok) throw new Error('Failed');
      showToast(`⏱️ ${minutes} min de jeu ajoutées`, 'success');
      setShowGameTimeModal(false);
      fetchOrders(sessionId);
      fetchStations();
    } catch {
      showToast('Erreur lors de l\'ajout du temps de jeu', 'error');
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
      router.push('/login');
    } catch {
      showToast('Erreur lors de la déconnexion', 'error');
    }
  };

  // ─── Render: Sidebar ─────────────────────────────
  const renderSidebar = () => (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-icon">🎮</div>
        <div>
          <h1>PushPlay</h1>
          <span>Manager V2</span>
        </div>
      </div>
      <nav className="sidebar-nav">
        <div className="nav-section-label">Menu Principal</div>
        <button 
          className={`nav-item ${currentPage === 'dashboard' ? 'active' : ''}`}
          onClick={() => setCurrentPage('dashboard')}
        >
          <span className="nav-icon">📊</span>
          Dashboard
        </button>
        <button 
          className={`nav-item ${currentPage === 'products' ? 'active' : ''}`}
          onClick={() => setCurrentPage('products')}
        >
          <span className="nav-icon">🛒</span>
          Produits
        </button>

        {user?.role === 'ADMIN' && (
          <button 
            className={`nav-item ${currentPage === 'team' ? 'active' : ''}`}
            onClick={() => setCurrentPage('team')}
          >
            <span className="nav-icon">👥</span>
            Équipe
          </button>
        )}

        <div className="nav-section-label">Système</div>
        <div className="nav-item" style={{ cursor: 'default' }}>
          <span className="nav-icon">●</span>
          <span style={{ color: dbConnected ? 'var(--accent-green)' : 'var(--accent-red)' }}>
            {dbConnected ? 'Base connectée' : 'DB déconnectée'}
          </span>
        </div>
        {user && (
          <div className="nav-section-label">Session</div>
        )}
        {user && (
          <div className="nav-item" style={{ flexDirection: 'column', alignItems: 'flex-start', cursor: 'default', padding: '12px 16px', background: 'var(--bg-glass)', border: '1px solid var(--border-glass)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <div style={{
                width: 38, height: 38, borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.1)',
                overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center'
              }}>
                {user.image_url ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={user.image_url} alt={user.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <span style={{ fontSize: '1.2rem' }}>👤</span>
                )}
              </div>
              <div>
                <div style={{ fontSize: '0.85rem', fontWeight: 700 }}>{user.username}</div>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{user.role}</div>
              </div>
            </div>
            
            <button 
              onClick={handleLogout}
              style={{ padding: '6px 8px', background: 'rgba(255, 82, 82, 0.15)', color: 'var(--accent-red)', border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: '0.75rem', fontWeight: 600, width: '100%' }}
            >
              Déconnexion
            </button>
          </div>
        )}
      </nav>
    </aside>
  );

  // ─── Render: Header ───────────────────────────────
  const renderHeader = () => {
    const titles: Record<string, { title: string; subtitle: string }> = {
      dashboard: { title: '📊 Dashboard', subtitle: 'Vue d\'ensemble de votre activité (PlayStation)' },
      products: { title: '🛒 Produits', subtitle: 'Carte et inventaire' },
      team: { title: '👥 Équipe', subtitle: 'Gérer les employés et les rôles' },
    };
    const current = titles[currentPage] || titles['dashboard'];

    return (
      <header className="header">
        <div>
          <div className="header-title">{current.title}</div>
          <div className="header-subtitle">{current.subtitle}</div>
        </div>
        <div className="header-actions">
          <div className="header-clock">{clock}</div>
        </div>
      </header>
    );
  };

  // ─── Render: Dashboard Page ───────────────────────
  const renderDashboard = () => {
    return (
      <div className="page">

        <div className="section-header" style={{ marginTop: 28 }}>
          <div>
            <div className="section-title">Contrôle des Postes</div>
            <div className="section-subtitle">{stations.filter(s => s.active_session_id).length} sessions en cours</div>
          </div>
        </div>
        <div className="stations-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))' }}>
          {stations.map((station) => {
            const isOccupied = !!station.active_session_id;
            const isPaused = station.session_status === 'PAUSED';
            let cardClass = 'station-card';
            let statusText = 'Libre';
            if (isOccupied) {
              if (isPaused) {
                cardClass += ' paused';
                statusText = 'En pause';
              } else {
                cardClass += ' active';
                statusText = 'En cours';
              }
            }

            return (
              <div
                key={station.id}
                className={cardClass}
                style={{
                  display: 'flex', flexDirection: 'column', padding: '20px',
                  borderTop: isOccupied ? (isPaused ? '4px solid var(--accent-orange)' : '4px solid var(--accent-green)') : '4px solid transparent',
                  background: isOccupied ? (isPaused ? 'rgba(255, 152, 0, 0.05)' : 'rgba(76, 175, 80, 0.05)') : 'var(--bg-glass)',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div className="station-icon" style={{ fontSize: '2rem', background: 'rgba(255,255,255,0.1)', padding: 12, borderRadius: '50%' }}>
                      {getStationIcon(station.type)}
                    </div>
                    <div>
                      <div className="station-name" style={{ fontSize: '1.4rem' }}>{station.name}</div>
                      <div className="station-type">{station.default_rate_per_hour} DA/h</div>
                    </div>
                  </div>
                  <div>
                    {isOccupied ? (
                      <span className={`badge ${isPaused ? 'badge-idle' : 'badge-active'}`} style={{ 
                        background: isPaused ? 'rgba(255, 152, 0, 0.2)' : 'rgba(76, 175, 80, 0.2)', 
                        color: isPaused ? 'var(--accent-orange)' : 'var(--accent-green)' 
                      }}>
                        {statusText}
                      </span>
                    ) : (
                      <span className="badge badge-idle">Libre</span>
                    )}
                  </div>
                </div>

                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minHeight: 80 }}>
                  {isOccupied ? (
                    <>
                      <div style={{ fontSize: '2.5rem', fontWeight: 800, color: isPaused ? 'var(--accent-orange)' : 'var(--accent-green)', fontVariantNumeric: 'tabular-nums', lineHeight: 1 }}>
                        {formatDuration(station)}
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: 8 }}>
                        <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-secondary)' }}>
                          {formatCurrency(calculateCost(station))} DA
                        </div>
                        {Number(station.orders_total_cost) > 0 && (
                          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: 2, fontStyle: 'italic' }}>
                            + {formatCurrency(Number(station.orders_total_cost))} DA consos
                          </div>
                        )}
                      </div>
                    </>
                  ) : (
                    <div style={{ color: 'var(--text-muted)' }}>Poste prêt</div>
                  )}
                </div>

                {/* Boutons d'action unifiés */}
                <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
                  {!isOccupied ? (
                    <button className="btn btn-success" style={{ flex: 1 }} onClick={() => startSession(station.id)}>
                      ▶ Start
                    </button>
                  ) : (
                    <>
                      {isPaused ? (
                        <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => resumeSession(station.active_session_id!)}>
                          ▶ Reprendre
                        </button>
                      ) : (
                        <button className="btn btn-warning" style={{ flex: 1, background: 'rgba(255, 152, 0, 0.1)', color: 'var(--accent-orange)' }} onClick={() => pauseSession(station.active_session_id!)}>
                          ⏸ Pause
                        </button>
                      )}
                      
                      <button className="btn btn-ghost" style={{ padding: '0 12px' }} onClick={() => setSelectedStation(station)}>
                        🛒
                      </button>

                      <button className="btn btn-danger" style={{ padding: '0 12px', background: 'rgba(255, 82, 82, 0.1)', color: 'var(--accent-red)' }} onClick={() => {
                        setSelectedStation(station);
                        setShowCancelModal(true);
                      }}>
                        ⏹️
                      </button>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };
  
  // ─── Render: Transfer Quantity Modal ───────────────────
  const renderTransferQtyModal = () => {
    if (!showTransferQtyModal) return null;
    const { order } = showTransferQtyModal;

    return (
      <div className="modal-overlay" onClick={() => setShowTransferQtyModal(null)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 320 }}>
          <div className="modal-header">
            <div className="modal-title">⇄ Transférer</div>
            <button className="modal-close" onClick={() => setShowTransferQtyModal(null)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>
              Combien de <strong>{order.category === 'GAME_TIME' ? 'Temps de jeu' : order.product_name}</strong> souhaitez-vous transférer ?
            </p>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 16, marginBottom: 20 }}>
              <button 
                className="btn btn-ghost" 
                style={{ width: 40, height: 40, borderRadius: '50%', fontSize: '1.2rem', padding: 0 }}
                onClick={() => setTransferQtyInput(q => Math.max(1, q - 1))}
              >
                -
              </button>
              <div style={{ fontSize: '2rem', fontWeight: 800 }}>{transferQtyInput}</div>
              <button 
                className="btn btn-ghost" 
                style={{ width: 40, height: 40, borderRadius: '50%', fontSize: '1.2rem', padding: 0 }}
                onClick={() => setTransferQtyInput(q => Math.min(order.quantity, q + 1))}
              >
                +
              </button>
            </div>
          </div>
          <div className="modal-footer">
            <button className="btn btn-ghost" onClick={() => setShowTransferQtyModal(null)}>Annuler</button>
            <button className="btn" style={{ background: 'var(--accent-orange)', color: '#fff', border: 'none' }} onClick={() => {
              setTransferList(prev => [...prev, { order, quantity: transferQtyInput }]);
              setShowTransferQtyModal(null);
            }}>
              Ajouter
            </button>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Products Page ────────────────────────
  const renderProducts = () => {
    const categoryIcons: Record<string, string> = {
      DRINK: '🥤',
      FOOD: '🍔',
      CHICHA: '💨',
      AUTRE: '📦',
    };
    return (
      <div className="page">
        <div className="section-header">
          <div>
            <div className="section-title">Catalogue Produits</div>
            <div className="section-subtitle">Gérez vos consommations et le stock</div>
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => setShowAddProductCatalogModal(true)}>
            + Ajouter
          </button>
        </div>

        <div className="stations-grid">
          {products.map((product) => (
            <div key={product.id} className="station-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
              <div style={{
                width: 72, height: 72, borderRadius: 12, backgroundColor: 'rgba(255,255,255,0.1)',
                marginBottom: 16, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center'
              }}>
                {product.image_url ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={product.image_url} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <span style={{ fontSize: '2rem' }}>{categoryIcons[product.category] || '📦'}</span>
                )}
              </div>
              <div className="station-name" style={{ fontSize: '1.2rem' }}>{product.name}</div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{product.category}</div>
              
              <div className="station-rate" style={{ color: 'var(--accent-orange)', fontWeight: 700, marginTop: 8 }}>
                {formatCurrency(product.price)} DA
              </div>
              <div style={{ color: product.stock_quantity > 0 ? 'var(--accent-green)' : 'var(--accent-red)', fontWeight: 600, fontSize: '0.9rem', marginTop: 4 }}>
                Stock: {product.stock_quantity}
              </div>
              
              <div style={{ display: 'flex', gap: 8, marginTop: 12, width: '100%' }}>
                <button 
                  className="btn btn-ghost btn-sm" 
                  style={{ flex: 1, padding: '6px 0', fontSize: '0.8rem' }}
                  onClick={() => { setSelectedCatalogProduct(product); setShowEditProductCatalogModal(true); }}
                >
                  ✏️ Modifier
                </button>
                <button 
                  className="btn btn-danger btn-sm" 
                  style={{ flex: 1, padding: '6px 0', fontSize: '0.8rem', background: 'rgba(255, 82, 82, 0.1)', color: 'var(--accent-red)' }}
                  onClick={() => { setSelectedCatalogProduct(product); setShowDeleteProductCatalogModal(true); }}
                >
                  🗑️
                </button>
              </div>
            </div>
          ))}
        </div>
        {products.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon">🛒</div>
            <p>Aucun produit trouvé. Ajoutez des produits à votre catalogue.</p>
          </div>
        )}
      </div>
    );
  };

  // ─── Render: Team Page ────────────────────────────
  const renderTeam = () => {
    return (
      <div className="page">
        <div className="section-header">
          <div>
            <div className="section-title">Utilisateurs</div>
            <div className="section-subtitle">Membres du personnel et accès au système</div>
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => setShowAddUserModal(true)}>
            + Ajouter
          </button>
        </div>

        <div className="stations-grid">
          {teamUsers.map((member) => (
            <div key={member.id} className="station-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
              <div style={{
                width: 72, height: 72, borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.1)',
                marginBottom: 16, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center'
              }}>
                {member.image_url ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={member.image_url} alt={member.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <span style={{ fontSize: '2rem' }}>👤</span>
                )}
              </div>
              <div className="station-name" style={{ fontSize: '1.2rem' }}>{member.username}</div>
              <div className="station-rate" style={{ color: 'var(--accent-blue)', fontWeight: 700, marginTop: 4 }}>
                {member.role}
              </div>
              <div style={{ display: 'flex', gap: 8, marginTop: 12, width: '100%' }}>
                <button 
                  className="btn btn-ghost btn-sm" 
                  style={{ flex: 1, padding: '6px 0', fontSize: '0.8rem' }}
                  onClick={() => { setSelectedTeamUser(member); setShowEditUserModal(true); }}
                >
                  ✏️ Modifier
                </button>
                {user?.username !== member.username && (
                  <button 
                    className="btn btn-danger btn-sm" 
                    style={{ flex: 1, padding: '6px 0', fontSize: '0.8rem', background: 'rgba(255, 82, 82, 0.1)', color: 'var(--accent-red)' }}
                    onClick={() => { setSelectedTeamUser(member); setShowDeleteUserModal(true); }}
                  >
                    🗑️
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  // ─── Render: Station Detail Modal ─────────────────
  const renderStationModal = () => {
    if (!selectedStation) return null;
    const isOccupied = !!selectedStation.active_session_id;
    
    // Calculates the cost of ordered products
    const ordersCost = stationOrders.reduce((sum, order) => sum + (Number(order.unit_price) * order.quantity), 0);
    const sessionCost = selectedStation.session_start ? calculateCost(selectedStation) : 0;
    const liveTotalCost = sessionCost + ordersCost;

    return (
      <div className="modal-overlay" onClick={() => { setSelectedStation(null); setStopResult(null); }}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: stopResult ? 400 : 950, width: '100%' }}>
          <div className="modal-header">
            <div>
              <div className="modal-title">
                {getStationIcon(selectedStation.type)} {selectedStation.name}
              </div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: 2 }}>
                {getStationLabel(selectedStation.type)} — {selectedStation.default_rate_per_hour} DA/h
              </div>
            </div>
            <button className="modal-close" onClick={() => { setSelectedStation(null); setStopResult(null); }}>✕</button>
          </div>

          <div className="modal-body" style={{ padding: '0' }}>
            {isOccupied && selectedStation.session_start ? (
              /* ── Session en cours (Layout 2 colonnes) ── */
              <div style={{ display: 'flex', minHeight: 400, flexWrap: 'wrap' }}>
                
                {/* Colonne de Gauche : Chrono & Contrôles */}
                <div style={{ flex: '1 1 250px', padding: 24, display: 'flex', flexDirection: 'column', alignItems: 'center', borderRight: '1px solid var(--border-glass)' }}>
                  <div className={`badge ${selectedStation.session_status === 'PAUSED' ? 'badge-paused' : 'badge-active'}`} style={{ fontSize: '0.85rem', padding: '6px 16px', marginBottom: 24 }}>
                    ● {selectedStation.session_status === 'PAUSED' ? 'Session en Pause' : 'Session Active'}
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    {selectedStation.session_status === 'ACTIVE' && (
                      <button 
                        className="btn btn-ghost btn-sm debug-btn" 
                        style={{ padding: 0, width: 32, height: 32, fontSize: '1.2rem', background: 'transparent' }} 
                        onClick={async () => {
                          await fetch(`/api/sessions/${selectedStation.active_session_id}/debug-remove-minute`, { method: 'POST' });
                          fetchStations();
                        }}
                        title="Enlever 1 min (Debug)"
                      >
                        ⏮
                      </button>
                    )}
                    <div style={{ fontSize: '3rem', fontWeight: 800, color: selectedStation.session_status === 'PAUSED' ? 'var(--text-muted)' : (selectedStation.transferred_minutes && selectedStation.transferred_minutes > 0 ? 'var(--accent-cyan, #00BCD4)' : 'var(--accent-green)'), fontVariantNumeric: 'tabular-nums', lineHeight: 1 }}>
                      {formatDuration(selectedStation)}
                    </div>
                    {selectedStation.session_status === 'ACTIVE' && (
                      <button 
                        className="btn btn-ghost btn-sm debug-btn" 
                        style={{ padding: 0, width: 32, height: 32, fontSize: '1.2rem', background: 'transparent' }} 
                        onClick={async () => {
                          await fetch(`/api/sessions/${selectedStation.active_session_id}/debug-add-minute`, { method: 'POST' });
                          fetchStations();
                        }}
                        title="Ajouter 1 min (Debug)"
                      >
                        ⏭
                      </button>
                    )}
                  </div>
                  
                  <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--accent-orange)', marginTop: 12, marginBottom: 32 }}>
                    {formatCurrency(sessionCost)} DA
                  </div>
                  
                  {/* Boutons de contrôle */}
                  <div style={{ display: 'flex', gap: 12, width: '100%', flexDirection: 'column', marginTop: 'auto' }}>
                    {selectedStation.session_status === 'PAUSED' ? (
                      <button className="btn btn-primary" onClick={() => resumeSession(selectedStation.active_session_id!)}>
                        ▶ Reprendre
                      </button>
                    ) : (
                      <button className="btn btn-ghost" style={{ background: 'rgba(255, 171, 0, 0.1)', color: 'var(--accent-orange)' }} onClick={() => pauseSession(selectedStation.active_session_id!)}>
                        ⏸ Pause
                      </button>
                    )}
                    <button className="btn" style={{ background: 'var(--accent-green)', color: '#1a1a1a', fontWeight: 800, border: 'none', boxShadow: 'none' }} onClick={handleValiderClick}>
                      💸 Valider
                    </button>
                    <button className="btn btn-danger" onClick={() => setShowCancelModal(true)}>
                      🛑 Arrêter
                    </button>
                  </div>
                </div>

                {/* Colonne de Droite : Consommations */}
                <div style={{ flex: '2 1 450px', padding: 24, display: 'flex', flexDirection: 'column', backgroundColor: 'rgba(0,0,0,0.2)' }}>
                  {/* Tab navigation header */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      {tabCount > 1 && (
                        <button 
                          className="btn btn-ghost btn-sm" 
                          style={{ padding: '4px 8px', minWidth: 28, opacity: activeTab === 1 ? 0.3 : 1 }} 
                          disabled={activeTab === 1}
                          onClick={() => setActiveTab(t => Math.max(1, t - 1))}
                        >
                          ◀
                        </button>
                      )}
                      <h4 style={{ margin: 0, fontSize: '1.1rem' }}>
                        🛒 {tabCount > 1 ? `Conso ${activeTab}/${tabCount}` : 'Consommations'}
                      </h4>
                      {tabCount > 1 && (
                        <button 
                          className="btn btn-ghost btn-sm" 
                          style={{ padding: '4px 8px', minWidth: 28, opacity: activeTab === tabCount ? 0.3 : 1 }} 
                          disabled={activeTab === tabCount}
                          onClick={() => setActiveTab(t => Math.min(tabCount, t + 1))}
                        >
                          ▶
                        </button>
                      )}
                    </div>
                    <div style={{ display: 'flex', gap: 6 }}>
                      {isMergeMode ? (
                        <>
                          <button 
                            className="btn btn-ghost btn-sm" 
                            style={{ padding: '4px 10px', fontSize: '0.8rem', background: 'rgba(255,255,255,0.1)' }}
                            onClick={() => { setIsMergeMode(false); setTabsToMerge([]); }}
                          >
                            ✕ Annuler
                          </button>
                          <button 
                            className="btn btn-primary btn-sm" 
                            style={{ padding: '4px 10px', fontSize: '0.8rem', background: 'var(--accent-purple)', borderColor: 'var(--accent-purple)' }}
                            disabled={tabsToMerge.length < 2}
                            onClick={mergeTabs}
                          >
                            ✔️ Valider Fusion
                          </button>
                        </>
                      ) : (
                        <>

                          <button 
                            className="btn btn-ghost btn-sm" 
                            style={{ padding: '4px 10px', fontSize: '0.8rem', border: '1px dashed rgba(255,255,255,0.2)' }}
                            onClick={() => { setTabCount(c => c + 1); setActiveTab(tabCount + 1); }}
                            title="Ajouter une liste de consommations"
                          >
                            + Liste
                          </button>
                          <button 
                            className="btn btn-ghost btn-sm" 
                            style={{ padding: '4px 10px', fontSize: '0.8rem', background: 'rgba(76, 175, 80, 0.1)', color: 'var(--accent-green)', border: '1px solid rgba(76, 175, 80, 0.3)' }}
                            onClick={() => setShowGameTimeModal(true)}
                            title="Ajouter du temps de jeu"
                          >
                            ⏱️ Temps
                          </button>
                          <button className="btn btn-primary btn-sm" onClick={() => setShowProductModal(true)}>
                            + Ajouter
                          </button>
                        </>
                      )}
                    </div>
                  </div>

                  {/* Tab indicator dots & Note Button */}
                  <div style={{ display: 'flex', gap: 6, justifyContent: 'center', alignItems: 'center', marginBottom: 12 }}>
                    
                    {/* Notes Button Container */}
                    <div 
                      style={{ position: 'relative', display: 'flex', alignItems: 'center' }}
                      onMouseEnter={() => setIsNoteHovered(true)}
                      onMouseLeave={() => setIsNoteHovered(false)}
                    >
                      <button 
                        className="btn btn-ghost btn-sm" 
                        style={{ 
                          padding: 0, 
                          width: 26, 
                          height: 26, 
                          display: 'flex', 
                          alignItems: 'center', 
                          justifyContent: 'center', 
                          borderRadius: '50%', 
                          background: sessionNotes[activeTab] ? 'rgba(33, 150, 243, 0.15)' : 'transparent',
                          border: sessionNotes[activeTab] ? '1px solid rgba(33, 150, 243, 0.4)' : '1px dashed rgba(255, 255, 255, 0.2)',
                          fontSize: '0.8rem',
                          opacity: sessionNotes[activeTab] ? 1 : 0.6,
                          transition: 'all 0.2s',
                          marginRight: tabCount > 1 ? 8 : 0
                        }}
                        onClick={() => { setNoteInput(sessionNotes[activeTab] || ''); setShowNoteModal(true); }}
                        title={!sessionNotes[activeTab] ? "Ajouter une note" : ""}
                      >
                        📝
                      </button>
                      
                      {/* Instant Tooltip */}
                      {sessionNotes[activeTab] && isNoteHovered && (
                        <div style={{
                          position: 'absolute',
                          bottom: '100%',
                          left: '0%',
                          transform: 'translateX(-20%)',
                          marginBottom: '8px',
                          background: 'rgba(20, 20, 20, 0.95)',
                          border: '1px solid rgba(33, 150, 243, 0.5)',
                          padding: '10px 14px',
                          borderRadius: '8px',
                          color: '#fff',
                          fontSize: '0.85rem',
                          whiteSpace: 'pre-wrap',
                          minWidth: 'max-content',
                          maxWidth: '280px',
                          pointerEvents: 'none',
                          zIndex: 50,
                          boxShadow: '0 8px 24px rgba(0,0,0,0.5)',
                          backdropFilter: 'blur(10px)'
                        }}>
                          {sessionNotes[activeTab]}
                        </div>
                      )}
                    </div>

                  {tabCount > 1 && (
                    <>
                      <div style={{ width: 1, height: 16, background: 'rgba(255,255,255,0.1)', margin: '0 4px' }} />
                      {Array.from({ length: tabCount }, (_, i) => {
                        const tabIdx = i + 1;
                        const tabOrders = stationOrders.filter(o => o.tab_index === tabIdx);
                        const hasItems = tabOrders.length > 0;
                        
                        let bgColor = activeTab === tabIdx ? 'var(--accent-blue)' : hasItems ? 'rgba(255,255,255,0.15)' : 'rgba(255,255,255,0.06)';
                        let textColor = activeTab === tabIdx ? '#fff' : 'var(--text-muted)';
                        let borderStyle = 'none';
                        
                        if (isMergeMode) {
                          const isSelected = tabsToMerge.includes(tabIdx);
                          if (isSelected) {
                             bgColor = 'var(--accent-purple)';
                             textColor = '#fff';
                          } else {
                             bgColor = 'rgba(255,255,255,0.05)';
                             textColor = 'rgba(255,255,255,0.3)';
                          }
                        } else if (transferList.length > 0) {
                          if (tabIdx === activeTab) {
                             bgColor = 'rgba(255,255,255,0.05)';
                             textColor = 'rgba(255,255,255,0.2)';
                          } else {
                             bgColor = 'var(--accent-orange)';
                             textColor = '#fff';
                             borderStyle = '1px solid rgba(255, 255, 255, 0.5)';
                          }
                        }

                        return (
                          <div key={tabIdx} style={{ display: 'flex', alignItems: 'center', gap: 4, background: bgColor, borderRadius: 16, padding: '2px 6px', transition: 'all 0.2s', border: borderStyle }}>
                            <button 
                              onClick={() => {
                                if (isMergeMode) {
                                  if (tabsToMerge.includes(tabIdx)) {
                                    setTabsToMerge(tabsToMerge.filter(t => t !== tabIdx));
                                  } else {
                                    setTabsToMerge([...tabsToMerge, tabIdx]);
                                  }
                                } else if (transferList.length > 0) {
                                  if (tabIdx !== activeTab) {
                                    executeTransfer(tabIdx);
                                  }
                                } else {
                                  setActiveTab(tabIdx);
                                }
                              }}
                              style={{ 
                                width: 24, height: 24, borderRadius: '50%', border: 'none', cursor: 'pointer',
                                background: 'transparent',
                                color: textColor,
                                fontSize: '0.75rem', fontWeight: 700,
                                display: 'flex', alignItems: 'center', justifyContent: 'center'
                              }}
                            >
                              {tabIdx}
                            </button>
                            {!isMergeMode && transferList.length === 0 && tabIdx > 1 && activeTab === tabIdx && (
                              <button 
                                onClick={() => deleteTab(tabIdx)}
                                style={{
                                  background: 'none', border: 'none', color: 'rgba(255,255,255,0.5)', cursor: 'pointer', fontSize: '0.7rem', padding: '0 4px', display: 'flex', alignItems: 'center'
                                }}
                                title="Supprimer cette liste"
                              >
                                ✕
                              </button>
                            )}
                          </div>
                        );
                      })}
                      
                      {!isMergeMode && transferList.length === 0 && (
                        <>
                          <div style={{ width: 1, height: 16, background: 'rgba(255,255,255,0.1)', margin: '0 4px' }} />
                          <button 
                            className="btn btn-ghost btn-sm" 
                            style={{ padding: '0', width: 26, height: 26, borderRadius: '50%', color: '#ce93d8', background: 'rgba(156, 39, 176, 0.1)', border: '1px solid rgba(156, 39, 176, 0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem' }}
                            onClick={() => { setIsMergeMode(true); setTabsToMerge([activeTab]); }}
                            title="Fusionner des listes"
                          >
                            🔗
                          </button>
                        </>
                      )}
                    </>
                  )}
                  </div>

                  {/* Banner indicating transfer mode */}
                  {transferList.length > 0 && (
                    <div style={{ background: 'rgba(255, 152, 0, 0.15)', border: '1px solid rgba(255, 152, 0, 0.3)', padding: '10px 16px', borderRadius: 8, marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div style={{ color: 'var(--accent-orange)', fontSize: '0.85rem', fontWeight: 600 }}>
                        ⇄ Sélectionnez la liste de destination pour transférer <strong style={{ color: '#fff' }}>{transferList.length} produit{transferList.length > 1 ? 's' : ''}</strong>
                      </div>
                      <div style={{ display: 'flex', gap: 8 }}>
                        <button 
                          className="btn btn-ghost btn-sm" 
                          style={{ padding: 0, width: 32, height: 32, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(255, 152, 0, 0.1)', border: '1px solid rgba(255,152,0,0.3)', borderRadius: 6 }} 
                          onClick={() => setShowExternalTransferModal(true)}
                          title="Transférer vers un autre poste"
                        >
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img src="/icons/transfer_extern.png" alt="Transfert externe" style={{ width: 18, height: 18, objectFit: 'contain', opacity: 0.9 }} />
                        </button>
                        <button className="btn btn-ghost btn-sm" style={{ padding: '4px 8px', minWidth: 'auto', fontSize: '0.75rem', borderRadius: 6, background: 'rgba(255,255,255,0.05)' }} onClick={() => setTransferList([])}>✕</button>
                      </div>
                    </div>
                  )}
                  
                  {/* Removed separate notes button */}

                  <div style={{ flex: 1, overflowY: 'auto', maxHeight: '350px', marginBottom: 16, display: 'flex', flexDirection: 'column', gap: 8, paddingRight: 8 }}>
                    {(() => {
                      const currentTabOrders = stationOrders.filter(o => o.tab_index === activeTab);
                      let gameTimeCounter = 1;
                      
                      return currentTabOrders.length === 0 ? (
                        <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '0.9rem', fontStyle: 'italic' }}>
                          Aucune consommation
                        </div>
                      ) : (
                        currentTabOrders.map(order => {
                          let displayTitle = order.product_name;
                          if (order.category === 'GAME_TIME') {
                            displayTitle = `⏱️ Temps de jeu ${gameTimeCounter}`;
                            gameTimeCounter++;
                          }

                          const isTransferSelected = transferList.some(t => t.order.id === order.id);
                          const transferItem = transferList.find(t => t.order.id === order.id);

                          return (
                          <div key={order.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 16px', background: isTransferSelected ? 'rgba(255, 152, 0, 0.15)' : (order.category === 'GAME_TIME' ? 'rgba(76, 175, 80, 0.08)' : 'var(--bg-glass)'), borderRadius: 8, alignItems: 'center', border: isTransferSelected ? '1px solid var(--accent-orange)' : (order.category === 'GAME_TIME' ? '1px solid rgba(76, 175, 80, 0.2)' : 'none') }}>
                            <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                              <div style={{ width: 40, height: 40, borderRadius: 6, backgroundColor: order.category === 'GAME_TIME' ? 'rgba(76, 175, 80, 0.15)' : 'rgba(255,255,255,0.1)', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                {order.category === 'GAME_TIME' ? (
                                  <span style={{ fontSize: '1.2rem' }}>⏱️</span>
                                ) : order.image_url ? (
                                  // eslint-disable-next-line @next/next/no-img-element
                                  <img src={order.image_url} alt={order.product_name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                ) : (
                                  <span style={{ fontSize: '1.2rem' }}>📦</span>
                                )}
                              </div>
                              
                              <div style={{ display: 'flex', flexDirection: 'column' }}>
                                <span style={{ fontWeight: 600, color: order.category === 'GAME_TIME' ? 'var(--accent-green)' : 'inherit' }}>
                                  {displayTitle}
                                </span>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 4 }}>
                                  <button className="btn btn-ghost btn-sm" style={{ padding: '2px 8px', minWidth: 28, background: 'rgba(255,255,255,0.1)' }} onClick={() => updateOrderQuantity(order.id, order.quantity - 1)}>-</button>
                                  <span style={{ color: 'var(--text-secondary)', fontWeight: 700, fontSize: '0.9rem', minWidth: 16, textAlign: 'center' }}>
                                    {order.category === 'GAME_TIME' ? `${order.quantity} min` : order.quantity}
                                  </span>
                                  <button className="btn btn-ghost btn-sm" style={{ padding: '2px 8px', minWidth: 28, background: 'rgba(255,255,255,0.1)' }} onClick={() => updateOrderQuantity(order.id, order.quantity + 1)}>+</button>
                                  <button 
                                    className="btn btn-ghost btn-sm" 
                                    style={{ padding: '2px 8px', minWidth: 28, background: isTransferSelected ? 'var(--accent-orange)' : 'rgba(255, 152, 0, 0.1)', color: isTransferSelected ? '#fff' : 'var(--accent-orange)', marginLeft: 4 }} 
                                    onClick={() => {
                                      if (isTransferSelected) {
                                        setTransferList(prev => prev.filter(t => t.order.id !== order.id));
                                      } else {
                                        if (order.quantity > 1 && order.category !== 'GAME_TIME') {
                                          setTransferQtyInput(1);
                                          setShowTransferQtyModal({ order });
                                        } else {
                                          setTransferList(prev => [...prev, { order, quantity: order.quantity }]);
                                        }
                                      }
                                    }} 
                                    title={isTransferSelected ? "Retirer du transfert" : "Ajouter au transfert"}
                                  >
                                    ⇄
                                  </button>
                                  {isTransferSelected && (
                                    <span style={{ color: 'var(--accent-orange)', fontSize: '0.8rem', fontWeight: 700, marginLeft: 8 }}>
                                      {transferItem?.quantity} ⇄
                                    </span>
                                  )}
                                  {order.category === 'GAME_TIME' && (
                                    <button 
                                      className="btn btn-ghost btn-sm" 
                                      style={{ padding: '2px 8px', minWidth: 28, background: 'rgba(0, 188, 212, 0.1)', color: 'var(--accent-cyan, #00BCD4)', marginLeft: 4 }} 
                                      onClick={() => transferGameTimeToChrono(order.id)} 
                                      title="Retransférer vers le compteur principal"
                                    >
                                      ⏱️
                                    </button>
                                  )}
                                  <button 
                                    className="btn btn-ghost btn-sm" 
                                    style={{ padding: '2px 8px', minWidth: 28, background: 'rgba(244, 67, 54, 0.1)', color: 'var(--accent-red)', marginLeft: 4, fontWeight: 'bold' }} 
                                    onClick={() => updateOrderQuantity(order.id, 0)} 
                                    title="Supprimer la ligne"
                                  >
                                    ✕
                                  </button>
                                </div>
                              </div>
                            </div>
                            
                            <span style={{ fontWeight: 700, color: order.category === 'GAME_TIME' ? 'var(--accent-green)' : 'var(--accent-orange)' }}>{formatCurrency(Number(order.unit_price) * order.quantity)} DA</span>
                          </div>
                        )})
                      );
                    })()}
                  </div>
                  
                  {/* Total Général */}
                  <div style={{ borderTop: '1px solid var(--border-glass)', paddingTop: 16 }}>
                    {(() => {
                      const activeTabOrders = stationOrders.filter(o => o.tab_index === activeTab);
                      const activeTabConsosCost = activeTabOrders.filter(o => o.category !== 'GAME_TIME').reduce((s, o) => s + Number(o.unit_price) * o.quantity, 0);
                      const activeTabGameTimeCost = activeTabOrders.filter(o => o.category === 'GAME_TIME').reduce((s, o) => s + Number(o.unit_price) * o.quantity, 0) + (activeTab === 1 ? sessionCost : 0);
                      const activeTabTotalCost = activeTabConsosCost + activeTabGameTimeCost;

                      const totalConsosCost = stationOrders.filter(o => o.category !== 'GAME_TIME').reduce((s, o) => s + Number(o.unit_price) * o.quantity, 0);
                      const totalGameTimeCost = stationOrders.filter(o => o.category === 'GAME_TIME').reduce((s, o) => s + Number(o.unit_price) * o.quantity, 0) + sessionCost;
                      const grandTotalCost = totalConsosCost + totalGameTimeCost;

                      return (
                        <>
                          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 12 }}>
                            {/* Colonne Liste Active */}
                            <div style={{ background: 'var(--bg-glass)', padding: '10px 12px', borderRadius: 8, border: '1px solid var(--border-glass)' }}>
                              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 6, fontWeight: 700, textTransform: 'uppercase' }}>
                                Liste #{activeTab} {activeTab === 1 && '(+ Chrono)'}
                              </div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: 4 }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Consos</span>
                                <span style={{ fontWeight: 600 }}>{formatCurrency(activeTabConsosCost)}</span>
                              </div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: 6 }}>
                                <span style={{ color: 'var(--accent-green)' }}>Jeu</span>
                                <span style={{ fontWeight: 600, color: 'var(--accent-green)' }}>{formatCurrency(activeTabGameTimeCost)}</span>
                              </div>
                              <div style={{ borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: 6, display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                                <span style={{ fontWeight: 700, color: 'var(--text-primary)' }}>Total</span>
                                <span style={{ fontWeight: 800, color: 'var(--text-primary)' }}>{formatCurrency(activeTabTotalCost)}</span>
                              </div>
                            </div>
                            
                            {/* Colonne Total Poste */}
                            <div style={{ background: 'var(--bg-glass)', padding: '10px 12px', borderRadius: 8, border: '1px solid var(--border-glass)' }}>
                              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 6, fontWeight: 700, textTransform: 'uppercase' }}>Total Poste</div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: 4 }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Consos</span>
                                <span style={{ fontWeight: 600 }}>{formatCurrency(totalConsosCost)}</span>
                              </div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                                <span style={{ color: 'var(--accent-green)' }}>Jeu</span>
                                <span style={{ fontWeight: 600, color: 'var(--accent-green)' }}>{formatCurrency(totalGameTimeCost)}</span>
                              </div>
                            </div>
                          </div>
                          
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255, 152, 0, 0.1)', padding: '12px 16px', borderRadius: 8, border: '1px solid rgba(255, 152, 0, 0.2)' }}>
                            <span style={{ fontWeight: 800, fontSize: '1.1rem', color: 'var(--accent-orange)' }}>TOTAL À PAYER</span>
                            <span style={{ fontWeight: 800, fontSize: '1.4rem', color: 'var(--accent-orange)' }}>{formatCurrency(grandTotalCost)} DA</span>
                          </div>
                        </>
                      );
                    })()}
                  </div>
                </div>
              </div>
            ) : (
              /* ── Station libre ── */
              <div className="empty-state" style={{ padding: '60px 0' }}>
                <div className="empty-icon">🟢</div>
                <p style={{ marginBottom: 24 }}>Ce poste est libre.</p>
                <button className="btn btn-primary" onClick={() => startSession(selectedStation.id)}>
                  ▶ Démarrer la Session
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };
  // ─── Render: Receipt Modal ────────────────────────
  const renderReceiptModal = () => {
    if (!stopResult) return null;
    const isPartial = stopResult.is_partial;
    const handleClose = () => {
      setStopResult(null);
      if (!isPartial) {
        setSelectedStation(null);
      }
    };

    // Combine live chrono and game items into a sequential list
    const allGameSessions: { duration: number; cost: number }[] = [];
    if ((stopResult.duration_minutes ?? 0) > 0) {
      allGameSessions.push({ duration: stopResult.duration_minutes!, cost: stopResult.base_cost! });
    }
    stopResult.gameTimes.forEach(g => {
      allGameSessions.push({ duration: g.quantity, cost: g.quantity * g.unit_price });
    });

    const gameTotal = allGameSessions.reduce((s, g) => s + g.cost, 0);
    const consosTotal = stopResult.consos.reduce((s, c) => s + c.quantity * c.unit_price, 0);

    const printReceipt = () => {
      const w = window.open('', '_blank', 'width=450,height=700');
      if (!w) return;
      w.document.write(`
        <html><head><title>Reçu — ${stopResult.station_name || ''}</title>
        <style>
          body { font-family: 'Courier New', monospace; padding: 20px; color: #111; max-width: 400px; margin: 0 auto; line-height: 1.4; }
          .header { text-align: center; border-bottom: 2px dashed #333; padding-bottom: 10px; margin-bottom: 15px; }
          .section-title { font-weight: bold; border-bottom: 1px solid #ddd; margin: 15px 0 5px; text-transform: uppercase; font-size: 0.9rem; }
          .row { display: flex; justify-content: space-between; margin: 4px 0; font-size: 0.85rem; }
          .item-row { margin-left: 10px; color: #444; }
          .total-row { font-weight: bold; margin-top: 5px; }
          .grand-total { font-weight: bold; font-size: 1.2rem; border-top: 2px dashed #333; padding-top: 10px; margin-top: 20px; text-align: right; }
          .center { text-align: center; color: #555; font-size: 0.8rem; margin-top: 20px; font-style: italic; }
        </style></head><body>
        <div class="header">
          <h2 style="margin:0">🧾 PUSHPLAY MANAGER</h2>
          <div style="font-size:0.8rem">Poste: ${stopResult.station_name || ''}</div>
          <div style="font-size:0.7rem">${new Date().toLocaleString('fr-FR')}</div>
        </div>

        ${allGameSessions.length > 0 ? `
          <div class="section-title">Sessions de Jeu</div>
          ${allGameSessions.map((g, i) => `
            <div class="row item-row">
              <span>Session de jeu ${i + 1} (${g.duration} min @${stopResult.rate_per_hour} DA/h)</span>
              <span>${g.cost.toFixed(2)} DA</span>
            </div>
          `).join('')}
          <div class="row total-row" style="border-top:1px dotted #ccc">
            <span>Total Jeu</span>
            <span>${gameTotal.toFixed(2)} DA</span>
          </div>
        ` : ''}

        ${stopResult.consos.length > 0 ? `
          <div class="section-title">Consommations</div>
          ${stopResult.consos.map(c => `
            <div class="row item-row">
              <span>${c.name} (x${c.quantity})</span>
              <span>${(c.quantity * c.unit_price).toFixed(2)} DA</span>
            </div>
          `).join('')}
          <div class="row total-row" style="border-top:1px dotted #ccc">
            <span>Total Conso</span>
            <span>${consosTotal.toFixed(2)} DA</span>
          </div>
        ` : ''}

        <div class="grand-total">
          TOTAL: ${stopResult.total_cost.toFixed(2)} DA
        </div>
        
        <div class="center">Merci pour votre visite !</div>
        </body></html>`);
      w.document.close();
      w.print();
    };

    return (
      <div className="modal-overlay" onClick={handleClose} style={{ zIndex: 9999 }}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 450, width: '90%' }}>
          <div className="modal-header">
            <div className="modal-title">🧾 {isPartial ? 'Reçu Partiel' : 'Reçu de Session'}</div>
            <button className="modal-close" onClick={handleClose}>✕</button>
          </div>
          <div className="modal-body" style={{ padding: '20px 24px' }}>
            <div style={{ textAlign: 'center', marginBottom: 20 }}>
              <div style={{ fontSize: '1.2rem', fontWeight: 700 }}>{stopResult.station_name}</div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{new Date().toLocaleString('fr-FR')}</div>
            </div>

            {/* Section Jeu */}
            {allGameSessions.length > 0 && (
              <div style={{ marginBottom: 20 }}>
                <div style={{ color: 'var(--accent-green)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', borderBottom: '1px solid var(--border-glass)', paddingBottom: 4, marginBottom: 8 }}>
                  Sessions de Jeu
                </div>
                {allGameSessions.map((g, idx) => (
                  <div key={idx} className="receipt-row">
                    <span style={{ fontSize: '0.9rem' }}>⏱️ Session de jeu {idx + 1} ({g.duration} min)</span>
                    <span>{formatCurrency(g.cost)} DA</span>
                  </div>
                ))}
                <div className="receipt-row" style={{ marginTop: 4, fontWeight: 700, borderTop: '1px dashed var(--border-glass)', paddingTop: 4 }}>
                  <span>Total Jeu</span>
                  <span style={{ color: 'var(--accent-green)' }}>{formatCurrency(gameTotal)} DA</span>
                </div>
              </div>
            )}

            {/* Section Consos */}
            {stopResult.consos.length > 0 && (
              <div style={{ marginBottom: 20 }}>
                <div style={{ color: 'var(--accent-blue)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', borderBottom: '1px solid var(--border-glass)', paddingBottom: 4, marginBottom: 8 }}>
                  Consommations
                </div>
                {stopResult.consos.map((c, idx) => (
                  <div key={idx} className="receipt-row">
                    <span style={{ fontSize: '0.9rem' }}>{c.name} <small style={{ color: 'var(--text-muted)' }}>x{c.quantity}</small></span>
                    <span>{formatCurrency(c.quantity * c.unit_price)} DA</span>
                  </div>
                ))}
                <div className="receipt-row" style={{ marginTop: 4, fontWeight: 700, borderTop: '1px dashed var(--border-glass)', paddingTop: 4 }}>
                  <span>Total Conso</span>
                  <span style={{ color: 'var(--accent-blue)' }}>{formatCurrency(consosTotal)} DA</span>
                </div>
              </div>
            )}

            <div style={{ marginTop: 24, padding: 16, background: 'rgba(255,255,255,0.05)', borderRadius: 12, border: '1px solid var(--border-glass)' }}>
              <div className="receipt-row" style={{ fontSize: '1.4rem', fontWeight: 900 }}>
                <span>TOTAL</span>
                <span style={{ color: 'var(--accent-orange)' }}>{formatCurrency(stopResult.total_cost)} DA</span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
              <button className="btn btn-ghost" style={{ flex: 1, border: '1px solid var(--border-glass)' }} onClick={printReceipt}>
                🖨️ Imprimer
              </button>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={handleClose}>
                {isPartial ? 'Continuer' : 'Fermer'}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Chrono Alert Modal ───────────────────
  const renderChronoAlertModal = () => {
    if (!showChronoAlert || !selectedStation?.active_session_id) return null;
    const activeChronoCost = calculateCost(selectedStation);

    return (
      <div className="modal-overlay" onClick={() => {}}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 400 }}>
          <div className="modal-header">
            <div className="modal-title">⏱️ Compteur en marche</div>
            <button className="modal-close" onClick={() => setShowChronoAlert(false)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)' }}>
              Le chronomètre est actuellement à <strong style={{ color: 'var(--accent-orange)' }}>{formatCurrency(activeChronoCost)} DA</strong>.
            </p>
            <p style={{ marginTop: 12 }}>
              Souhaitez-vous le valider avec le reste des consommations (le compteur sera remis à zéro) ?
            </p>
          </div>
          <div className="modal-footer" style={{ flexDirection: 'column', gap: 8 }}>
            <button className="btn" style={{ background: 'var(--accent-green)', color: '#1a1a1a', fontWeight: 800, width: '100%', border: 'none' }} onClick={() => convertChrono(selectedStation.active_session_id!)}>
              Oui, valider et remettre à zéro
            </button>
            <button className="btn btn-ghost" style={{ border: '1px solid var(--border-glass)', width: '100%' }} onClick={() => {
              setTabsToPay(Array.from({ length: tabCount }).map((_, i) => i + 1));
              setShowChronoAlert(false);
              setShowStopModal(true);
            }}>
              Non, ignorer le compteur
            </button>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Confirm Checkout Modal ───────────────────
  const renderStopConfirmModal = () => {
    if (!showStopModal || !selectedStation?.active_session_id) return null;

    const toggleTab = (t: number) => {
      setTabsToPay(prev => prev.includes(t) ? prev.filter(x => x !== t) : [...prev, t].sort());
    };

    const chronoCost = selectedStation ? calculateCost(selectedStation) : 0;
    const ordersToPay = stationOrders.filter(o => tabsToPay.includes(o.tab_index));
    const totalToPay = ordersToPay.reduce((sum, o) => sum + Number(o.unit_price) * o.quantity, 0);

    return (
      <div className="modal-overlay" onClick={() => setShowStopModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 400 }}>
          <div className="modal-header">
            <div className="modal-title">💸 Valider la session</div>
            <button className="modal-close" onClick={() => setShowStopModal(false)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>
              Sélectionnez les listes à valider (leur contenu sera vidé) :
            </p>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 20 }}>
              {Array.from({ length: tabCount }).map((_, i) => {
                const t = i + 1;
                const cost = stationOrders.filter(o => o.tab_index === t).reduce((sum, o) => sum + Number(o.unit_price) * o.quantity, 0);
                return (
                  <label key={t} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px', background: 'var(--bg-glass)', borderRadius: 8, cursor: 'pointer', border: tabsToPay.includes(t) ? '1px solid var(--accent-green)' : '1px solid transparent' }}>
                    <input 
                      type="checkbox" 
                      checked={tabsToPay.includes(t)} 
                      onChange={() => toggleTab(t)} 
                      style={{ width: 18, height: 18, accentColor: 'var(--accent-green)' }} 
                    />
                    <div style={{ flex: 1, display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ fontWeight: 600 }}>Liste #{t} {t === 1 && chronoCost > 0 && '(+ Chrono)'}</span>
                      <span style={{ color: 'var(--accent-orange)' }}>{formatCurrency(cost)} DA</span>
                    </div>
                  </label>
                )
              })}
            </div>

            <div style={{ textAlign: 'center', marginTop: 16, padding: '12px', background: 'rgba(76, 175, 80, 0.1)', borderRadius: 8 }}>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: 4 }}>Total à régler</div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-green)' }}>
                {formatCurrency(totalToPay)} DA
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button className="btn btn-ghost" onClick={() => setShowStopModal(false)}>Retour</button>
            <button 
              className="btn" 
              style={{ background: 'var(--accent-green)', color: '#1a1a1a', fontWeight: 800, border: 'none', boxShadow: 'none', opacity: tabsToPay.length === 0 ? 0.5 : 1 }} 
              onClick={() => {
                setShowStopModal(false);
                executeCheckout(selectedStation.active_session_id!, tabsToPay);
              }}
              disabled={tabsToPay.length === 0}
            >
              🧾 Encaisser
            </button>
          </div>
        </div>
      </div>
    );
  };
  // ─── Render: Game Time Modal ───────────────────
  const renderGameTimeModal = () => {
    if (!showGameTimeModal || !selectedStation?.active_session_id) return null;
    const ratePerHour = selectedStation.default_rate_per_hour || 0;
    const estimatedCost = (gameTimeMinutes / 60) * ratePerHour;

    return (
      <div className="modal-overlay" onClick={() => setShowGameTimeModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 380 }}>
          <div className="modal-header">
            <div className="modal-title">⏱️ Ajouter du temps de jeu</div>
            <button className="modal-close" onClick={() => setShowGameTimeModal(false)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>
              Tarif du poste : <strong style={{ color: 'var(--accent-orange)' }}>{formatCurrency(ratePerHour)} DA/h</strong>
            </p>

            {/* Preset buttons */}
            <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
              {[15, 30, 45, 60].map(m => (
                <button 
                  key={m}
                  className="btn btn-ghost" 
                  style={{ 
                    flex: 1, padding: '10px 0', 
                    background: gameTimeMinutes === m ? 'rgba(76, 175, 80, 0.15)' : 'var(--bg-glass)',
                    border: gameTimeMinutes === m ? '1px solid var(--accent-green)' : '1px solid transparent',
                    color: gameTimeMinutes === m ? 'var(--accent-green)' : 'var(--text-secondary)',
                    fontWeight: gameTimeMinutes === m ? 700 : 400
                  }}
                  onClick={() => setGameTimeMinutes(m)}
                >
                  {m} min
                </button>
              ))}
            </div>

            {/* Custom input */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
              <label style={{ color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>Personnalisé :</label>
              <input
                type="number"
                min={1}
                max={999}
                value={gameTimeMinutes}
                onChange={(e) => setGameTimeMinutes(Math.max(1, parseInt(e.target.value) || 1))}
                style={{
                  flex: 1, padding: '10px 12px', borderRadius: 8,
                  background: 'var(--bg-glass)', border: '1px solid var(--border-glass)',
                  color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: 700,
                  textAlign: 'center'
                }}
              />
              <span style={{ color: 'var(--text-muted)' }}>min</span>
            </div>

            {/* Cost preview */}
            <div style={{ textAlign: 'center', padding: 16, background: 'rgba(76, 175, 80, 0.08)', borderRadius: 12, border: '1px solid rgba(76, 175, 80, 0.2)' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-green)' }}>
                {formatCurrency(estimatedCost)} DA
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 4 }}>
                {gameTimeMinutes} min × {formatCurrency(ratePerHour / 60)} DA/min
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button className="btn btn-ghost" onClick={() => setShowGameTimeModal(false)}>Annuler</button>
            <button className="btn" style={{ background: 'var(--accent-green)', color: '#1a1a1a', fontWeight: 800, border: 'none', boxShadow: 'none' }} onClick={() => {
              addGameTime(selectedStation.active_session_id!, gameTimeMinutes);
            }}>
              ⏱️ Ajouter {gameTimeMinutes} min
            </button>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Confirm Cancel/Arrêt Modal ───────────────────
  const renderCancelConfirmModal = () => {
    if (!showCancelModal || !selectedStation?.active_session_id) return null;

    return (
      <div className="modal-overlay" onClick={() => setShowCancelModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 450 }}>
          <div className="modal-header">
            <div className="modal-title">⏹️ Arrêter le poste</div>
            <button className="modal-close" onClick={() => setShowCancelModal(false)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)' }}>
              Que voulez-vous faire pour le poste <strong style={{ color: 'var(--text-primary)' }}>{selectedStation.name}</strong> ?
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginTop: 24 }}>
              <button 
                className="btn btn-primary" 
                style={{ justifyContent: 'flex-start', padding: '16px', background: 'var(--bg-glass)', border: '1px solid rgba(255,255,255,0.2)' }}
                onClick={() => {
                  setShowCancelModal(false);
                  cancelSession(selectedStation.active_session_id!, true);
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontSize: '1.5rem' }}>⏱️</span>
                  <div style={{ textAlign: 'left' }}>
                    <div style={{ fontWeight: 700 }}>Remettre le compteur à 0</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Le poste se met en pause mais conserve la liste des consommations.</div>
                  </div>
                </div>
              </button>

              <button 
                className="btn btn-danger" 
                style={{ justifyContent: 'flex-start', padding: '16px', background: 'rgba(255, 82, 82, 0.1)', border: '1px solid rgba(255, 82, 82, 0.3)' }}
                onClick={() => {
                  setShowCancelModal(false);
                  cancelSession(selectedStation.active_session_id!, false);
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontSize: '1.5rem' }}>🗑️</span>
                  <div style={{ textAlign: 'left' }}>
                    <div style={{ fontWeight: 700, color: 'var(--accent-red)' }}>Tout effacer</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Le compteur ET la liste de consommations seront supprimés sans rien sauvegarder.</div>
                  </div>
                </div>
              </button>
            </div>
          </div>
          <div className="modal-footer" style={{ borderTop: 'none', paddingTop: 8 }}>
            <button className="btn btn-ghost" style={{ width: '100%' }} onClick={() => setShowCancelModal(false)}>Retour</button>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Add Product Modal ────────────────────
  const renderProductModal = () => {
    if (!showProductModal || !selectedStation?.active_session_id) return null;

    const filteredProducts = products.filter(p => 
      p.name.toLowerCase().includes(productSearchQuery.toLowerCase()) || 
      p.category.toLowerCase().includes(productSearchQuery.toLowerCase())
    );

    return (
      <div className="modal-overlay" onClick={() => setShowProductModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title">🛒 Ajouter une consommation</div>
            <button className="modal-close" onClick={() => setShowProductModal(false)}>✕</button>
          </div>
          <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <input 
              type="text" 
              placeholder="Rechercher un produit (Nom ou catégorie)..." 
              value={productSearchQuery}
              onChange={(e) => setProductSearchQuery(e.target.value)}
              style={{ width: '100%', padding: '12px 16px', borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white', fontSize: '1rem' }}
              autoFocus
            />
            
            <div className="product-list" style={{ maxHeight: 400, overflowY: 'auto' }}>
              {filteredProducts.map((product) => (
                <div
                  key={product.id}
                  className="product-item"
                  onClick={() => addProduct(selectedStation.active_session_id!, product.id)}
                >
                  <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <div style={{ width: 44, height: 44, borderRadius: 8, backgroundColor: 'rgba(255,255,255,0.1)', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      {product.image_url ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img src={product.image_url} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      ) : (
                        <span style={{ fontSize: '1.2rem' }}>📦</span>
                      )}
                    </div>
                    <div>
                      <div className="product-name">{product.name}</div>
                      <div className="product-category">{product.category}</div>
                    </div>
                  </div>
                  <div className="product-price">{formatCurrency(product.price)} DA</div>
                </div>
              ))}
              {filteredProducts.length === 0 && (
                <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text-muted)' }}>
                  Aucun produit ne correspond à votre recherche.
                </div>
              )}
            </div>
          </div>
          <div className="modal-footer" style={{ borderTop: '1px solid var(--border-glass)', marginTop: 8, paddingTop: 16 }}>
             <button className="btn btn-ghost" style={{ width: '100%', border: '1px dashed rgba(255,255,255,0.2)', fontSize: '0.9rem' }} onClick={() => { 
                setShowProductModal(false); 
                setSelectedStation(null); 
                setCurrentPage('products'); 
             }}>
               ⚙️ Ajouter ou modifier un produit au catalogue
             </button>
          </div>
        </div>
      </div>
    );
  };
  // ─── Render: External Transfer Modal ───────────────────
  const renderExternalTransferModal = () => {
    if (!showExternalTransferModal) return null;

    const availableStations = stations.filter(s => s.active_session_id && s.id !== selectedStation?.id);

    return (
      <div className="modal-overlay" onClick={() => { setShowExternalTransferModal(false); setExternalTargetStation(null); }}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
          <div className="modal-header">
            <div className="modal-title">➜ Transfert vers un autre poste</div>
            <button className="modal-close" onClick={() => { setShowExternalTransferModal(false); setExternalTargetStation(null); }}>✕</button>
          </div>
          <div className="modal-body">
            {!externalTargetStation ? (
              <>
                <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>
                  Sélectionnez le poste de destination :
                </p>
                {availableStations.length === 0 ? (
                  <div style={{ padding: 24, textAlign: 'center', color: 'var(--text-muted)' }}>
                    Aucun autre poste actif disponible.
                  </div>
                ) : (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                    {availableStations.map(station => (
                      <button 
                        key={station.id} 
                        className="btn btn-ghost" 
                        style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', padding: 16, background: 'var(--bg-glass)', border: '1px solid rgba(255,255,255,0.1)' }}
                        onClick={async () => {
                          setExternalTargetStation(station);
                          try {
                            const res = await fetch(`/api/orders?session_id=${station.active_session_id}`);
                            const data = await res.json();
                            const tabs = Array.from(new Set(data.map((o: any) => o.tab_index))) as number[];
                            setExternalTargetTabs(tabs.length > 0 ? tabs.sort((a,b)=>a-b) : [1]);
                          } catch {
                            setExternalTargetTabs([1]);
                          }
                        }}
                      >
                        <div style={{ fontWeight: 700, fontSize: '1.1rem', marginBottom: 4 }}>
                          {getStationIcon(station.type)} {station.name}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {getStationLabel(station.type)}
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </>
            ) : (
              <>
                <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>
                  Sélectionnez la liste de destination sur <strong>{externalTargetStation.name}</strong> :
                </p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, justifyContent: 'center' }}>
                  {externalTargetTabs.map(tabIdx => (
                    <button 
                      key={tabIdx}
                      className="btn btn-primary"
                      style={{ padding: '16px 24px', fontSize: '1.2rem', background: 'var(--accent-orange)', borderColor: 'var(--accent-orange)' }}
                      onClick={() => executeExternalTransfer(externalTargetStation.active_session_id!, tabIdx)}
                    >
                      Liste {tabIdx}
                    </button>
                  ))}
                  <button 
                    className="btn btn-ghost"
                    style={{ padding: '16px 24px', fontSize: '1.2rem', border: '1px dashed rgba(255,255,255,0.3)' }}
                    onClick={() => executeExternalTransfer(externalTargetStation.active_session_id!, externalTargetTabs.length > 0 ? Math.max(...externalTargetTabs) + 1 : 2)}
                  >
                    + Nouvelle liste
                  </button>
                </div>
              </>
            )}
          </div>
          {externalTargetStation && (
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setExternalTargetStation(null)}>Retour aux postes</button>
            </div>
          )}
        </div>
      </div>
    );
  };

  // ─── Render: Note Modal ──────────────────────────────
  const renderNoteModal = () => {
    if (!showNoteModal) return null;
    return (
      <div className="modal-overlay" onClick={() => setShowNoteModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
          <div className="modal-header">
            <div className="modal-title">📝 Note de session</div>
            <button className="modal-close" onClick={() => setShowNoteModal(false)}>✕</button>
          </div>
          <div className="modal-body">
            <textarea
              style={{
                width: '100%',
                height: '150px',
                padding: '12px',
                borderRadius: '8px',
                background: 'var(--bg-glass)',
                border: '1px solid rgba(255,255,255,0.2)',
                color: 'white',
                fontSize: '1rem',
                resize: 'none'
              }}
              placeholder="Écrivez un commentaire ou une note pour cette session..."
              value={noteInput}
              onChange={(e) => setNoteInput(e.target.value)}
              autoFocus
            />
          </div>
          <div className="modal-footer">
            <button className="btn btn-ghost" onClick={() => setShowNoteModal(false)}>Annuler</button>
            <button className="btn btn-primary" onClick={saveSessionNote}>Sauvegarder</button>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Add User Modal ───────────────────────
  const renderAddUserModal = () => {
    if (!showAddUserModal) return null;

    const handleCreateUser = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      const formData = new FormData(e.currentTarget);
      
      try {
        const res = await fetch('/api/users', {
          method: 'POST',
          body: formData,
        });
        
        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || 'Erreur lors de la création');
        }
        
        showToast('Employé ajouté avec succès ✅', 'success');
        setShowAddUserModal(false);
        fetchTeam();
      } catch (error: any) {
        showToast(error.message, 'error');
      }
    };

    return (
      <div className="modal-overlay" onClick={() => setShowAddUserModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title">👤 Ajouter un Employé</div>
            <button className="modal-close" onClick={() => setShowAddUserModal(false)}>✕</button>
          </div>
          
          <form onSubmit={handleCreateUser} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nom d&apos;utilisateur</label>
              <input name="username" type="text" required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Mot de passe initial</label>
              <input name="password" type="password" required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Rôle</label>
              <select name="role" required style={{ padding: 12, borderRadius: 8, background: '#12121e', border: '1px solid var(--border-glass)', color: 'white' }}>
                <option value="CAISSIER">Caissier</option>
                <option value="SERVEUR">Serveur</option>
                <option value="CHICHISTE">Chichiste</option>
                <option value="CUISINIER">Cuisinier</option>
                <option value="GERANT">Gérant</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Photo de profil (Optionnel)</label>
              <input name="image" type="file" accept="image/*" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>

            <div className="modal-footer" style={{ marginTop: 16 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowAddUserModal(false)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Créer</button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  // ─── Render: Edit User Modal ──────────────────────
  const renderEditUserModal = () => {
    if (!showEditUserModal || !selectedTeamUser) return null;

    const handleEditUser = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      const formData = new FormData(e.currentTarget);
      
      try {
        const res = await fetch(`/api/users/${selectedTeamUser.id}`, {
          method: 'PATCH',
          // Ne PAS définir Content-Type pour que le navigateur génère le boundary multipart automatiquement
          body: formData,
        });
        
        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || 'Erreur lors de la modification');
        }
        
        showToast('Profil modifié ✅', 'success');
        setShowEditUserModal(false);
        fetchTeam();
      } catch (error: any) {
        showToast(error.message, 'error');
      }
    };

    return (
      <div className="modal-overlay" onClick={() => setShowEditUserModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title">✏️ Modifier le profil</div>
            <button className="modal-close" onClick={() => setShowEditUserModal(false)}>✕</button>
          </div>
          
          <form onSubmit={handleEditUser} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nom d&apos;utilisateur</label>
              <input name="username" type="text" defaultValue={selectedTeamUser.username} required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Rôle</label>
              <select name="role" defaultValue={selectedTeamUser.role} required style={{ padding: 12, borderRadius: 8, background: '#12121e', border: '1px solid var(--border-glass)', color: 'white' }}>
                <option value="ADMIN">Admin</option>
                <option value="CAISSIER">Caissier</option>
                <option value="SERVEUR">Serveur</option>
                <option value="CHICHISTE">Chichiste</option>
                <option value="CUISINIER">Cuisinier</option>
                <option value="GERANT">Gérant</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nouvelle photo de profil (facultatif)</label>
              <input name="image" type="file" accept="image/*" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nouveau mot de passe (laisser vide pour ne pas changer)</label>
              <input name="password" type="password" placeholder="••••••••" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 8, borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: 16 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--accent-red)', fontWeight: 600 }}>🔒 Votre mot de passe Admin pour valider :</label>
              <input name="adminPassword" type="password" required placeholder="••••••••" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid rgba(255, 82, 82, 0.4)', color: 'white' }} />
            </div>

            <div className="modal-footer" style={{ marginTop: 16 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowEditUserModal(false)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Enregistrer</button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  // ─── Render: Delete User Confirm Modal ───────────
  const renderDeleteUserModal = () => {
    if (!showDeleteUserModal || !selectedTeamUser) return null;

    const handleDeleteUser = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      const formData = new FormData(e.currentTarget);
      const adminPassword = formData.get('adminPassword');
      
      try {
        const res = await fetch(`/api/users/${selectedTeamUser.id}`, {
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ adminPassword }),
        });
        
        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || 'Erreur lors de la suppression');
        }
        
        showToast('Utilisateur supprimé 🗑️', 'success');
        setShowDeleteUserModal(false);
        fetchTeam();
      } catch (error: any) {
        showToast(error.message, 'error');
      }
    };

    return (
      <div className="modal-overlay" onClick={() => setShowDeleteUserModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 380 }}>
          <div className="modal-header">
            <div className="modal-title">⚠️ Suppression</div>
            <button className="modal-close" onClick={() => setShowDeleteUserModal(false)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)' }}>
              Voulez-vous vraiment supprimer le compte de <strong style={{ color: 'var(--accent-red)' }}>{selectedTeamUser.username}</strong> ?
            </p>
            
            <form onSubmit={handleDeleteUser} style={{ display: 'flex', flexDirection: 'column', gap: 16, marginTop: 16 }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Votre mot de passe (Admin) pour confirmer :</label>
                <input name="adminPassword" type="password" required autoFocus placeholder="••••••••" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
              </div>

              <div className="modal-footer" style={{ marginTop: 8 }}>
                <button type="button" className="btn btn-ghost" onClick={() => setShowDeleteUserModal(false)}>Annuler</button>
                <button type="submit" className="btn btn-danger">🗑️ Confirmer</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render: Add Product Catalog Modal ────────────
  const renderAddProductCatalogModal = () => {
    if (!showAddProductCatalogModal) return null;

    const handleCreateProduct = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      const formData = new FormData(e.currentTarget);
      
      try {
        const res = await fetch('/api/products', {
          method: 'POST',
          body: formData,
        });
        
        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || 'Erreur lors de la création');
        }
        
        showToast('Produit ajouté au catalogue ✅', 'success');
        setShowAddProductCatalogModal(false);
        fetchProducts();
      } catch (error: any) {
        showToast(error.message, 'error');
      }
    };

    return (
      <div className="modal-overlay" onClick={() => setShowAddProductCatalogModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title">🏷️ Ajouter un Produit</div>
            <button className="modal-close" onClick={() => setShowAddProductCatalogModal(false)}>✕</button>
          </div>
          
          <form onSubmit={handleCreateProduct} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nom du produit</label>
              <input name="name" type="text" required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>
            
            <div style={{ display: 'flex', gap: 16 }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Tarif (DA)</label>
                <input name="price" type="number" step="0.01" required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Stock Initial</label>
                <input name="stock_quantity" type="number" defaultValue="0" required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
              </div>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Catégorie</label>
              <select name="category" required style={{ padding: 12, borderRadius: 8, background: '#12121e', border: '1px solid var(--border-glass)', color: 'white' }}>
                <option value="DRINK">Boisson</option>
                <option value="FOOD">Nourriture</option>
                <option value="CHICHA">Chicha</option>
                <option value="AUTRE">Autre</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Image du produit (Optionnelle)</label>
              <input name="image" type="file" accept="image/*" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>

            <div className="modal-footer" style={{ marginTop: 16 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowAddProductCatalogModal(false)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Créer</button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  // ─── Render: Edit Product Catalog Modal ────────────
  const renderEditProductCatalogModal = () => {
    if (!showEditProductCatalogModal || !selectedCatalogProduct) return null;

    const handleEditProduct = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      const formData = new FormData(e.currentTarget);
      
      try {
        const res = await fetch(`/api/products/${selectedCatalogProduct.id}`, {
          method: 'PATCH',
          body: formData,
        });
        
        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || 'Erreur lors de la modification');
        }
        
        showToast('Produit mis à jour ✅', 'success');
        setShowEditProductCatalogModal(false);
        fetchProducts();
      } catch (error: any) {
        showToast(error.message, 'error');
      }
    };

    return (
      <div className="modal-overlay" onClick={() => setShowEditProductCatalogModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title">✏️ Modifier le produit</div>
            <button className="modal-close" onClick={() => setShowEditProductCatalogModal(false)}>✕</button>
          </div>
          
          <form onSubmit={handleEditProduct} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nom du produit</label>
              <input name="name" type="text" defaultValue={selectedCatalogProduct.name} required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>
            
            <div style={{ display: 'flex', gap: 16 }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Tarif (DA)</label>
                <input name="price" type="number" step="0.01" defaultValue={selectedCatalogProduct.price} required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Stock actuel</label>
                <input name="stock_quantity" type="number" defaultValue={selectedCatalogProduct.stock_quantity} required style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
              </div>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Catégorie</label>
              <select name="category" defaultValue={selectedCatalogProduct.category} required style={{ padding: 12, borderRadius: 8, background: '#12121e', border: '1px solid var(--border-glass)', color: 'white' }}>
                <option value="DRINK">Boisson</option>
                <option value="FOOD">Nourriture</option>
                <option value="CHICHA">Chicha</option>
                <option value="AUTRE">Autre</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Nouvelle image (Optionnelle)</label>
              <input name="image" type="file" accept="image/*" style={{ padding: 12, borderRadius: 8, background: 'var(--bg-glass)', border: '1px solid var(--border-glass)', color: 'white' }} />
            </div>

            <div className="modal-footer" style={{ marginTop: 16 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowEditProductCatalogModal(false)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Enregistrer</button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  // ─── Render: Delete Product Catalog Modal ──────────
  const renderDeleteProductCatalogModal = () => {
    if (!showDeleteProductCatalogModal || !selectedCatalogProduct) return null;

    const handleDeleteProduct = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      try {
        const res = await fetch(`/api/products/${selectedCatalogProduct.id}`, {
          method: 'DELETE',
        });
        
        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || 'Erreur lors de la suppression');
        }
        
        showToast('Produit retiré du catalogue 🗑️', 'success');
        setShowDeleteProductCatalogModal(false);
        fetchProducts();
      } catch (error: any) {
        showToast(error.message, 'error');
      }
    };

    return (
      <div className="modal-overlay" onClick={() => setShowDeleteProductCatalogModal(false)}>
        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 380 }}>
          <div className="modal-header">
            <div className="modal-title">⚠️ Retirer du catalogue</div>
            <button className="modal-close" onClick={() => setShowDeleteProductCatalogModal(false)}>✕</button>
          </div>
          <div className="modal-body">
            <p style={{ color: 'var(--text-secondary)' }}>
              Voulez-vous vraiment retirer <strong style={{ color: 'var(--text-primary)' }}>{selectedCatalogProduct.name}</strong> du catalogue des ventes ?
            </p>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: 8 }}>
              Le produit ne sera plus disponible pour les prochaines commandes, mais restera visible dans l'historique des anciennes sessions.
            </p>
            
            <form onSubmit={handleDeleteProduct} style={{ marginTop: 24 }}>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={() => setShowDeleteProductCatalogModal(false)}>Annuler</button>
                <button type="submit" className="btn btn-danger">🗑️ Retirer</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    );
  };

  // ─── Loading State ────────────────────────────────
  if (loading && !dbConnected) {
    return (
      <div style={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center', 
        minHeight: '100vh',
        flexDirection: 'column',
        gap: 16,
      }}>
        <div style={{ fontSize: '3rem' }}>🎮</div>
        <h2>PushPlay Manager V2</h2>
        <p style={{ color: 'var(--text-secondary)' }}>Connexion à la base de données...</p>
        <p style={{ color: 'var(--accent-red)', fontSize: '0.85rem' }}>
          ⚠️ Assurez-vous que Docker est démarré et que MySQL est accessible.
        </p>
      </div>
    );
  }

  // ─── Main Render ──────────────────────────────────
  return (
    <div className="app-layout">
      {renderSidebar()}
      <main className="main-content">
        {renderHeader()}
        {currentPage === 'dashboard' && renderDashboard()}
        {currentPage === 'products' && renderProducts()}
        {currentPage === 'team' && renderTeam()}
      </main>

      {/* Modals */}
      {renderStationModal()}
      {renderGameTimeModal()}
      {renderTransferQtyModal()}
      {renderExternalTransferModal()}
      {renderNoteModal()}
      {renderStopConfirmModal()}
      {renderChronoAlertModal()}
      {renderReceiptModal()}
      {renderCancelConfirmModal()}
      {renderProductModal()}
      {renderAddUserModal()}
      {renderEditUserModal()}
      {renderDeleteUserModal()}
      {renderAddProductCatalogModal()}
      {renderEditProductCatalogModal()}
      {renderDeleteProductCatalogModal()}

      {/* Toast */}

      {toast && (
        <div className={`toast ${toast.type}`}>
          {toast.type === 'success' ? '✅' : '❌'} {toast.message}
        </div>
      )}
    </div>
  );
}
