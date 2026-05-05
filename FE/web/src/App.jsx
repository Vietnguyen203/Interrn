import React, { useState, useEffect, useCallback, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { LayoutDashboard, Users, Utensils, ClipboardList, PieChart, LogOut, Settings, Search, Bell, Menu, User as UserIcon, Filter, Download, Plus, AlertCircle, Calendar, CheckCircle, XCircle, Info, X } from 'lucide-react';
import { apiService } from './services/api';
import './index.css';

// =========================================================
// TOAST NOTIFICATION SYSTEM
// =========================================================
const ToastContext = React.createContext(null);

const ToastContainer = ({ toasts, onRemove }) => (
  <div style={{
    position: 'fixed', top: '20px', right: '20px',
    zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '10px',
    pointerEvents: 'none'
  }}>
    <AnimatePresence>
      {toasts.map(t => {
        const cfg = {
          success: { bg: '#10b981', icon: <CheckCircle size={18} />, label: 'Thành công' },
          error: { bg: '#ef4444', icon: <XCircle size={18} />, label: 'Lỗi' },
          info: { bg: '#6366f1', icon: <Info size={18} />, label: 'Thông báo' },
          warning: { bg: '#f59e0b', icon: <AlertCircle size={18} />, label: 'Cảnh báo' },
        }[t.type] || { bg: '#6366f1', icon: <Info size={18} />, label: '' };
        return (
          <motion.div key={t.id}
            initial={{ opacity: 0, x: 80, scale: 0.9 }}
            animate={{ opacity: 1, x: 0, scale: 1 }}
            exit={{ opacity: 0, x: 80, scale: 0.85 }}
            transition={{ type: 'spring', damping: 20, stiffness: 300 }}
            style={{
              pointerEvents: 'all',
              display: 'flex', alignItems: 'flex-start', gap: '10px',
              background: 'white', borderRadius: '12px',
              boxShadow: '0 8px 32px rgba(0,0,0,0.14)', padding: '14px 16px',
              minWidth: '280px', maxWidth: '360px',
              borderLeft: `4px solid ${cfg.bg}`,
            }}>
            <span style={{ color: cfg.bg, flexShrink: 0, marginTop: '1px' }}>{cfg.icon}</span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ fontSize: '13px', fontWeight: '700', margin: '0 0 2px', color: '#1a1a2e' }}>{cfg.label}</p>
              <p style={{ fontSize: '13px', color: '#555', margin: 0, wordBreak: 'break-word' }}>{t.message}</p>
            </div>
            <button onClick={() => onRemove(t.id)}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#aaa', padding: '0', flexShrink: 0 }}>
              <X size={15} />
            </button>
          </motion.div>
        );
      })}
    </AnimatePresence>
  </div>
);

const useToast = () => {
  const [toasts, setToasts] = useState([]);
  const timerRef = useRef({});

  const remove = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
    clearTimeout(timerRef.current[id]);
  }, []);

  const show = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev.slice(-4), { id, message, type }]);
    timerRef.current[id] = setTimeout(() => remove(id), duration);
    return id;
  }, [remove]);

  const toast = {
    success: (msg, dur) => show(msg, 'success', dur),
    error: (msg, dur) => show(msg, 'error', dur || 5000),
    info: (msg, dur) => show(msg, 'info', dur),
    warning: (msg, dur) => show(msg, 'warning', dur),
  };

  return { toasts, toast, remove };
};

// =========================================================
// CONFIRM DIALOG
// =========================================================
const ConfirmDialog = ({ open, title, message, onConfirm, onCancel, confirmLabel = 'Xác nhận', danger = false }) => {
  if (!open) return null;
  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 10000,
      backgroundColor: 'rgba(0,0,0,0.45)', backdropFilter: 'blur(2px)',
      display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}
      onClick={onCancel}>
      <motion.div
        initial={{ opacity: 0, scale: 0.9, y: -10 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.9 }}
        transition={{ type: 'spring', damping: 22, stiffness: 350 }}
        onClick={e => e.stopPropagation()}
        style={{
          background: 'white', borderRadius: '16px', padding: '28px 32px',
          minWidth: '340px', maxWidth: '440px', width: '90%',
          boxShadow: '0 20px 60px rgba(0,0,0,0.2)',
        }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
          <span style={{
            width: '38px', height: '38px', borderRadius: '50%',
            backgroundColor: danger ? '#fee2e2' : '#eff6ff',
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
          }}>
            <AlertCircle size={20} color={danger ? '#ef4444' : '#6366f1'} />
          </span>
          <h3 style={{ fontSize: '17px', fontWeight: '700', margin: 0, color: '#1a1a2e' }}>{title}</h3>
        </div>
        <p style={{ fontSize: '14px', color: '#555', margin: '0 0 24px', lineHeight: '1.6' }}>{message}</p>
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
          <button onClick={onCancel}
            style={{ padding: '9px 20px', borderRadius: '8px', border: '1px solid #e2e8f0', background: 'white', cursor: 'pointer', fontSize: '14px', fontWeight: '600', color: '#555' }}>
            Hủy bỏ
          </button>
          <button onClick={onConfirm}
            style={{ padding: '9px 20px', borderRadius: '8px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '700', backgroundColor: danger ? '#ef4444' : '#6366f1', color: 'white' }}>
            {confirmLabel}
          </button>
        </div>
      </motion.div>
    </div>
  );
};

const useConfirm = () => {
  const [state, setState] = useState({ open: false, title: '', message: '', danger: false, confirmLabel: 'Xác nhận', resolve: null });

  const confirm = useCallback((title, message, { danger = false, confirmLabel = 'Xác nhận' } = {}) => {
    return new Promise(resolve => {
      setState({ open: true, title, message, danger, confirmLabel, resolve });
    });
  }, []);

  const handleConfirm = useCallback(() => {
    state.resolve?.(true);
    setState(s => ({ ...s, open: false }));
  }, [state]);

  const handleCancel = useCallback(() => {
    state.resolve?.(false);
    setState(s => ({ ...s, open: false }));
  }, [state]);

  const ConfirmUI = (
    <ConfirmDialog
      open={state.open}
      title={state.title}
      message={state.message}
      danger={state.danger}
      confirmLabel={state.confirmLabel}
      onConfirm={handleConfirm}
      onCancel={handleCancel}
    />
  );

  return { confirm, ConfirmUI };
};

// --- Token helper to decode JWT payload (base64) ---
const parseJwt = (token) => {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  } catch (e) {
    return dateString;
  }
};

// ---------------------------------------------------------
// Login Screen
// ---------------------------------------------------------
const LoginScreen = ({ onLoginSuccess }) => {
  const [empId, setEmpId] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [server, setServer] = useState('server-1'); // Default to server-1

  // Forgot Password State
  const [isForgotPassword, setIsForgotPassword] = useState(false);
  const [forgotStep, setForgotStep] = useState(1);
  const [forgotEmail, setForgotEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      // Gọi đúng signature mới: (username, password)
      const response = await apiService.auth.login(empId, password);
      // BE trả về: { code, message, token }
      const token = response.token;
      if (!token) throw new Error('Không nhận được token từ server');
      sessionStorage.setItem('token', token);
      const userPayload = parseJwt(token);
      onLoginSuccess({
        id: userPayload?.sub || empId,
        role: userPayload?.role || 'ADMIN',
        server: 'local',
        birthday: userPayload?.birthday || ''
      });
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const toggleForgotPassword = () => {
    setIsForgotPassword(!isForgotPassword);
    setForgotStep(1);
    setError('');
    setSuccessMessage('');
  };

  const handleSendOTP = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await apiService.auth.forgotPassword(forgotEmail);
      setForgotStep(2);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await apiService.auth.resetPassword(forgotEmail, otp, newPassword);
      setSuccessMessage('Password reset successfully! You can now login.');
      setIsForgotPassword(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex', height: '100vh', width: '100%' }}>
      {/* Left Side Branding */}
      <div style={{ flex: 1, backgroundColor: 'var(--primary)', position: 'relative', overflow: 'hidden', display: 'flex', flexDirection: 'column', padding: '60px', color: 'white' }}>
        <div style={{ zIndex: 10, display: 'flex', alignItems: 'center', gap: '12px', marginBottom: 'auto' }}>
          <div style={{ background: 'white', padding: '10px', borderRadius: '12px' }}>
            <Utensils color="var(--primary)" size={28} />
          </div>
          <h1 style={{ fontSize: '24px', letterSpacing: '-0.5px' }}>FoodOrder Admin</h1>
        </div>
        <div style={{ zIndex: 10, maxWidth: '400px' }}>
          <h2 style={{ fontSize: '48px', lineHeight: '1.1', marginBottom: '20px' }}>Manage your restaurant beautifully.</h2>
          <p style={{ fontSize: '18px', color: 'rgba(255,255,255,0.8)' }}>Access the dashboard to oversee orders, manage staff, and analyze revenue in real-time.</p>
        </div>
        <div style={{ position: 'absolute', top: '-10%', right: '-10%', width: '500px', height: '500px', borderRadius: '50%', background: 'radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%)', zIndex: 0 }}></div>
      </div>

      {/* Right Side Login Form */}
      <div style={{ flex: 1, backgroundColor: 'var(--bg-surface)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px' }}>
        <div style={{ width: '100%', maxWidth: '420px' }}>

          <div style={{ marginBottom: '40px' }}>
            <h2 style={{ fontSize: '32px', color: 'var(--text-primary)', marginBottom: '8px' }}>
              {isForgotPassword ? (forgotStep === 1 ? 'Reset Password' : 'Enter OTP') : 'Welcome back'}
            </h2>
            <p style={{ color: 'var(--text-secondary)' }}>
              {isForgotPassword
                ? (forgotStep === 1 ? 'Enter your email to receive an OTP.' : 'Check your email for the 6-digit OTP code.')
                : 'Please enter your details to sign in.'}
            </p>
          </div>

          {error && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', backgroundColor: 'rgba(244, 67, 54, 0.1)', color: 'var(--status-cancelled)', borderRadius: 'var(--radius-md)', marginBottom: '20px', fontSize: '14px' }}>
              <AlertCircle size={16} /> {error}
            </div>
          )}

          {successMessage && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', backgroundColor: 'rgba(76, 175, 80, 0.1)', color: 'var(--status-completed)', borderRadius: 'var(--radius-md)', marginBottom: '20px', fontSize: '14px' }}>
              <AlertCircle size={16} /> {successMessage}
            </div>
          )}

          {!isForgotPassword ? (
            // --- LOGIN FORM ---
            <form onSubmit={handleLogin}>
              <div className="form-group">
                <label className="form-label">Employee ID</label>
                <input type="text" className="form-input" value={empId} onChange={e => setEmpId(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input type="password" className="form-input" value={password} onChange={e => setPassword(e.target.value)} required />
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px', color: 'var(--text-secondary)' }}>
                  <input type="checkbox" style={{ accentColor: 'var(--primary)', width: '16px', height: '16px' }} /> Remember me
                </label>
                <button type="button" onClick={toggleForgotPassword} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '14px', color: 'var(--primary)', fontWeight: '500', padding: 0 }}>Forgot password?</button>
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '16px' }} disabled={loading}>
                {loading ? 'Signing in...' : 'Sign In'}
              </button>
            </form>
          ) : forgotStep === 1 ? (
            // --- FORGOT PASSWORD STEP 1 ---
            <form onSubmit={handleSendOtp}>
              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input type="email" className="form-input" placeholder="user@example.com" value={forgotEmail} onChange={e => setForgotEmail(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '16px', marginBottom: '16px' }} disabled={loading}>
                {loading ? 'Sending OTP...' : 'Send OTP'}
              </button>
              <div style={{ textAlign: 'center' }}>
                <button type="button" onClick={toggleForgotPassword} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '14px', color: 'var(--text-secondary)', fontWeight: '500' }}>← Back to Login</button>
              </div>
            </form>
          ) : (
            // --- FORGOT PASSWORD STEP 2 ---
            <form onSubmit={handleResetPassword}>
              <div className="form-group">
                <label className="form-label">6-Digit OTP</label>
                <input type="text" className="form-input" placeholder="123456" value={otp} onChange={e => setOtp(e.target.value)} required maxLength={6} />
              </div>
              <div className="form-group">
                <label className="form-label">New Password</label>
                <input type="password" className="form-input" placeholder="Min 6 characters" value={newPassword} onChange={e => setNewPassword(e.target.value)} required minLength={6} />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '16px', marginBottom: '16px' }} disabled={loading}>
                {loading ? 'Resetting...' : 'Reset Password'}
              </button>
              <div style={{ textAlign: 'center' }}>
                <button type="button" onClick={toggleForgotPassword} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '14px', color: 'var(--text-secondary)', fontWeight: '500' }}>← Cancel</button>
              </div>
            </form>
          )}

          {!isForgotPassword && (
            <div style={{ textAlign: 'center', marginTop: '24px' }}>
              <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>Don't have an account? </span>
              <a href="#" style={{ fontSize: '14px', color: 'var(--primary)', textDecoration: 'none', fontWeight: '500' }}>Contact Admin</a>
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
};

// ---------------------------------------------------------
// Dashboard Screen
// ---------------------------------------------------------
const DashboardScreen = ({ user, onLogout }) => {
  const { toasts, toast, remove: removeToast } = useToast();
  const { confirm, ConfirmUI } = useConfirm();
  const [activeTab, setActiveTab] = useState(user?.role === 'KITCHEN' ? 'Kitchen' : 'Overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  // Data States
  const [tables, setTables] = useState([]);
  const [recentOrders, setRecentOrders] = useState([]);
  const [allOrders, setAllOrders] = useState([]);
  const [foods, setFoods] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null); // null = đang xem danh mục; object = đang xem món của danh mục đó

  const [staff, setStaff] = useState([]);

  // Modal States
  const [isFoodModalOpen, setIsFoodModalOpen] = useState(false);
  const [editingFood, setEditingFood] = useState(null);
  const [foodFormData, setFoodFormData] = useState({
    code: '', foodName: '', price: '', categoryId: '', imageUrl: ''
  });

  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [categoryFormData, setCategoryFormData] = useState({
    code: '', name: '', description: ''
  });

  const [isStaffModalOpen, setIsStaffModalOpen] = useState(false);
  const [editingStaff, setEditingStaff] = useState(null);
  const [staffFormData, setStaffFormData] = useState({
    username: '', password: '', fullName: '', email: '', phoneNumber: '', birthday: '', role: 'WAITER', server: 'HCM', gender: 'MALE'
  });

  const [loadingConfig, setLoadingConfig] = useState({
    overview: true, orders: false, foods: false, staff: false
  });

  const navItems = [
    { icon: <LayoutDashboard size={20} />, label: 'Overview', roles: ['ADMIN'] },
    { icon: <ClipboardList size={20} />, label: 'Orders', roles: ['ADMIN'] },
    { icon: <Utensils size={20} />, label: 'Kitchen', roles: ['ADMIN', 'KITCHEN'] },
    { icon: <Utensils size={20} />, label: 'Menu & Food', roles: ['ADMIN'] },
    { icon: <Users size={20} />, label: 'Staff', roles: ['ADMIN'] },
    { icon: <PieChart size={20} />, label: 'Reports', roles: ['ADMIN'] },
    { icon: <Settings size={20} />, label: 'Settings', roles: ['ADMIN'] },
  ];

  // Filter nav items based on user role
  const filteredNavItems = navItems.filter(item => item.roles.includes(user?.role || 'USER'));

  // Fetch data based on active tab
  useEffect(() => {
    if (activeTab === 'Overview') fetchOverviewData();
    else if (activeTab === 'Orders') fetchOrdersData();
    else if (activeTab === 'Menu & Food') fetchFoodsData();
    else if (activeTab === 'Staff') fetchStaffData();
    else if (activeTab === 'Kitchen') fetchKitchenData();
  }, [activeTab]);

  const [kitchenItems, setKitchenItems] = useState([]);
  const fetchKitchenData = async () => {
    setLoadingConfig(prev => ({ ...prev, kitchen: true }));
    try {
      // Lấy cả đơn PENDING và CONFIRMED, flatten items để bếp thấy từng món
      const [pendingRes, confirmedRes] = await Promise.allSettled([
        apiService.kitchen.getPendingOrders(),
        apiService.kitchen.getConfirmedOrders(),
      ]);

      const orders = [
        ...(pendingRes.status === 'fulfilled' ? (pendingRes.value.data || []) : []),
        ...(confirmedRes.status === 'fulfilled' ? (confirmedRes.value.data || []) : []),
      ];

      // Flatten: mỗi item giữ thông tin đơn (bàn, orderId, orderStatus)
      const flatItems = orders.flatMap(order =>
        (order.items || []).map(item => ({
          ...item,
          tableNumber: order.tableNumber || order.tableId || '—',
          orderId: order.id,
          orderStatus: order.status,
          createdAt: order.createdAt,
        }))
      ).filter(item => item.kitchenStatus !== 'SERVED'); // ẩn món đã phục vụ xong

      setKitchenItems(flatItems);
    } catch (error) { console.error('Error fetching kitchen data:', error); }
    finally { setLoadingConfig(prev => ({ ...prev, kitchen: false })); }
  };

  const handleUpdateItemStatus = async (itemId, newStatus) => {
    try {
      await apiService.kitchen.updateItemStatus(itemId, newStatus);
      const labels = { COOKING: 'đang nấu', READY: 'sẵn sàng', SERVED: 'đã phục vụ' };
      toast.success(`Cập nhật món → ${labels[newStatus] || newStatus}`);
      fetchKitchenData();
    } catch (error) { toast.error('Lỗi cập nhật: ' + error.message); }
  };

  const fetchOverviewData = async () => {
    setLoadingConfig(prev => ({ ...prev, overview: true }));
    try {
      const ordersRes = await apiService.order.getAll();
      if (ordersRes.data) {
        setRecentOrders(ordersRes.data.slice(0, 5));
        setAllOrders(ordersRes.data);
      }
    } catch { }
    setLoadingConfig(prev => ({ ...prev, overview: false }));
  };



  const fetchOrdersData = async (statusFilter) => {
    setLoadingConfig(prev => ({ ...prev, orders: true }));
    try {
      const res = await apiService.order.getAll(statusFilter);
      if (res.data) setAllOrders(res.data);
    } catch (error) { toast.error('Không thể tải đơn hàng: ' + error.message); }
    finally { setLoadingConfig(prev => ({ ...prev, orders: false })); }
  };

  const handleCancelOrder = async (orderId) => {
    const ok = await confirm('Hủy đơn hàng', 'Bạn có chắc muốn hủy đơn hàng này không?', { danger: true, confirmLabel: 'Hủy đơn' });
    if (!ok) return;
    try {
      await apiService.order.cancel(orderId);
      toast.success('Đã hủy đơn hàng');
      fetchOrdersData();
    } catch (e) { toast.error('Lỗi hủy đơn: ' + e.message); }
  };

  const handleUpdateOrderStatus = async (orderId, status) => {
    try {
      await apiService.order.updateStatus(orderId, status);
      const labels = { CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy' };
      toast.success(`Đơn hàng → ${labels[status] || status}`);
      fetchOrdersData();
    } catch (e) { toast.error('Lỗi cập nhật đơn: ' + e.message); }
  };

  const fetchFoodsData = async () => {
    setLoadingConfig(prev => ({ ...prev, foods: true }));
    try {
      const [itemsRes, catsRes] = await Promise.all([
        apiService.catalog.getItems(),
        apiService.catalog.getCategories(),
      ]);
      if (itemsRes.data) setFoods(itemsRes.data);
      if (catsRes.data) setCategories(catsRes.data);
    } catch (error) { toast.error('Không thể tải thực đơn: ' + error.message); }
    finally { setLoadingConfig(prev => ({ ...prev, foods: false })); }
  };

  const fetchStaffData = async () => {
    setLoadingConfig(prev => ({ ...prev, staff: true }));
    try {
      const server = user?.server || 'HCM';
      const res = await apiService.dashboard.getStaff(server);
      if (res.data) setStaff(res.data);
    } catch (error) {
      toast.error('Không thể tải danh sách nhân viên: ' + error.message);
    } finally {
      setLoadingConfig(prev => ({ ...prev, staff: false }));
    }
  };

  const statCards = [
    { title: 'Total Revenue', value: '$12,426', change: '+14%', positive: true },
    { title: 'Total Orders', value: allOrders.length || recentOrders.length || 0, change: 'Today', positive: true },
    { title: 'Total Menu Items', value: foods.length || 0, change: 'Active', positive: true },
    { title: 'Total Tables', value: tables.length || 0, change: `${tables.filter(t => t.currentOrderId).length} Occupied`, positive: true },
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return 'var(--status-completed)';
      case 'CONFIRMED': return 'var(--status-ordering)';
      case 'PENDING': return '#f59e0b';  // amber
      case 'CANCELLED': return 'var(--status-cancelled)';
      case 'ORDERING': return 'var(--status-ordering)';
      default: return 'var(--text-secondary)';
    }
  };

  // ---- Handlers ----
  const handleLogoutClick = () => {
    sessionStorage.clear();
    onLogout();
  };

  const handleOpenFoodModal = (food = null) => {
    if (food) {
      setEditingFood(food);
      setFoodFormData({ code: food.code || '', foodName: food.foodName || '', price: food.price || '', categoryId: food.categoryId || '', imageUrl: food.imageUrl || '' });
    } else {
      setEditingFood(null);
      setFoodFormData({ code: '', foodName: '', price: '', categoryId: categories[0]?.id || '', imageUrl: '' });
    }
    setIsFoodModalOpen(true);
  };

  const handleSaveFood = async (e) => {
    e.preventDefault();
    try {
      const payload = { ...foodFormData, price: parseFloat(foodFormData.price) };
      if (editingFood) {
        await apiService.catalog.updateItem(editingFood.id, payload);
        toast.success('Cập nhật món ăn thành công');
      } else {
        await apiService.catalog.createItem(payload);
        toast.success('Thêm món ăn thành công');
      }
      setIsFoodModalOpen(false);
      fetchFoodsData();
    } catch (err) { toast.error('Lỗi lưu món ăn: ' + err.message); }
  };

  const handleDeleteFood = async (id) => {
    const ok = await confirm('Xóa món ăn', 'Bạn có chắc muốn xóa món ăn này không?', { danger: true, confirmLabel: 'Xóa' });
    if (!ok) return;
    try {
      await apiService.catalog.deleteItem(id);
      toast.success('Đã xóa món ăn');
      fetchFoodsData();
    } catch (err) { toast.error('Lỗi xóa món ăn: ' + err.message); }
  };

  const handleOpenCategoryModal = (cat = null) => {
    if (cat) {
      setEditingCategory(cat);
      setCategoryFormData({ code: cat.code || '', name: cat.name || '', description: cat.description || '' });
    } else {
      setEditingCategory(null);
      setCategoryFormData({ code: '', name: '', description: '' });
    }
    setIsCategoryModalOpen(true);
  };

  const handleSaveCategory = async (e) => {
    e.preventDefault();
    try {
      if (editingCategory) {
        await apiService.catalog.updateCategory(editingCategory.id, categoryFormData);
        toast.success('Cập nhật danh mục thành công');
      } else {
        await apiService.catalog.createCategory(categoryFormData);
        toast.success('Thêm danh mục thành công');
      }
      setIsCategoryModalOpen(false);
      fetchFoodsData();
    } catch (err) { toast.error('Lỗi lưu danh mục: ' + err.message); }
  };

  const handleDeleteCategory = async (id) => {
    const ok = await confirm('Xóa danh mục', 'Các món ăn thuộc danh mục này sẽ bị ảnh hưởng. Bạn có chắc muốn xóa?', { danger: true, confirmLabel: 'Xóa danh mục' });
    if (!ok) return;
    try {
      await apiService.catalog.deleteCategory(id);
      toast.success('Đã xóa danh mục');
      fetchFoodsData();
    } catch (err) { toast.error('Lỗi xóa danh mục: ' + err.message); }
  };

  const handleOpenStaffModal = (person = null) => {
    if (person) {
      setEditingStaff(person);
      setStaffFormData({
        ...person,
        username: person.employeeId, // Use employeeId as username for display/tracking
        password: '',
        birthday: person.birthday || '',
        gender: person.gender || 'MALE'
      });
    } else {
      setEditingStaff(null);
      setStaffFormData({ username: '', password: '', fullName: '', email: '', phoneNumber: '', birthday: '', role: 'WAITER', server: user?.server || 'HCM', gender: 'MALE' });
    }
    setIsStaffModalOpen(true);
  };

  const handleSaveStaff = async (e) => {
    e.preventDefault();
    try {
      const payload = { ...staffFormData };
      if (editingStaff && !payload.password) {
        delete payload.password;
      }
      if (editingStaff) {
        await apiService.dashboard.updateStaff(editingStaff.server, editingStaff.uid, payload);
        toast.success('Cập nhật nhân viên thành công');
      } else {
        await apiService.dashboard.createStaff(payload);
        toast.success('Thêm nhân viên thành công');
      }
      setIsStaffModalOpen(false);
      fetchStaffData();
    } catch (err) {
      toast.error('Lỗi lưu nhân viên: ' + err.message);
    }
  };

  const handleDeleteStaff = async (server, employeeId) => {
    const ok = await confirm('Xóa nhân viên', 'Bạn có chắc muốn xóa nhân viên này không?', { danger: true, confirmLabel: 'Xóa' });
    if (!ok) return;
    try {
      await apiService.dashboard.deleteStaff(server, employeeId);
      toast.success('Đã xóa nhân viên');
      fetchStaffData();
    } catch (err) {
      toast.error('Lỗi xóa nhân viên: ' + err.message);
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', width: '100%', overflow: 'hidden', backgroundColor: 'var(--bg-app)' }}>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
      {ConfirmUI}
      {/* Sidebar */}
      <motion.aside initial={false} animate={{ width: isSidebarOpen ? '260px' : '0px', opacity: isSidebarOpen ? 1 : 0 }} style={{ backgroundColor: 'var(--bg-surface)', borderRight: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', overflow: 'hidden', whiteSpace: 'nowrap' }}>
        <div style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ background: 'var(--primary)', padding: '8px', borderRadius: '10px' }}>
            <Utensils color="white" size={20} />
          </div>
          <h2 style={{ fontSize: '20px', color: 'var(--primary)' }}>FoodOrder</h2>
        </div>

        <nav style={{ flex: 1, padding: '24px 16px', display: 'flex', flexDirection: 'column', gap: '8px', overflowY: 'auto' }}>
          <div style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '8px', paddingLeft: '8px' }}>Menu</div>
          {filteredNavItems.map((item, idx) => (
            <button key={idx} onClick={() => setActiveTab(item.label)} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px 16px', borderRadius: 'var(--radius-md)', backgroundColor: activeTab === item.label ? 'var(--primary-light)' : 'transparent', color: activeTab === item.label ? 'var(--primary)' : 'var(--text-secondary)', border: 'none', cursor: 'pointer', textAlign: 'left', width: '100%', fontWeight: activeTab === item.label ? '600' : '400', transition: 'var(--transition)' }}>
              {item.icon} <span style={{ fontSize: '15px' }}>{item.label}</span>
            </button>
          ))}
        </nav>

        {/* User Card from JWT info */}
        <div style={{ padding: '20px', borderTop: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <UserIcon color="var(--primary)" size={20} />
            </div>
            <div style={{ flex: 1, overflow: 'hidden' }}>
              <p style={{ fontSize: '14px', fontWeight: '600', color: 'var(--text-primary)', textTransform: 'capitalize', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>{user?.id}</p>
              <p style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Calendar size={10} /> {formatDate(user?.birthday) || 'N/A'}
              </p>
              <p style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Sys: {user?.server} • {user?.role}</p>
            </div>
            <button onClick={handleLogoutClick} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)' }} title="Logout">
              <LogOut size={20} />
            </button>
          </div>
        </div>
      </motion.aside>

      {/* Main Content */}
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <header style={{ height: '72px', backgroundColor: 'var(--bg-surface)', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}><Menu size={24} /></button>
            <h1 style={{ fontSize: '24px', fontWeight: '600', color: 'var(--text-primary)' }}>{activeTab}</h1>
          </div>
        </header>

        <div style={{ flex: 1, overflowY: 'auto', padding: '32px' }}>

          {/* OVERVIEW TAB */}
          {activeTab === 'Overview' && (
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="animate-fade-in">
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '24px', marginBottom: '32px' }}>
                {statCards.map((stat, idx) => (
                  <div key={idx} className="card" style={{ padding: '24px' }}>
                    <h3 style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '12px', fontWeight: '500' }}>{stat.title}</h3>
                    <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
                      <span style={{ fontSize: '32px', fontWeight: '700', color: 'var(--primary)', lineHeight: '1' }}>{stat.value}</span>
                      <span style={{ fontSize: '14px', fontWeight: '600', color: stat.positive ? 'var(--status-completed)' : 'var(--text-secondary)' }}>{stat.change}</span>
                    </div>
                  </div>
                ))}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
                <div className="card" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: '600' }}>Recent Orders</h3>
                    <button onClick={fetchOverviewData} className="btn-ghost" style={{ padding: '4px 8px', fontSize: '14px', border: 'none' }}>Refresh</button>
                  </div>
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                      <thead>
                        <tr style={{ borderBottom: '1px solid var(--border-color)' }}>
                          <th style={{ padding: '12px', color: 'var(--text-secondary)', fontWeight: '500', fontSize: '14px' }}>Order ID</th>
                          <th style={{ padding: '12px', color: 'var(--text-secondary)', fontWeight: '500', fontSize: '14px' }}>Table ID</th>
                          <th style={{ padding: '12px', color: 'var(--text-secondary)', fontWeight: '500', fontSize: '14px' }}>Amount</th>
                          <th style={{ padding: '12px', color: 'var(--text-secondary)', fontWeight: '500', fontSize: '14px' }}>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {loadingConfig.overview ? (
                          <tr><td colSpan="4" style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading...</td></tr>
                        ) : recentOrders.length === 0 ? (
                          <tr><td colSpan="4" style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>No orders found.</td></tr>
                        ) : (
                          recentOrders.map((order, idx) => (
                            <tr key={idx} style={{ borderBottom: '1px solid var(--bg-app)' }}>
                              <td style={{ padding: '16px 12px', fontWeight: '500' }}>{order.id.slice(0, 8)}...</td>
                              <td style={{ padding: '16px 12px', color: 'var(--text-secondary)' }}>{order.tableId}</td>
                              <td style={{ padding: '16px 12px', fontWeight: '600' }}>{order.totalAmount}</td>
                              <td style={{ padding: '16px 12px' }}>
                                <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: '600', backgroundColor: `${getStatusColor(order.status)}20`, color: getStatusColor(order.status) }}>
                                  {order.status}
                                </span>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>

                <div className="card" style={{ padding: '24px', gridColumn: '2 / 3', gridRow: '1 / 2' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: '600' }}>Table Status</h3>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                    {loadingConfig.overview ? (
                      <p style={{ color: 'var(--text-secondary)', gridColumn: '1 / -1', textAlign: 'center' }}>Loading...</p>
                    ) : tables.length === 0 ? (
                      <p style={{ color: 'var(--text-secondary)', gridColumn: '1 / -1', textAlign: 'center' }}>No tables.</p>
                    ) : tables.map((table) => {
                      const isOccupied = table.currentOrderId != null;
                      return (
                        <div key={table.id} style={{ padding: '16px', borderRadius: 'var(--radius-md)', textAlign: 'center', border: `1px solid ${isOccupied ? 'var(--secondary)' : 'var(--border-color)'}`, backgroundColor: isOccupied ? 'rgba(9, 52, 219, 0.05)' : 'var(--bg-app)' }}>
                          <span style={{ display: 'block', fontSize: '14px', fontWeight: '600', marginBottom: '4px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{table.tableName}</span>
                          <span className={`badge ${isOccupied ? 'badge-occupied' : 'badge-empty'}`}>
                            {isOccupied ? 'Occupied' : 'Empty'}
                          </span>
                        </div>
                      )
                    })}
                  </div>
                </div>
              </div>
            </motion.div>
          )}

          {/* KITCHEN TAB */}
          {activeTab === 'Kitchen' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <div>
                  <h3 style={{ fontSize: '20px', fontWeight: '700', margin: 0 }}>🍳 Bếp — Đơn cần chế biến</h3>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                    {kitchenItems.filter(i => i.kitchenStatus === 'PENDING').length} chờ &middot;{' '}
                    {kitchenItems.filter(i => i.kitchenStatus === 'COOKING').length} đang nấu &middot;{' '}
                    {kitchenItems.filter(i => i.kitchenStatus === 'READY').length} sẵn sàng
                  </p>
                </div>
                <button onClick={fetchKitchenData} className="btn btn-outline" style={{ padding: '8px 16px', fontSize: '14px' }}>🔄 Làm mới</button>
              </div>

              {loadingConfig.kitchen ? (
                <p style={{ textAlign: 'center', padding: '60px', color: 'var(--text-secondary)' }}>Đang tải dữ liệu bếp...</p>
              ) : kitchenItems.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '80px', color: 'var(--text-secondary)' }}>
                  <Utensils size={52} style={{ margin: '0 auto 16px', opacity: 0.25 }} />
                  <p style={{ fontWeight: '600', fontSize: '16px' }}>Không có món nào cần chế biến</p>
                  <p style={{ fontSize: '13px', marginTop: '4px' }}>Tất cả đơn đã được phục vụ hoặc chưa có đơn mới.</p>
                </div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(290px, 1fr))', gap: '20px' }}>
                  {kitchenItems.map(item => {
                    const ks = item.kitchenStatus;
                    const borderColor = ks === 'PENDING' ? '#f59e0b' : ks === 'COOKING' ? '#ef4444' : ks === 'READY' ? '#10b981' : '#6b7280';
                    return (
                      <div key={item.id} className="card" style={{ padding: '20px', borderLeft: `4px solid ${borderColor}` }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                          <div>
                            <span style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>
                              🪑 {item.tableNumber}
                            </span>
                            <h4 style={{ fontSize: '17px', fontWeight: '700', margin: '4px 0 0' }}>{item.foodName}</h4>
                          </div>
                          <span style={{ padding: '3px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '700', backgroundColor: `${borderColor}20`, color: borderColor, whiteSpace: 'nowrap', marginLeft: '8px' }}>
                            {ks === 'PENDING' ? '⏳ Chờ' : ks === 'COOKING' ? '🔥 Nấu' : ks === 'READY' ? '✅ Sẵn sàng' : ks}
                          </span>
                        </div>

                        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                          Số lượng: <strong style={{ fontSize: '16px', color: 'var(--text-primary)' }}>x{item.quantity}</strong>
                        </p>

                        {item.note && (
                          <p style={{ fontSize: '13px', fontStyle: 'italic', backgroundColor: '#fffbeb', padding: '8px 10px', borderRadius: '6px', marginBottom: '12px', color: '#92400e', border: '1px solid #fde68a' }}>
                            📝 {item.note}
                          </p>
                        )}

                        <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
                          {ks === 'PENDING' && (
                            <button onClick={() => handleUpdateItemStatus(item.id, 'COOKING')}
                              style={{ flex: 1, padding: '8px', fontSize: '13px', fontWeight: '700', borderRadius: '8px', border: 'none', cursor: 'pointer', backgroundColor: '#ef4444', color: 'white' }}>
                              🔥 Bắt đầu nấu
                            </button>
                          )}
                          {ks === 'COOKING' && (
                            <button onClick={() => handleUpdateItemStatus(item.id, 'READY')}
                              style={{ flex: 1, padding: '8px', fontSize: '13px', fontWeight: '700', borderRadius: '8px', border: 'none', cursor: 'pointer', backgroundColor: '#10b981', color: 'white' }}>
                              ✅ Hoàn thành
                            </button>
                          )}
                          {ks === 'READY' && (
                            <button onClick={() => handleUpdateItemStatus(item.id, 'SERVED')}
                              style={{ flex: 1, padding: '8px', fontSize: '13px', fontWeight: '700', borderRadius: '8px', border: 'none', cursor: 'pointer', backgroundColor: '#6366f1', color: 'white' }}>
                              🛎️ Đã phục vụ
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </motion.div>
          )}

          {/* ORDERS TAB */}
          {activeTab === 'Orders' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card" style={{ padding: '24px' }}>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div>
                  <h3 style={{ fontSize: '20px', fontWeight: '700', margin: 0 }}>📋 Quản lý Đơn hàng</h3>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>{allOrders.length} đơn hàng</p>
                </div>
                <button onClick={() => fetchOrdersData()} className="btn btn-outline" style={{ padding: '8px 16px', fontSize: '14px' }}>🔄 Làm mới</button>
              </div>

              {/* Status Filter Tabs */}
              <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', flexWrap: 'wrap' }}>
                {[null, 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'].map(s => (
                  <button key={s ?? 'all'} onClick={() => fetchOrdersData(s)}
                    className="btn" style={{
                      padding: '6px 16px', fontSize: '13px', fontWeight: '600',
                      backgroundColor: 'var(--bg-app)', border: '1px solid var(--border-color)',
                      color: 'var(--text-secondary)', borderRadius: '20px', cursor: 'pointer'
                    }}>
                    {s === null ? '🔍 Tất cả' : s === 'PENDING' ? '⏳ Chờ xác nhận' : s === 'CONFIRMED' ? '✅ Đã xác nhận' : s === 'COMPLETED' ? '🎉 Hoàn thành' : '❌ Đã hủy'}
                  </button>
                ))}
              </div>

              {loadingConfig.orders ? (
                <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Đang tải đơn hàng...</p>
              ) : (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                      <tr style={{ borderBottom: '2px solid var(--border-color)', backgroundColor: 'var(--bg-app)' }}>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Mã đơn</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Bàn</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Tổng tiền</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Người tạo</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Ngày tạo</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Trạng thái</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '600', fontSize: '13px' }}>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {allOrders.map((order) => (
                        <tr key={order.id} style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.15s' }}
                          onMouseEnter={e => e.currentTarget.style.backgroundColor = 'var(--bg-app)'}
                          onMouseLeave={e => e.currentTarget.style.backgroundColor = 'transparent'}>
                          <td style={{ padding: '14px 16px', fontFamily: 'monospace', fontSize: '12px', color: 'var(--text-secondary)' }}>
                            {String(order.id).slice(0, 8)}…
                          </td>
                          <td style={{ padding: '14px 16px', fontWeight: '600' }}>
                            {order.tableNumber || order.tableId || '—'}
                          </td>
                          <td style={{ padding: '14px 16px', fontWeight: '700', color: 'var(--status-ordering)' }}>
                            {Number(order.totalAmount || 0).toLocaleString('vi-VN')}đ
                          </td>
                          <td style={{ padding: '14px 16px', color: 'var(--text-secondary)', fontSize: '13px' }}>{order.createdBy || '—'}</td>
                          <td style={{ padding: '14px 16px', color: 'var(--text-secondary)', fontSize: '13px' }}>{formatDate(order.createdAt)}</td>
                          <td style={{ padding: '14px 16px' }}>
                            <span style={{
                              padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: '600',
                              backgroundColor: `${getStatusColor(order.status)}15`,
                              color: getStatusColor(order.status)
                            }}>
                              {order.status === 'PENDING' ? 'Chờ xác nhận'
                                : order.status === 'CONFIRMED' ? 'Đã xác nhận'
                                  : order.status === 'COMPLETED' ? 'Hoàn thành'
                                    : order.status === 'CANCELLED' ? 'Đã hủy'
                                      : order.status}
                            </span>
                          </td>
                          <td style={{ padding: '14px 16px' }}>
                            <div style={{ display: 'flex', gap: '6px' }}>
                              {order.status === 'PENDING' && (
                                <button onClick={() => handleUpdateOrderStatus(order.id, 'CONFIRMED')}
                                  style={{ padding: '4px 10px', fontSize: '12px', fontWeight: '600', borderRadius: '6px', border: 'none', cursor: 'pointer', backgroundColor: 'var(--primary)', color: 'white' }}>
                                  Xác nhận
                                </button>
                              )}
                              {order.status === 'CONFIRMED' && (
                                <button onClick={() => handleUpdateOrderStatus(order.id, 'COMPLETED')}
                                  style={{ padding: '4px 10px', fontSize: '12px', fontWeight: '600', borderRadius: '6px', border: 'none', cursor: 'pointer', backgroundColor: 'var(--status-completed)', color: 'white' }}>
                                  Hoàn thành
                                </button>
                              )}
                              {(order.status === 'PENDING' || order.status === 'CONFIRMED') && (
                                <button onClick={() => handleCancelOrder(order.id)}
                                  style={{ padding: '4px 10px', fontSize: '12px', fontWeight: '600', borderRadius: '6px', border: 'none', cursor: 'pointer', backgroundColor: 'var(--status-cancelled)', color: 'white' }}>
                                  Hủy
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))}
                      {allOrders.length === 0 && (
                        <tr>
                          <td colSpan="7" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                            <ClipboardList size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} />
                            <p>Chưa có đơn hàng nào.</p>
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </motion.div>
          )}



          {/* MENU & FOOD TAB */}

          {activeTab === 'Menu & Food' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>

              {loadingConfig.foods ? (
                <p style={{ textAlign: 'center', padding: '60px', color: 'var(--text-secondary)' }}>Đang tải dữ liệu...</p>
              ) : selectedCategory === null ? (

                /* ===== MÀN HÌNH 1: LƯỚI DANH MỤC ===== */
                <>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                    <div>
                      <h3 style={{ fontSize: '22px', fontWeight: '700', margin: 0 }}>📂 Danh mục thực đơn</h3>
                      <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>Chọn danh mục để xem và quản lý món ăn</p>
                    </div>
                    <button onClick={() => handleOpenCategoryModal()} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Plus size={16} /> Thêm Danh Mục
                    </button>
                  </div>

                  {categories.length === 0 ? (
                    <div className="card" style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                      <p style={{ fontSize: '48px', margin: '0 0 16px' }}>📂</p>
                      <p style={{ fontWeight: '600' }}>Chưa có danh mục nào.</p>
                      <p style={{ fontSize: '13px' }}>Hãy tạo danh mục đầu tiên để bắt đầu quản lý thực đơn!</p>
                    </div>
                  ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px' }}>
                      {categories.map(cat => (
                        <div key={cat.id} className="card" style={{ padding: '0', overflow: 'hidden', cursor: 'pointer', transition: 'var(--transition)', border: '2px solid transparent' }}
                          onClick={() => setSelectedCategory(cat)}
                          onMouseEnter={e => e.currentTarget.style.borderColor = 'var(--primary)'}
                          onMouseLeave={e => e.currentTarget.style.borderColor = 'transparent'}
                        >
                          {/* Header màu */}
                          <div style={{ height: '8px', background: 'linear-gradient(90deg, var(--primary), var(--primary-hover))' }} />
                          <div style={{ padding: '20px' }}>
                            <p style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '6px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>#{cat.code}</p>
                            <h4 style={{ fontSize: '18px', fontWeight: '700', margin: '0 0 6px', color: 'var(--text-primary)' }}>{cat.name}</h4>
                            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '0 0 16px', minHeight: '20px' }}>{cat.description || '—'}</p>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--primary)', background: 'var(--primary-light)', padding: '4px 10px', borderRadius: '20px' }}>
                                {foods.filter(f => f.categoryId === cat.id).length} món
                              </span>
                              <div style={{ display: 'flex', gap: '4px' }} onClick={e => e.stopPropagation()}>
                                <button onClick={() => handleOpenCategoryModal(cat)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)', padding: '4px' }}><Settings size={15} /></button>
                                <button onClick={() => handleDeleteCategory(cat.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)', padding: '4px' }}><LogOut size={15} /></button>
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </>

              ) : (

                /* ===== MÀN HÌNH 2: DANH SÁCH MÓN ĂN THEO DANH MỤC ===== */
                <>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <button onClick={() => setSelectedCategory(null)} style={{ background: 'var(--bg-app)', border: 'none', borderRadius: '8px', padding: '8px 12px', cursor: 'pointer', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px', fontWeight: '600' }}>
                        ← Danh mục
                      </button>
                      <div>
                        <h3 style={{ fontSize: '20px', fontWeight: '700', margin: 0 }}>🍽️ {selectedCategory.name}</h3>
                        <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: 0 }}>#{selectedCategory.code}</p>
                      </div>
                    </div>
                    <button onClick={() => handleOpenFoodModal()} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Plus size={16} /> Thêm Món
                    </button>
                  </div>

                  {(() => {
                    const catFoods = foods.filter(f => f.categoryId === selectedCategory.id);
                    return catFoods.length === 0 ? (
                      <div className="card" style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                        <p style={{ fontSize: '48px', margin: '0 0 16px' }}>🍽️</p>
                        <p style={{ fontWeight: '600' }}>Danh mục này chưa có món ăn nào.</p>
                        <button onClick={() => handleOpenFoodModal()} className="btn btn-primary" style={{ marginTop: '16px' }}>+ Thêm món đầu tiên</button>
                      </div>
                    ) : (
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px' }}>
                        {catFoods.map(food => (
                          <div key={food.id} className="card" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                            <div style={{ height: '150px', backgroundColor: 'var(--bg-app)', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
                              {food.imageUrl ? (
                                <img src={food.imageUrl} alt={food.foodName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={e => e.target.style.display = 'none'} />
                              ) : (
                                <Utensils size={40} color="var(--border-color)" />
                              )}
                            </div>
                            <div style={{ padding: '16px', flex: 1, display: 'flex', flexDirection: 'column', gap: '6px' }}>
                              <h4 style={{ fontSize: '15px', fontWeight: '700', color: 'var(--text-primary)', margin: 0 }}>{food.foodName}</h4>
                              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: 0 }}>#{food.code}</p>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto', paddingTop: '8px' }}>
                                <p style={{ fontSize: '17px', fontWeight: '700', color: 'var(--status-ordering)', margin: 0 }}>
                                  {Number(food.price).toLocaleString('vi-VN')}<span style={{ fontSize: '12px', fontWeight: '400', color: 'var(--text-secondary)' }}> đ</span>
                                </p>
                                <div style={{ display: 'flex', gap: '8px' }}>
                                  <button onClick={() => handleOpenFoodModal(food)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}><Settings size={15} /></button>
                                  <button onClick={() => handleDeleteFood(food.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)' }}><LogOut size={15} /></button>
                                </div>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    );
                  })()}
                </>
              )}

              {/* Modal Thêm/Sửa Món ăn */}
              {isFoodModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="card" style={{ width: '100%', maxWidth: '480px', padding: '32px', maxHeight: '90vh', overflowY: 'auto' }}>
                    <h2 style={{ fontSize: '22px', fontWeight: '700', marginBottom: '24px' }}>{editingFood ? '✏️ Sửa Món Ăn' : '➕ Thêm Món Ăn'}</h2>
                    <form onSubmit={handleSaveFood} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                      <div className="input-group">
                        <label>Mã món (code) *</label>
                        <input type="text" required value={foodFormData.code} onChange={e => setFoodFormData({ ...foodFormData, code: e.target.value })} className="input-field" placeholder="VD: COM001" />
                      </div>
                      <div className="input-group">
                        <label>Tên món ăn *</label>
                        <input type="text" required value={foodFormData.foodName} onChange={e => setFoodFormData({ ...foodFormData, foodName: e.target.value })} className="input-field" placeholder="VD: Cơm chiên dương châu" />
                      </div>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                        <div className="input-group">
                          <label>Giá (đ) *</label>
                          <input type="number" required min="1000" value={foodFormData.price} onChange={e => setFoodFormData({ ...foodFormData, price: e.target.value })} className="input-field" placeholder="45000" />
                        </div>
                        <div className="input-group">
                          <label>Danh mục *</label>
                          <select required value={foodFormData.categoryId} onChange={e => setFoodFormData({ ...foodFormData, categoryId: e.target.value })} className="input-field">
                            <option value="">-- Chọn danh mục --</option>
                            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                          </select>
                        </div>
                      </div>
                      <div className="input-group">
                        <label>URL Hình ảnh (tuỳ chọn)</label>
                        <input type="url" value={foodFormData.imageUrl || ''} onChange={e => setFoodFormData({ ...foodFormData, imageUrl: e.target.value })} className="input-field" placeholder="https://example.com/image.jpg" />
                      </div>
                      <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                        <button type="button" onClick={() => setIsFoodModalOpen(false)} className="btn btn-outline" style={{ flex: 1 }}>Hủy</button>
                        <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Lưu Món Ăn</button>
                      </div>
                    </form>
                  </motion.div>
                </div>
              )}

              {/* Modal Thêm/Sửa Danh mục */}
              {isCategoryModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="card" style={{ width: '100%', maxWidth: '440px', padding: '32px' }}>
                    <h2 style={{ fontSize: '22px', fontWeight: '700', marginBottom: '24px' }}>{editingCategory ? '✏️ Sửa Danh Mục' : '➕ Thêm Danh Mục'}</h2>
                    <form onSubmit={handleSaveCategory} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                      <div className="input-group">
                        <label>Mã danh mục (code) *</label>
                        <input type="text" required value={categoryFormData.code} onChange={e => setCategoryFormData({ ...categoryFormData, code: e.target.value })} className="input-field" placeholder="VD: MAIN" />
                      </div>
                      <div className="input-group">
                        <label>Tên danh mục *</label>
                        <input type="text" required value={categoryFormData.name} onChange={e => setCategoryFormData({ ...categoryFormData, name: e.target.value })} className="input-field" placeholder="VD: Món chính" />
                      </div>
                      <div className="input-group">
                        <label>Mô tả (tuỳ chọn)</label>
                        <input type="text" value={categoryFormData.description || ''} onChange={e => setCategoryFormData({ ...categoryFormData, description: e.target.value })} className="input-field" placeholder="Mô tả ngắn về danh mục..." />
                      </div>
                      <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                        <button type="button" onClick={() => setIsCategoryModalOpen(false)} className="btn btn-outline" style={{ flex: 1 }}>Hủy</button>
                        <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Lưu Danh Mục</button>
                      </div>
                    </form>
                  </motion.div>
                </div>
              )}

            </motion.div>
          )}
          {/* STAFF TAB */}
          {activeTab === 'Staff' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card" style={{ padding: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h3 style={{ fontSize: '20px', fontWeight: '600' }}>Staff Directory</h3>
                <button onClick={() => handleOpenStaffModal()} className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '14px' }}>
                  <Users size={16} /> Add Staff
                </button>
              </div>

              {loadingConfig.staff ? (
                <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Loading staff data...</p>
              ) : (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-app)' }}>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>ID</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Full Name</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Birthday</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Role</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Contact</th>
                        <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Server</th>
                      </tr>
                    </thead>
                    <tbody>
                      {staff.map((person) => (
                        <tr key={person.uid} style={{ borderBottom: '1px solid var(--border-color)' }}>
                          <td style={{ padding: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>{person.uid}</td>
                          <td style={{ padding: '16px', fontWeight: '700', color: '#11117F' }}>{person.fullName || 'N/A'}</td>
                          <td style={{ padding: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                              <Calendar size={14} style={{ opacity: 0.6 }} />
                              {formatDate(person.birthday)}
                            </div>
                          </td>
                          <td style={{ padding: '16px' }}>
                            <span style={{ padding: '6px 12px', borderRadius: '8px', fontSize: '12px', backgroundColor: '#F0F0FF', border: '1px solid #11117F', fontWeight: '700', color: '#11117F' }}>{person.role}</span>
                          </td>
                          <td style={{ padding: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>
                            <div style={{ fontWeight: '500', color: 'var(--text-primary)' }}>{person.phoneNumber || '-'}</div>
                            <div style={{ opacity: 0.8 }}>{person.email || '-'}</div>
                          </td>
                          <td style={{ padding: '16px', color: 'var(--text-secondary)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }}>
                              <span style={{ fontSize: '13px' }}>{person.server}</span>
                              <div style={{ display: 'flex', gap: '8px' }}>
                                <button onClick={() => handleOpenStaffModal(person)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--primary)', padding: '4px' }} title="Edit Staff"><Settings size={16} /></button>
                                <button onClick={() => handleDeleteStaff(person.server, person.uid)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)', padding: '4px' }} title="Remove Staff"><LogOut size={16} /></button>
                              </div>
                            </div>
                          </td>
                        </tr>
                      ))}
                      {staff.length === 0 && (
                        <tr><td colSpan="5" style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)' }}>No staff found.</td></tr>
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </motion.div>
          )}

          {/* Development Placeholders for others */}
          {['Reports', 'Settings'].includes(activeTab) && (
            <div style={{ display: 'flex', alignItems: 'center', justifyItems: 'center', height: '400px', width: '100%' }}>
              <div style={{ margin: 'auto', textAlign: 'center', color: 'var(--text-secondary)' }}>
                <Settings size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
                <h2>{activeTab} module under construction</h2>
                <p>This feature will be available in the next release.</p>
              </div>
            </div>
          )}

        </div>
      </main>

      {/* Staff Modal overlay - Outside main card to avoid hover interference */}
      {isStaffModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <motion.div
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            style={{
              width: '100%',
              maxWidth: '650px',
              padding: '40px',
              maxHeight: '90vh',
              overflowY: 'auto',
              backgroundColor: 'var(--bg-surface)',
              borderRadius: 'var(--radius-lg)',
              boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
              display: 'flex',
              flexDirection: 'column',
              gap: '16px'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
              <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#11117F', letterSpacing: '-0.5px' }}>
                {editingStaff ? 'Update Staff Member' : 'Register New Staff'}
              </h2>
              <div style={{ width: '44px', height: '44px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#F0F0FF', borderRadius: '12px', color: '#11117F' }}>
                <Users size={24} />
              </div>
            </div>

            <form onSubmit={handleSaveStaff} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Username</label>
                  <input type="text" required value={staffFormData.username} onChange={e => setStaffFormData({ ...staffFormData, username: e.target.value })} className="form-input" disabled={!!editingStaff} style={{ opacity: editingStaff ? 0.7 : 1, borderColor: '#E0E0E0' }} />
                </div>
                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Password</label>
                  <input type="password" required={!editingStaff} value={staffFormData.password} onChange={e => setStaffFormData({ ...staffFormData, password: e.target.value })} className="form-input" placeholder={editingStaff ? '••••••••' : 'Enter password'} style={{ borderColor: '#E0E0E0' }} />
                  {editingStaff && <small style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '4px', display: 'block' }}>Leave blank to keep current password</small>}
                </div>

                <div className="input-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Full Name</label>
                  <input type="text" required value={staffFormData.fullName} onChange={e => setStaffFormData({ ...staffFormData, fullName: e.target.value })} className="form-input" placeholder="e.g. John Doe" style={{ borderColor: '#E0E0E0' }} />
                </div>

                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Email Address</label>
                  <input type="email" required value={staffFormData.email} onChange={e => setStaffFormData({ ...staffFormData, email: e.target.value })} className="form-input" placeholder="john@example.com" style={{ borderColor: '#E0E0E0' }} />
                </div>
                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Phone Number</label>
                  <input type="text" required value={staffFormData.phoneNumber} onChange={e => setStaffFormData({ ...staffFormData, phoneNumber: e.target.value })} className="form-input" placeholder="090 123 4567" style={{ borderColor: '#E0E0E0' }} />
                </div>

                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Birthday</label>
                  <div style={{ position: 'relative' }}>
                    <Calendar size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: '#11117F', opacity: 0.5, pointerEvents: 'none' }} />
                    <input
                      type="date"
                      value={staffFormData.birthday}
                      onChange={e => setStaffFormData({ ...staffFormData, birthday: e.target.value })}
                      className="form-input"
                      style={{ paddingLeft: '44px', borderColor: '#E0E0E0' }}
                    />
                  </div>
                </div>

                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Gender</label>
                  <div style={{ display: 'flex', gap: '12px' }}>
                    {['MALE', 'FEMALE', 'OTHER'].map(g => (
                      <label key={g} style={{
                        flex: 1, padding: '10px', borderRadius: '8px', border: staffFormData.gender === g ? '2px solid #11117F' : '1px solid #E0E0E0',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                        fontSize: '13px', color: staffFormData.gender === g ? '#11117F' : '#666', fontWeight: staffFormData.gender === g ? '700' : '500'
                      }}>
                        <input type="radio" name="gender" value={g} checked={staffFormData.gender === g} onChange={() => setStaffFormData({ ...staffFormData, gender: g })} style={{ display: 'none' }} />
                        {g === 'MALE' ? 'Male' : g === 'FEMALE' ? 'Female' : 'Other'}
                      </label>
                    ))}
                  </div>
                </div>

                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Assigned Server</label>
                  <div style={{ position: 'relative' }}>
                    <select value={staffFormData.server} onChange={e => setStaffFormData({ ...staffFormData, server: e.target.value })} className="form-input" style={{ appearance: 'none', paddingRight: '40px', borderColor: '#E0E0E0' }}>
                      <option value="HCM">Ho Chi Minh (HCM)</option>
                      <option value="HN">Ha Noi (HN)</option>
                      <option value="DN">Da Nang (DN)</option>
                    </select>
                    <div style={{ position: 'absolute', right: '14px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none', color: '#11117F', opacity: 0.5 }}>
                      <Filter size={16} />
                    </div>
                  </div>
                </div>

                <div className="input-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Access Role</label>
                  <div style={{ display: 'flex', gap: '16px' }}>
                    {['WAITER', 'KITCHEN', 'ADMIN'].map(role => (
                      <label key={role} style={{
                        flex: 1, padding: '14px', borderRadius: '12px', border: staffFormData.role === role ? '2px solid #11117F' : '1px solid #E0E0E0',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', cursor: 'pointer',
                        transition: 'all 0.2s ease',
                        color: staffFormData.role === role ? '#11117F' : '#666',
                        fontWeight: staffFormData.role === role ? '800' : '500',
                        backgroundColor: 'white'
                      }}>
                        <input
                          type="radio" name="role" value={role}
                          checked={staffFormData.role === role}
                          onChange={() => setStaffFormData({ ...staffFormData, role })}
                          style={{ display: 'none' }}
                        />
                        {role === 'WAITER' ? 'WAITRESS/WAITER' : role === 'KITCHEN' ? 'KITCHEN' : 'ADMINISTRATOR'}
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '20px', marginTop: '32px' }}>
                <button type="button" onClick={() => setIsStaffModalOpen(false)} className="btn" style={{ flex: 1, padding: '16px', borderRadius: '12px', border: '2px solid #11117F', color: '#11117F', fontWeight: '800', background: 'white', cursor: 'pointer' }}>Discard Changes</button>
                <button type="submit" className="btn" style={{ flex: 1, padding: '16px', borderRadius: '12px', border: 'none', background: '#11117F', color: 'white', fontWeight: '800', cursor: 'pointer' }}>
                  {editingStaff ? 'Update Information' : 'Confirm Registration'}
                </button>
              </div>
            </form>
          </motion.div>
        </div>
      )}
    </div>
  );
};

// ---------------------------------------------------------
// Main App Component
// ---------------------------------------------------------
const App = () => {
  const [currentUser, setCurrentUser] = useState(null);

  // Auto-login if valid token in sessionStorage
  useEffect(() => {
    const token = sessionStorage.getItem('token');
    if (token) {
      const payload = parseJwt(token);
      if (payload) {
        setCurrentUser({ id: payload.uid, role: payload.role, server: payload.server });
      }
    }
  }, []);


  return (
    <AnimatePresence mode="wait">
      {currentUser ? (
        <DashboardScreen key="dashboard" user={currentUser} onLogout={() => setCurrentUser(null)} />
      ) : (
        <LoginScreen key="login" onLoginSuccess={(user) => setCurrentUser(user)} />
      )}
    </AnimatePresence>
  );
};

export default App;
