import React, { useState, useEffect, useCallback, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  LayoutDashboard, Users, Utensils, ClipboardList, Settings, LogOut, Menu, X, Plus, 
  User as UserIcon, Calendar, BarChart as BarChartIcon, TrendingUp, PieChart as PieChartIcon, 
  Search, Download, Trash2, Edit, Bell, Filter, CheckCircle, XCircle, Info, RefreshCw, AlertCircle, QrCode 
} from 'lucide-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, 
  LineChart, Line, PieChart, Cell, Pie 
} from 'recharts';
import { apiService } from './services/api';
import './index.css';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

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
  const [loginStep, setLoginStep] = useState(0); // 0: Credentials, 1: OTP
  const [loginOtp, setLoginOtp] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      // Gọi đúng signature mới: (username, password)
      const response = await apiService.auth.login(empId, password);
      // BE trả về: { code, status, message, token }
      
      if (response.status === 'REQUIRE_OTP') {
        setLoginStep(1);
        setSuccessMessage(response.message);
        return;
      }

      const token = response.token;
      if (!token) throw new Error('Không nhận được token từ server');
      
      handleSuccessfulLogin(token);
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyLoginOTP = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await apiService.auth.verifyOtp(empId, loginOtp);
      // response: { code, status, message, token }
      if (response.token) {
        handleSuccessfulLogin(response.token);
      } else {
        throw new Error('Xác thực OTP thất bại');
      }
    } catch (err) {
      setError(err.message || 'Xác thực OTP thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleSuccessfulLogin = (token) => {
    sessionStorage.setItem('token', token);
    const userPayload = parseJwt(token);
    const userData = {
      fullName: userPayload.fullName || userPayload.sub,
      role: userPayload.role, // Lấy trực tiếp từ Token
      server: 'local'
    };
    
    sessionStorage.setItem('user', JSON.stringify(userData));
    onLoginSuccess(userData);
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
            loginStep === 0 ? (
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
            ) : (
              // --- LOGIN OTP STEP ---
              <form onSubmit={handleVerifyLoginOTP}>
                <div className="form-group">
                  <label className="form-label">Nhập mã OTP (từ Email)</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    placeholder="______"
                    maxLength={6}
                    value={loginOtp} 
                    onChange={e => setLoginOtp(e.target.value)} 
                    required 
                    style={{ textAlign: 'center', fontSize: '24px', letterSpacing: '8px', fontWeight: '800' }}
                  />
                </div>
                <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '16px', marginBottom: '16px' }} disabled={loading}>
                  {loading ? 'Verifying...' : 'Xác nhận OTP'}
                </button>
                <button type="button" onClick={() => setLoginStep(0)} style={{ width: '100%', background: 'none', border: 'none', cursor: 'pointer', fontSize: '14px', color: 'var(--text-secondary)' }}>
                  Quay lại đăng nhập
                </button>
              </form>
            )
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
  const [activeTab, setActiveTab] = useState(() => {
    return sessionStorage.getItem('activeTab') || 'Overview';
  });

  const handleLogout = async () => {
    const ok = await confirm(
      'Đăng xuất', 
      'Bạn có chắc chắn muốn đăng xuất khỏi hệ thống không?', 
      { danger: true, confirmLabel: 'Đăng xuất' }
    );
    if (ok) {
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('user');
      sessionStorage.removeItem('activeTab');
      onLogout();
    }
  };

  // Validate active tab on role change or mount
  useEffect(() => {
    const isTabAllowed = filteredNavItems.some(item => item.label === activeTab);
    if (!isTabAllowed && filteredNavItems.length > 0) {
      setActiveTab(filteredNavItems[0].label);
    }
  }, [user?.role, filteredNavItems, activeTab]);
  
  useEffect(() => {
    sessionStorage.setItem('activeTab', activeTab);
  }, [activeTab]);
  
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
    username: '', password: '', fullName: '', email: '', phoneNumber: '', birthday: '', role: 'WAITER', server: 'HCM', gender: 'MALE', citizenPid: ''
  });

  const [isTableModalOpen, setIsTableModalOpen] = useState(false);
  const [editingTable, setEditingTable] = useState(null);
  const [tableFormData, setTableFormData] = useState({
    tableNumber: '', capacity: 4, location: ''
  });

  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState(null);

  // Create Order States
  const [isCreateOrderModalOpen, setIsCreateOrderModalOpen] = useState(false);
  const [cartItems, setCartItems] = useState([]); // { menuItemId, foodName, unitPrice, quantity, note }
  const [selectedTableId, setSelectedTableId] = useState('');
  const [orderNote, setOrderNote] = useState('');
  const [selectedTableForOrders, setSelectedTableForOrders] = useState(null);

  // Checkout (Payment) States
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState(false);
  const [checkoutOrder, setCheckoutOrder] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('CASH'); // CASH or TRANSFER
  const [customerCash, setCustomerCash] = useState('');
  const [isQRModalOpen, setIsQRModalOpen] = useState(false);

  const [loadingConfig, setLoadingConfig] = useState({
    overview: true, orders: false, foods: false, staff: false, tables: false
  });

  // WebSocket Connection for Real-time Tables
  useEffect(() => {
    const socket = new SockJS('http://localhost:8083/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('>>> [WS Debug Table]:', str),
      onConnect: () => {
        console.log('>>> WebSocket Connected to table-service');
        stompClient.subscribe('/topic/tables', (message) => {
          if (message.body === 'REFRESH_TABLES') {
            fetchTablesData();
            if (activeTab === 'Overview') fetchOverviewData();
          }
        });
      },
    });
    stompClient.activate();
    return () => { if (stompClient.active) stompClient.deactivate(); };
  }, [activeTab]);

  // WebSocket Connection for General Notifications
  useEffect(() => {
    const socket = new SockJS('http://localhost:8086/ws-notifications');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('>>> [WS Debug Note]:', str),
      onConnect: () => {
        console.log('>>> WebSocket Connected to notification-service');
        stompClient.subscribe('/topic/public', (message) => {
          try {
            const note = JSON.parse(message.body);
            // Hiển thị thông báo bằng hệ thống toast sẵn có
            toast[note.type || 'info'](note.message || note.title);
            
            // Nếu là thông báo đơn hàng mới hoặc thanh toán, có thể refresh data
            if (note.title?.includes('Đơn hàng') || note.title?.includes('Thanh toán')) {
              if (activeTab === 'Orders') fetchOrdersData();
              if (activeTab === 'Overview') fetchOverviewData();
            }
          } catch (e) { console.error('Error parsing notification:', e); }
        });
      },
    });
    stompClient.activate();
    return () => { if (stompClient.active) stompClient.deactivate(); };
  }, [activeTab]);

  const navItems = [
    { icon: <LayoutDashboard size={20} />, label: 'Overview', roles: ['ADMIN'] },
    { icon: <ClipboardList size={20} />, label: 'Orders', roles: ['ADMIN'] },
    { icon: <LayoutDashboard size={20} />, label: 'Tables', roles: ['ADMIN', 'WAITER'] },
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
    else if (activeTab === 'Tables') fetchTablesData();
    else if (activeTab === 'Menu & Food') fetchFoodsData();
    else if (activeTab === 'Staff') fetchStaffData();
    else if (activeTab === 'Kitchen') fetchKitchenData();
    else if (activeTab === 'Reports') fetchReportData();
  }, [activeTab]);

  const [reportData, setReportData] = useState([]);
  const [reportType, setReportType] = useState('DATE');

  const fetchReportData = async (type = reportType) => {
    setLoadingConfig(prev => ({ ...prev, reports: true }));
    try {
      const res = await apiService.order.getReports(type);
      setReportData(res.data || []);
      setReportType(type);
    } catch (error) {
      toast.error('Lỗi tải báo cáo: ' + error.message);
    } finally {
      setLoadingConfig(prev => ({ ...prev, reports: false }));
    }
  };

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
      const flatItems = orders.flatMap(order => {
        const table = tables.find(t => String(t.id) === String(order.tableId));
        const tableName = table ? `Bàn ${table.tableNumber}` : (order.tableNumber || order.tableId || '—');
        
        return (order.items || []).map(item => ({
          ...item,
          tableNumber: tableName,
          orderId: order.id,
          orderStatus: order.status,
          createdAt: order.createdAt,
        }));
      }).filter(item => item.kitchenStatus !== 'SERVED');

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
      const [ordersRes, tablesRes, foodsRes] = await Promise.allSettled([
        apiService.order.getAll(),
        apiService.dashboard.getTables(),
        apiService.catalog.getItems()
      ]);

      console.log('Orders Response:', ordersRes);
      console.log('Tables Response:', tablesRes);
      console.log('Foods Response:', foodsRes);

      if (ordersRes.status === 'fulfilled' && ordersRes.value.data) {
        const ordersArr = Array.isArray(ordersRes.value.data) ? ordersRes.value.data : (ordersRes.value.data.content || []);
        
        // Làm giàu dữ liệu tên bàn
        const enrichedOrders = ordersArr.map(order => {
          const table = (tablesRes.status === 'fulfilled' && tablesRes.value.data) 
            ? (Array.isArray(tablesRes.value.data) ? tablesRes.value.data : tablesRes.value.data.content).find(t => String(t.id) === String(order.tableId))
            : null;
          return {
            ...order,
            tableNumber: table ? `Bàn ${table.tableNumber}` : (order.tableNumber || order.tableId)
          };
        });

        setRecentOrders(enrichedOrders.slice(0, 5));
        setAllOrders(enrichedOrders);
      }
      if (tablesRes.status === 'fulfilled' && tablesRes.value.data) {
        const tablesArr = Array.isArray(tablesRes.value.data) ? tablesRes.value.data : (tablesRes.value.data.content || []);
        setTables(tablesArr);
      }
      if (foodsRes.status === 'fulfilled' && foodsRes.value.data) {
        const foodsArr = Array.isArray(foodsRes.value.data) ? foodsRes.value.data : (foodsRes.value.data.content || []);
        setFoods(foodsArr);
      }
    } catch (error) {
      console.error('Lỗi khi tải dữ liệu tổng quan:', error);
    } finally {
      setLoadingConfig(prev => ({ ...prev, overview: false }));
    }
  };



  const fetchOrdersData = async (statusFilter) => {
    // Đảm bảo statusFilter là chuỗi, nếu là Object (Event) thì coi như null
    const finalStatus = (statusFilter && typeof statusFilter === 'string') ? statusFilter : null;
    
    setLoadingConfig(prev => ({ ...prev, orders: true }));
    try {
      const res = await apiService.order.getAll(finalStatus);
      if (res.data) {
        const enrichedOrders = res.data.map(order => {
          const table = tables.find(t => String(t.id) === String(order.tableId));
          return {
            ...order,
            tableNumber: table ? `Bàn ${table.tableNumber}` : (order.tableNumber || order.tableId)
          };
        });
        setAllOrders(enrichedOrders);
      }
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

  // ---- Logic Gọi Món (Create Order) ----
  useEffect(() => {
    if (isCreateOrderModalOpen) {
      // Đảm bảo luôn có dữ liệu bàn và món ăn mới nhất khi mở Modal
      fetchTablesData();
      fetchFoodsData();
    }
  }, [isCreateOrderModalOpen]);

  const handleAddToCart = (food) => {
    setCartItems(prev => {
      const existing = prev.find(item => item.menuItemId === food.id);
      if (existing) {
        return prev.map(item => item.menuItemId === food.id ? { ...item, quantity: item.quantity + 1 } : item);
      }
      return [...prev, { menuItemId: food.id, foodName: food.foodName, unitPrice: food.price, quantity: 1, note: '' }];
    });
  };

  const handleUpdateCartItem = (menuItemId, delta, note = undefined) => {
    setCartItems(prev => prev.map(item => {
      if (item.menuItemId !== menuItemId) return item;
      const updated = { ...item };
      if (delta !== 0) updated.quantity = Math.max(1, updated.quantity + delta);
      if (note !== undefined) updated.note = note;
      return updated;
    }));
  };

  const handleRemoveFromCart = (menuItemId) => {
    setCartItems(prev => prev.filter(item => item.menuItemId !== menuItemId));
  };

  const handleSubmitOrder = async () => {
    if (!selectedTableId) {
      toast.error('Vui lòng chọn bàn!');
      return;
    }
    if (cartItems.length === 0) {
      toast.error('Giỏ hàng trống, vui lòng chọn món!');
      return;
    }

    try {
      const selectedTable = tables.find(t => String(t.id) === String(selectedTableId));
      const payload = {
        tableId: selectedTable.id,
        tableNumber: selectedTable.tableNumber || selectedTable.id,
        note: orderNote,
        items: cartItems
      };

      await apiService.order.create(payload);
      toast.success('🎉 Đã tạo đơn hàng thành công!');
      
      // Cleanup & Refresh
      setIsCreateOrderModalOpen(false);
      setCartItems([]);
      setSelectedTableId('');
      setOrderNote('');
      fetchOrdersData();
    } catch (err) {
      toast.error('Lỗi khi tạo đơn hàng: ' + err.message);
    }
  };

  // ---- Logic Thanh Toán (Checkout) ----
  const handleOpenCheckout = (order) => {
    setCheckoutOrder(order);
    setPaymentMethod('CASH');
    setCustomerCash('');
    setIsCheckoutModalOpen(true);
  };

  const handleConfirmPayment = async () => {
    if (paymentMethod === 'CASH' && checkoutOrder) {
      const total = Number(checkoutOrder.totalAmount || checkoutOrder.totalPrice || 0);
      const cash = Number(customerCash.replace(/\D/g, '') || 0);
      if (cash < total) {
        toast.error('Tiền khách đưa không đủ để thanh toán!');
        return;
      }
    }

    try {
      // 1. Lưu lịch sử thanh toán vào payment-service (Port 8085)
      await apiService.payment.create({
        orderId: checkoutOrder.id,
        amount: checkoutOrder.totalAmount || checkoutOrder.totalPrice || 0,
        method: paymentMethod,
        note: paymentMethod === 'CASH' ? `Khách đưa: ${customerCash}đ` : 'Thanh toán chuyển khoản VietQR'
      });

      // 2. Cập nhật trạng thái đơn hàng sang COMPLETED (Port 8082)
      await handleUpdateOrderStatus(checkoutOrder.id, 'COMPLETED');
      
      toast.success('🎉 Thanh toán thành công và đã đóng đơn hàng!');
      setIsCheckoutModalOpen(false);
      setCheckoutOrder(null);
      setCustomerCash('');
    } catch (error) {
      toast.error('❌ Lỗi thanh toán: ' + error.message);
      console.error('Payment Error:', error);
    }
  };
  // ------------------------------------

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
      const res = await apiService.dashboard.getStaff();
      if (res.data) setStaff(res.data);
    } catch (error) {
      toast.error('Không thể tải danh sách nhân viên: ' + error.message);
    } finally {
      setLoadingConfig(prev => ({ ...prev, staff: false }));
    }
  };

  const fetchTablesData = async () => {
    setLoadingConfig(prev => ({ ...prev, tables: true }));
    try {
      const res = await apiService.dashboard.getTables();
      if (res.data) setTables(res.data);
    } catch (error) {
      toast.error('Không thể tải danh sách bàn: ' + error.message);
    } finally {
      setLoadingConfig(prev => ({ ...prev, tables: false }));
    }
  };

  const handleOpenTableModal = (table = null) => {
    if (table) {
      setEditingTable(table);
      setTableFormData({
        tableNumber: table.tableNumber || '',
        capacity: table.capacity || 4,
        location: table.location || ''
      });
    } else {
      setEditingTable(null);
      setTableFormData({ tableNumber: '', capacity: 4, location: '' });
    }
    setIsTableModalOpen(true);
  };

  const handleSaveTable = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...tableFormData,
        tableNumber: parseInt(tableFormData.tableNumber),
        capacity: parseInt(tableFormData.capacity)
      };

      if (editingTable) {
        await apiService.dashboard.updateTable(editingTable.id, payload);
        toast.success('Cập nhật bàn thành công');
      } else {
        await apiService.dashboard.createTable(payload);
        toast.success('Thêm bàn mới thành công');
      }
      setIsTableModalOpen(false);
      fetchTablesData();
    } catch (err) {
      toast.error('Lỗi lưu thông tin bàn: ' + err.message);
    }
  };

  const handleDeleteTable = async (id) => {
    const ok = await confirm('Xóa bàn', 'Bạn có chắc chắn muốn xóa bàn này không?', { danger: true, confirmLabel: 'Xóa bàn' });
    if (!ok) return;
    try {
      await apiService.dashboard.deleteTable(id);
      toast.success('Đã xóa bàn');
      fetchTablesData();
    } catch (err) {
      toast.error('Lỗi xóa bàn: ' + err.message);
    }
  };

  const statCards = [
    { 
      title: 'Total Revenue', 
      value: new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(
        allOrders.filter(o => o.status === 'COMPLETED').reduce((acc, o) => acc + (o.totalPrice || 0), 0)
      ), 
      change: '+0%', 
      positive: true 
    },
    { title: 'Total Orders', value: allOrders.length || 0, change: 'All time', positive: true },
    { title: 'Total Menu Items', value: foods.length || 0, change: 'Active', positive: true },
    { 
      title: 'Total Tables', 
      value: tables.length || 0, 
      change: `${tables.filter(t => String(t.status).toUpperCase() === 'OCCUPIED' || String(t.status) === '1').length} Occupied`, 
      positive: true 
    },
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
        username: person.username || '',
        password: '',
        fullName: person.fullName || '',
        email: person.email || '',
        phoneNumber: person.phoneNumber || '',
        birthday: person.birthday || '',
        role: person.role || 'WAITER',
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
      const roleStrToInt = { WAITER: 0, ADMIN: 1, CHEF: 2, KITCHEN: 3 };
      const payload = { ...staffFormData };
      
      // Đảm bảo birthday khớp định dạng LocalDateTime (yyyy-MM-ddTHH:mm:ss)
      if (payload.birthday && payload.birthday.length === 10) {
        payload.birthday = `${payload.birthday}T00:00:00`;
      }

      if (typeof payload.role === 'string') payload.role = roleStrToInt[payload.role] ?? 0;
      if (editingStaff && !payload.password) delete payload.password;
      if (editingStaff) {
        await apiService.dashboard.updateStaff(null, editingStaff.id, payload);
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

  const handleDeleteStaff = async (id) => {
    const ok = await confirm('Xóa nhân viên', 'Bạn có chắc muốn xóa nhân viên này không?', { danger: true, confirmLabel: 'Xóa' });
    if (!ok) return;
    try {
      await apiService.dashboard.deleteStaff(null, id);
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
          <img src="/food_order_logo_1778061110379.png" alt="Logo" style={{ width: '40px', height: '40px', borderRadius: '10px', objectFit: 'cover' }} />
          <h2 style={{ fontSize: '22px', fontWeight: '900', color: '#11117F', letterSpacing: '-0.5px' }}>Food<span style={{ color: 'var(--primary)' }}>Order</span></h2>
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
            <button onClick={handleLogout} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)' }} title="Logout">
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
                          <th style={{ padding: '12px', color: 'var(--text-secondary)', fontWeight: '500', fontSize: '14px' }}>Mã đơn</th>
                          <th style={{ padding: '12px', color: 'var(--text-secondary)', fontWeight: '500', fontSize: '14px' }}>Table</th>
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
                              <td style={{ padding: '16px 12px', fontWeight: '700', color: 'var(--primary)', fontFamily: 'monospace' }}>ORD-{String(order.id).slice(0, 8).toUpperCase()}</td>
                              <td style={{ padding: '16px 12px', color: 'var(--text-secondary)', fontWeight: '600' }}>{order.tableNumber || order.tableId}</td>
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
                      const status = String(table.status).toUpperCase();
                      const isAvailable = status === 'AVAILABLE' || status === '0';
                      const isOccupied = status === 'OCCUPIED' || status === '1';
                      const isCleaning = status === 'CLEANING' || status === '3';

                      return (
                        <div key={table.id} style={{ 
                          padding: '16px', 
                          borderRadius: 'var(--radius-md)', 
                          textAlign: 'center', 
                          border: `1px solid ${isOccupied ? 'var(--secondary)' : 'var(--border-color)'}`, 
                          backgroundColor: isOccupied ? 'rgba(9, 52, 219, 0.05)' : 'var(--bg-app)',
                          transition: 'all 0.3s ease'
                        }}>
                          <span style={{ display: 'block', fontSize: '15px', fontWeight: '700', marginBottom: '6px', color: '#11117F' }}>
                            Bàn {table.tableNumber}
                          </span>
                          <span className={`badge ${isOccupied ? 'badge-occupied' : 'badge-empty'}`} style={{ 
                            padding: '4px 12px',
                            borderRadius: '12px',
                            fontSize: '12px',
                            fontWeight: '700',
                            backgroundColor: isAvailable ? '#ecfdf5' : isOccupied ? '#fef2f2' : '#fffbeb',
                            color: isAvailable ? '#10b981' : isOccupied ? '#ef4444' : '#f59e0b'
                          }}>
                            {isAvailable ? 'Sẵn sàng' : isOccupied ? 'Có khách' : isCleaning ? 'Đang dọn' : status}
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

          {/* ORDERS TAB - New Real-time Table Grid View */}
          {activeTab === 'Orders' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
              
              {/* Header section */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3 style={{ fontSize: '24px', fontWeight: '800', color: '#11117F', margin: 0 }}>🏪 Sơ đồ bàn & Đơn hàng</h3>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                    {tables.filter(t => t.status === 'AVAILABLE' || t.status === '0').length} bàn trống &middot; {tables.filter(t => t.status === 'OCCUPIED' || t.status === '1').length} bàn đang sử dụng
                  </p>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button onClick={() => { fetchTablesData(); fetchOrdersData(); }} className="btn btn-outline" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <RefreshCw size={16} /> Làm mới
                  </button>
                  <button onClick={() => setIsCreateOrderModalOpen(true)} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: '#10b981', borderColor: '#10b981' }}>
                    <Plus size={16} /> Mở bàn / Gọi món
                  </button>
                </div>
              </div>

              {/* Main Content Layout: Grid and Detail */}
              <div style={{ display: 'grid', gridTemplateColumns: selectedTableForOrders ? '1fr 380px' : '1fr', gap: '24px', transition: 'all 0.4s ease' }}>
                
                {/* Left: Table Grid */}
                <div className="card" style={{ padding: '24px', backgroundColor: '#F8FAFC' }}>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: '16px' }}>
                    {loadingConfig.tables ? (
                      <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: '40px' }}>Loading...</div>
                    ) : (
                      tables.map((table) => {
                        const status = String(table.status).toUpperCase();
                        const isOccupied = status === 'OCCUPIED' || status === '1';
                        const isSelected = selectedTableForOrders?.id === table.id;
                        
                        // Find current active orders for this table
                        const tableOrders = allOrders.filter(o => String(o.tableId) === String(table.id) && (o.status === 'PENDING' || o.status === 'CONFIRMED'));
                        const totalAmount = tableOrders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);

                        return (
                          <motion.div 
                            key={table.id}
                            whileHover={{ scale: 1.03, y: -2 }}
                            whileTap={{ scale: 0.97 }}
                            onClick={() => setSelectedTableForOrders(isSelected ? null : table)}
                            style={{ 
                              aspectRatio: '1/1',
                              cursor: 'pointer',
                              borderRadius: '20px',
                              display: 'flex',
                              flexDirection: 'column',
                              alignItems: 'center',
                              justifyContent: 'center',
                              position: 'relative',
                              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                              backgroundColor: isSelected ? '#11117F' : '#FFF',
                              boxShadow: isSelected ? '0 10px 25px rgba(17, 17, 127, 0.3)' : '0 4px 12px rgba(0,0,0,0.05)',
                              border: `2px solid ${isOccupied ? '#EF4444' : isSelected ? '#11117F' : 'transparent'}`
                            }}
                          >
                            <div style={{ 
                              width: '48px', height: '48px', borderRadius: '14px', 
                              backgroundColor: isSelected ? 'rgba(255,255,255,0.2)' : isOccupied ? '#FEF2F2' : '#F0F9FF',
                              display: 'flex', alignItems: 'center', justifyContent: 'center',
                              marginBottom: '12px', color: isSelected ? '#FFF' : isOccupied ? '#EF4444' : '#11117F'
                            }}>
                              {isOccupied ? <Users size={24} /> : <Utensils size={24} />}
                            </div>
                            <span style={{ fontSize: '18px', fontWeight: '800', color: isSelected ? '#FFF' : '#0F172A' }}>
                              Bàn {table.tableNumber}
                            </span>
                            <span style={{ fontSize: '11px', fontWeight: '600', color: isSelected ? 'rgba(255,255,255,0.7)' : 'var(--text-secondary)' }}>
                              {table.capacity} chỗ
                            </span>
                            
                            {isOccupied && (
                              <div style={{ 
                                marginTop: '8px', padding: '2px 8px', borderRadius: '10px', 
                                backgroundColor: isSelected ? 'rgba(255,255,255,0.2)' : '#EF4444', 
                                color: '#FFF', fontSize: '10px', fontWeight: '800'
                              }}>
                                {totalAmount.toLocaleString('vi-VN')}đ
                              </div>
                            )}

                            {isOccupied && !isSelected && (
                              <div style={{ position: 'absolute', top: '10px', right: '10px', width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#EF4444', border: '2px solid white' }} />
                            )}
                          </motion.div>
                        );
                      })
                    )}
                  </div>
                </div>

                {/* Right: Bill Detail (Conditional) */}
                <AnimatePresence>
                  {selectedTableForOrders && (
                    <motion.div 
                      initial={{ opacity: 0, x: 20 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 20 }}
                      className="card" 
                      style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px', backgroundColor: '#FFF', boxShadow: '0 10px 30px rgba(0,0,0,0.1)' }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <h4 style={{ margin: 0, fontSize: '20px', fontWeight: '800', color: '#11117F' }}>Bàn {selectedTableForOrders.tableNumber}</h4>
                          <p style={{ margin: '4px 0 0', fontSize: '13px', color: 'var(--text-secondary)' }}>Hóa đơn tạm tính</p>
                        </div>
                        <button onClick={() => setSelectedTableForOrders(null)} style={{ background: '#F1F5F9', border: 'none', borderRadius: '50%', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}><X size={18} /></button>
                      </div>

                      <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        {(() => {
                          const tableOrders = allOrders.filter(o => String(o.tableId) === String(selectedTableForOrders.id) && (o.status === 'PENDING' || o.status === 'CONFIRMED'));
                          if (tableOrders.length === 0) {
                            return (
                              <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
                                <Utensils size={48} style={{ opacity: 0.2, marginBottom: '16px' }} />
                                <p>Bàn này chưa có món nào</p>
                              </div>
                            );
                          }
                          return tableOrders.map(order => (
                            <div key={order.id} style={{ borderBottom: '1px dashed #E2E8F0', paddingBottom: '16px' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                                <span style={{ fontSize: '12px', fontWeight: '800', color: 'var(--primary)', fontFamily: 'monospace' }}>#{String(order.id).slice(0,6)}</span>
                                <span style={{ fontSize: '11px', fontWeight: '700', padding: '2px 8px', borderRadius: '10px', backgroundColor: `${getStatusColor(order.status)}15`, color: getStatusColor(order.status) }}>{order.status}</span>
                              </div>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                {(order.items || []).map((item, idx) => (
                                  <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                                    <span style={{ color: '#475569' }}><strong style={{ color: '#0F172A' }}>{item.quantity}x</strong> {item.foodName}</span>
                                    <span style={{ fontWeight: '600' }}>{(item.unitPrice * item.quantity).toLocaleString('vi-VN')}đ</span>
                                  </div>
                                ))}
                              </div>
                            </div>
                          ));
                        })()}
                      </div>

                      {(() => {
                        const tableOrders = allOrders.filter(o => String(o.tableId) === String(selectedTableForOrders.id) && (o.status === 'PENDING' || o.status === 'CONFIRMED'));
                        const total = tableOrders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
                        if (tableOrders.length === 0) return null;
                        
                        return (
                          <div style={{ borderTop: '2px solid #F1F5F9', paddingTop: '20px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                              <span style={{ fontSize: '16px', fontWeight: '700', color: '#475569' }}>TỔNG CỘNG:</span>
                              <span style={{ fontSize: '24px', fontWeight: '900', color: '#10B981' }}>{total.toLocaleString('vi-VN')}đ</span>
                            </div>
                            <div style={{ display: 'flex', gap: '10px' }}>
                              <button onClick={() => setIsCreateOrderModalOpen(true)} className="btn btn-outline" style={{ flex: 1, padding: '12px', fontSize: '14px' }}>Thêm món</button>
                              <button 
                                onClick={() => handleOpenCheckout(tableOrders[0])} // For now, checkout the first order (usually there's only one active)
                                className="btn btn-primary" style={{ flex: 2, padding: '12px', fontSize: '14px', backgroundColor: '#11117F' }}
                              >
                                💳 Thanh toán
                              </button>
                            </div>
                          </div>
                        );
                      })()}
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
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

          {/* TABLES TAB - Payment History & Invoices */}
          {activeTab === 'Tables' && (
            <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h2 style={{ fontSize: '26px', fontWeight: '800', color: '#11117F', letterSpacing: '-0.5px' }}>Lịch sử hóa đơn</h2>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>Quản lý và tra cứu các giao dịch đã hoàn tất</p>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button onClick={fetchOrdersData} className="btn btn-outline" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 18px' }}>
                    <RefreshCw size={16} /> Làm mới
                  </button>
                  <button className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 18px', boxShadow: '0 4px 12px rgba(9, 52, 219, 0.2)' }}>
                    <Download size={16} /> Xuất báo cáo
                  </button>
                </div>
              </div>

              <div className="card" style={{ padding: 0, overflow: 'hidden', borderRadius: '16px', border: 'none', boxShadow: '0 10px 30px rgba(0,0,0,0.05)' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #EEF2F6' }}>
                      <th style={{ padding: '20px 24px', color: '#64748B', fontWeight: '700', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px' }}>Mã đơn</th>
                      <th style={{ padding: '20px 24px', color: '#64748B', fontWeight: '700', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px' }}>Vị trí bàn</th>
                      <th style={{ padding: '20px 24px', color: '#64748B', fontWeight: '700', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px' }}>Thời gian</th>
                      <th style={{ padding: '20px 24px', color: '#64748B', fontWeight: '700', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px' }}>Tổng thanh toán</th>
                      <th style={{ padding: '20px 24px', color: '#64748B', fontWeight: '700', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px' }}>Trạng thái</th>
                      <th style={{ padding: '20px 24px', color: '#64748B', fontWeight: '700', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px', textAlign: 'center' }}>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allOrders.filter(o => o.status === 'COMPLETED').map(order => (
                      <tr key={order.id} style={{ borderBottom: '1px solid #F1F5F9', transition: 'all 0.2s' }} className="table-row-hover">
                        <td style={{ padding: '20px 24px' }}>
                          <span style={{ fontWeight: '700', color: 'var(--primary)', fontFamily: 'monospace', fontSize: '14px' }}>ORD-{String(order.id).slice(0, 8).toUpperCase()}</span>
                        </td>
                        <td style={{ padding: '20px 24px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <div style={{ width: '32px', height: '32px', borderRadius: '8px', backgroundColor: '#EFF6FF', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#11117F', fontWeight: '800', fontSize: '12px' }}>
                              {order.tableNumber || '?'}
                            </div>
                            <span style={{ fontWeight: '600', color: '#1E293B' }}>Bàn {order.tableNumber || order.tableId}</span>
                          </div>
                        </td>
                        <td style={{ padding: '20px 24px' }}>
                          <div style={{ color: '#475569', fontSize: '13px', fontWeight: '500' }}>{new Date(order.updatedAt || order.createdAt).toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'})}</div>
                          <div style={{ color: '#94A3B8', fontSize: '11px' }}>{new Date(order.updatedAt || order.createdAt).toLocaleDateString('vi-VN')}</div>
                        </td>
                        <td style={{ padding: '20px 24px' }}>
                          <div style={{ fontWeight: '800', color: '#0F172A', fontSize: '16px' }}>
                            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(order.totalAmount || order.totalPrice || 0)}
                          </div>
                        </td>
                        <td style={{ padding: '20px 24px' }}>
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '5px', padding: '6px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '700', backgroundColor: '#DCFCE7', color: '#166534' }}>
                            <CheckCircle size={14} /> Thành công
                          </span>
                        </td>
                        <td style={{ padding: '20px 24px', textAlign: 'center' }}>
                          <button 
                            className="btn-ghost" 
                            style={{ color: '#6366F1', fontWeight: '700', fontSize: '13px', padding: '6px 12px', borderRadius: '8px', cursor: 'pointer', border: 'none', background: 'transparent' }}
                            onClick={() => {
                              setSelectedInvoice(order);
                              setIsInvoiceModalOpen(true);
                            }}
                          >
                            Chi tiết
                          </button>
                        </td>
                      </tr>
                    ))}
                    {allOrders.filter(o => o.status === 'COMPLETED').length === 0 && (
                      <tr>
                        <td colSpan="6" style={{ padding: '100px 24px', textAlign: 'center' }}>
                          <div style={{ opacity: 0.4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
                            <ClipboardList size={64} strokeWidth={1.5} />
                            <div>
                              <p style={{ fontSize: '18px', fontWeight: '700', color: '#1E293B', margin: 0 }}>Chưa có hóa đơn nào</p>
                              <p style={{ fontSize: '14px', color: '#64748B', marginTop: '4px' }}>Dữ liệu thanh toán sẽ xuất hiện tại đây sau khi hoàn tất đơn hàng.</p>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
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
                      {staff.map((person) => {
                        const roleLabel = { 0: 'WAITER', 1: 'ADMIN', 2: 'CHEF', 3: 'KITCHEN' }[person.role] || String(person.role);
                        const roleBg   = { 0: '#EFF6FF', 1: '#FEF3C7', 2: '#D1FAE5', 3: '#FFF7ED' }[person.role] || '#EFF6FF';
                        const roleColor = { 0: '#1D4ED8', 1: '#92400E', 2: '#065F46', 3: '#C2410C' }[person.role] || '#1D4ED8';
                        return (
                          <tr key={person.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                            <td style={{ padding: '16px', color: 'var(--text-secondary)', fontSize: '13px', maxWidth: '80px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{person.id}</td>
                            <td style={{ padding: '16px', fontWeight: '700', color: '#11117F' }}>{person.fullName || 'N/A'}</td>
                            <td style={{ padding: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Calendar size={14} style={{ opacity: 0.6 }} />
                                {formatDate(person.birthday)}
                              </div>
                            </td>
                            <td style={{ padding: '16px' }}>
                              <span style={{ padding: '5px 12px', borderRadius: '8px', fontSize: '12px', backgroundColor: roleBg, border: `1px solid ${roleColor}40`, fontWeight: '700', color: roleColor }}>
                                {roleLabel}
                              </span>
                            </td>
                            <td style={{ padding: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>
                              <div style={{ fontWeight: '500', color: 'var(--text-primary)' }}>{person.phoneNumber || '-'}</div>
                              <div style={{ opacity: 0.8 }}>{person.email || '-'}</div>
                            </td>
                            <td style={{ padding: '16px', color: 'var(--text-secondary)' }}>
                              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px' }}>
                                <button onClick={() => handleOpenStaffModal(person)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--primary)', padding: '4px' }} title="Chỉnh sửa"><Settings size={16} /></button>
                                <button onClick={() => handleDeleteStaff(person.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)', padding: '4px' }} title="Xóa nhân viên"><LogOut size={16} /></button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                      {staff.length === 0 && (
                        <tr><td colSpan="5" style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)' }}>No staff found.</td></tr>
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </motion.div>
          )}

          {/* SETTINGS TAB - System Configuration */}
          {activeTab === 'Settings' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
              
              {/* Table Management Section */}
              <div className="card" style={{ padding: '24px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                  <div>
                    <h3 style={{ fontSize: '20px', fontWeight: '700', margin: 0, color: 'var(--primary)' }}>🪑 Quản lý sơ đồ bàn</h3>
                    <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>Thêm, sửa, xóa các bàn trong nhà hàng</p>
                  </div>
                  <button onClick={() => handleOpenTableModal()} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Plus size={16} /> Thêm Bàn
                  </button>
                </div>

                {loadingConfig.tables ? (
                  <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Đang tải dữ liệu bàn...</p>
                ) : (
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '16px' }}>
                    {tables.map(table => {
                      const tableId = table.id || table.ID;
                      return (
                        <div key={tableId} style={{ padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-app)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <div>
                            <span style={{ display: 'block', fontWeight: '700', fontSize: '16px' }}>Bàn {table.tableNumber}</span>
                            <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{table.capacity} chỗ &middot; {table.location || '—'}</span>
                          </div>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button onClick={() => handleOpenTableModal(table)} className="btn-ghost" style={{ color: 'var(--primary)', padding: '4px' }}><Settings size={16} /></button>
                            <button onClick={() => handleDeleteTable(tableId)} className="btn-ghost" style={{ color: 'var(--status-cancelled)', padding: '4px' }}><Trash2 size={16} /></button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Other Settings Placeholder */}
              <div className="card" style={{ padding: '24px' }}>
                <h3 style={{ fontSize: '20px', fontWeight: '700', marginBottom: '16px' }}>⚙️ Cấu hình hệ thống khác</h3>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                  <div style={{ padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                    <p style={{ fontWeight: '600', marginBottom: '4px' }}>Thông tin nhà hàng</p>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Tên, địa chỉ, số điện thoại, logo...</p>
                  </div>
                  <div style={{ padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                    <p style={{ fontWeight: '600', marginBottom: '4px' }}>Phân quyền nâng cao</p>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Quản lý chi tiết quyền truy cập các nhóm...</p>
                  </div>
                </div>
              </div>

              {/* Modal Thêm/Sửa Bàn (Đặt trong Settings) */}
              {isTableModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="card" style={{ width: '100%', maxWidth: '400px', padding: '32px' }}>
                    <h2 style={{ fontSize: '22px', fontWeight: '700', marginBottom: '24px' }}>{editingTable ? '✏️ Sửa Thông Tin Bàn' : '➕ Thêm Bàn Mới'}</h2>
                    <form onSubmit={handleSaveTable} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                      <div className="input-group">
                        <label>Số bàn *</label>
                        <input type="number" required value={tableFormData.tableNumber} onChange={e => setTableFormData({ ...tableFormData, tableNumber: e.target.value })} className="input-field" placeholder="VD: 10" />
                      </div>
                      <div className="input-group">
                        <label>Sức chứa (người) *</label>
                        <input type="number" required min="1" value={tableFormData.capacity} onChange={e => setTableFormData({ ...tableFormData, capacity: e.target.value })} className="input-field" placeholder="VD: 4" />
                      </div>
                      <div className="input-group">
                        <label>Vị trí (tầng, khu vực)</label>
                        <input type="text" value={tableFormData.location} onChange={e => setTableFormData({ ...tableFormData, location: e.target.value })} className="input-field" placeholder="VD: Tầng 1, cạnh cửa sổ" />
                      </div>
                      <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                        <button type="button" onClick={() => setIsTableModalOpen(false)} className="btn btn-outline" style={{ flex: 1 }}>Hủy</button>
                        <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Lưu Bàn</button>
                      </div>
                    </form>
                  </motion.div>
                </div>
              )}
            </motion.div>
          )}

          {/* Development Placeholders for others */}
          {activeTab === 'Reports' && (
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
              <div className="card" style={{ padding: '24px', marginBottom: '24px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ padding: '10px', borderRadius: '12px', backgroundColor: 'rgba(17, 17, 127, 0.1)', color: '#11117F' }}>
                      <BarChartIcon size={24} />
                    </div>
                    <div>
                      <h3 style={{ fontSize: '22px', fontWeight: '800', color: '#11117F', margin: 0 }}>Hệ thống Báo cáo Thông minh</h3>
                      <p style={{ fontSize: '14px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>Phân tích hiệu quả kinh doanh dựa trên dữ liệu thực tế</p>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px', background: '#F1F5F9', padding: '4px', borderRadius: '12px' }}>
                    {['DAY', 'MONTH', 'YEAR', 'CATEGORY'].map(type => (
                      <button 
                        key={type}
                        onClick={() => fetchReportData(type)} 
                        style={{ 
                          padding: '8px 14px', borderRadius: '8px', border: 'none', cursor: 'pointer', 
                          fontWeight: '700', fontSize: '12px',
                          backgroundColor: reportType === type ? '#FFF' : 'transparent', 
                          color: reportType === type ? '#11117F' : '#64748B', 
                          boxShadow: reportType === type ? '0 4px 6px -1px rgba(0,0,0,0.1)' : 'none' 
                        }}
                      >
                        {type === 'DAY' ? '📅 Ngày' : type === 'MONTH' ? '🗓️ Tháng' : type === 'YEAR' ? '📆 Năm' : '🍔 Món Ăn'}
                      </button>
                    ))}
                  </div>
                </div>

                <div style={{ height: '450px', width: '100%', marginTop: '20px' }}>
                  {loadingConfig.reports ? (
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                      <RefreshCw className="animate-spin" size={32} color="var(--primary)" />
                    </div>
                  ) : reportData.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '100px 0' }}>
                      <TrendingUp size={48} style={{ opacity: 0.2, marginBottom: '16px' }} />
                      <p style={{ color: 'var(--text-secondary)' }}>Chưa có đủ dữ liệu để tạo biểu đồ.</p>
                    </div>
                  ) : (
                    <ResponsiveContainer width="100%" height="100%">
                      {reportType === 'DATE' ? (
                        <BarChart data={reportData}>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                          <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748B' }} dy={10} />
                          <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748B' }} />
                          <Tooltip 
                            contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}
                            formatter={(value) => [new Intl.NumberFormat('vi-VN').format(value) + 'đ', 'Doanh thu']}
                          />
                          <Bar dataKey="value" fill="#11117F" radius={[6, 6, 0, 0]} barSize={40} />
                        </BarChart>
                      ) : (
                        <PieChart>
                          <Pie
                            data={reportData}
                            cx="50%"
                            cy="50%"
                            innerRadius={80}
                            outerRadius={140}
                            paddingAngle={5}
                            dataKey="value"
                          >
                            {reportData.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={['#11117F', '#3B82F6', '#10B981', '#F59E0B', '#EF4444'][index % 5]} />
                            ))}
                          </Pie>
                          <Tooltip formatter={(value) => [new Intl.NumberFormat('vi-VN').format(value) + 'đ', 'Doanh thu']} />
                          <Legend verticalAlign="bottom" height={36}/>
                        </PieChart>
                      )}
                    </ResponsiveContainer>
                  )}
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px' }}>
                <div className="card" style={{ padding: '20px', borderLeft: '4px solid #11117F' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: '600' }}>TỔNG DOANH THU</p>
                  <h4 style={{ fontSize: '24px', fontWeight: '800', margin: '8px 0 0' }}>
                    {new Intl.NumberFormat('vi-VN').format(reportData.reduce((acc, curr) => acc + curr.value, 0))}đ
                  </h4>
                </div>
                <div className="card" style={{ padding: '20px', borderLeft: '4px solid #10B981' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: '600' }}>SỐ LƯỢNG MỤC</p>
                  <h4 style={{ fontSize: '24px', fontWeight: '800', margin: '8px 0 0' }}>{reportData.length}</h4>
                </div>
                <div className="card" style={{ padding: '20px', borderLeft: '4px solid #F59E0B' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: '600' }}>LOẠI BÁO CÁO</p>
                  <h4 style={{ fontSize: '24px', fontWeight: '800', margin: '8px 0 0' }}>{reportType}</h4>
                </div>
              </div>
            </motion.div>
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
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Số CCCD (Citizen ID) *</label>
                  <input type="text" required value={staffFormData.citizenPid} onChange={e => setStaffFormData({ ...staffFormData, citizenPid: e.target.value })} className="form-input" placeholder="Nhập 12 số CCCD" style={{ borderColor: '#E0E0E0' }} />
                </div>

                <div className="input-group">
                  <label className="form-label" style={{ color: '#11117F', fontWeight: '700' }}>Phân quyền (Role)</label>
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

      {/* CREATE ORDER MODAL (POS) */}
      {isCreateOrderModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} style={{ background: '#F8FAFC', borderRadius: '16px', width: '100%', maxWidth: '1100px', height: '90vh', overflow: 'hidden', display: 'flex', flexDirection: 'column', boxShadow: '0 24px 48px rgba(0,0,0,0.3)' }}>
            
            {/* Header */}
            <div style={{ padding: '20px 24px', backgroundColor: '#FFF', borderBottom: '1px solid #E2E8F0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: 0, color: '#0F172A', fontSize: '20px', fontWeight: '800' }}>TẠO ĐƠN HÀNG MỚI (POS)</h3>
              <button onClick={() => setIsCreateOrderModalOpen(false)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#64748B' }}><X size={24} /></button>
            </div>

            {/* Split Content */}
            <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
              
              {/* LEFT COLUMN: Menus & Tables */}
              <div style={{ flex: 6, display: 'flex', flexDirection: 'column', borderRight: '1px solid #E2E8F0', backgroundColor: '#F8FAFC', overflow: 'hidden' }}>
                <div style={{ padding: '20px', backgroundColor: '#FFF', borderBottom: '1px solid #E2E8F0' }}>
                  <label style={{ fontSize: '13px', fontWeight: '700', color: '#475569', display: 'block', marginBottom: '8px' }}>CHỌN BÀN (Chỉ hiển thị bàn trống)</label>
                  <select 
                    value={selectedTableId} 
                    onChange={(e) => setSelectedTableId(e.target.value)}
                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', outline: 'none', fontSize: '15px', fontWeight: '600', color: '#0F172A' }}
                  >
                    <option value="">-- Bấm để chọn bàn --</option>
                    {tables.filter(t => t.status === 'AVAILABLE' || t.status === '0').map(t => (
                      <option key={t.id} value={t.id}>Bàn {t.tableNumber || t.id} (Sức chứa: {t.capacity})</option>
                    ))}
                  </select>
                </div>
                
                <div style={{ flex: 1, padding: '20px', overflowY: 'auto' }}>
                  <h4 style={{ margin: '0 0 16px', fontSize: '15px', color: '#475569' }}>DANH SÁCH MÓN ĂN</h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '16px' }}>
                    {foods.map(food => (
                      <div key={food.id} style={{ backgroundColor: '#FFF', borderRadius: '12px', overflow: 'hidden', border: '1px solid #E2E8F0', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s', cursor: 'pointer' }} onClick={() => handleAddToCart(food)}>
                        <div style={{ height: '120px', backgroundColor: '#E2E8F0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          {food.imageUrl ? (
                            <img src={food.imageUrl} alt={food.foodName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                          ) : (
                            <Utensils size={40} color="#94A3B8" />
                          )}
                        </div>
                        <div style={{ padding: '12px', display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
                          <span style={{ fontSize: '14px', fontWeight: '700', color: '#0F172A', lineHeight: 1.3 }}>{food.foodName}</span>
                          <span style={{ fontSize: '14px', fontWeight: '600', color: '#10B981' }}>{new Intl.NumberFormat('vi-VN').format(food.price)}đ</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* RIGHT COLUMN: Cart */}
              <div style={{ flex: 4, display: 'flex', flexDirection: 'column', backgroundColor: '#FFF' }}>
                <div style={{ padding: '20px', borderBottom: '1px solid #E2E8F0', backgroundColor: '#F1F5F9' }}>
                  <h4 style={{ margin: 0, fontSize: '15px', color: '#0F172A', display: 'flex', justifyContent: 'space-between' }}>
                    <span>GIỎ HÀNG</span>
                    <span style={{ backgroundColor: '#10B981', color: '#FFF', padding: '2px 8px', borderRadius: '12px', fontSize: '12px' }}>{cartItems.reduce((acc, i) => acc + i.quantity, 0)} món</span>
                  </h4>
                </div>

                <div style={{ flex: 1, padding: '20px', overflowY: 'auto' }}>
                  {cartItems.length === 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', opacity: 0.5, gap: '12px' }}>
                      <ClipboardList size={48} />
                      <p>Chưa có món nào được chọn</p>
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                      {cartItems.map(item => (
                        <div key={item.menuItemId} style={{ display: 'flex', flexDirection: 'column', gap: '8px', paddingBottom: '16px', borderBottom: '1px dashed #E2E8F0' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div style={{ flex: 1 }}>
                              <p style={{ margin: 0, fontSize: '14px', fontWeight: '700', color: '#0F172A' }}>{item.foodName}</p>
                              <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#64748B' }}>{new Intl.NumberFormat('vi-VN').format(item.unitPrice)}đ / món</p>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', border: '1px solid #CBD5E1', borderRadius: '6px', overflow: 'hidden' }}>
                                <button onClick={() => handleUpdateCartItem(item.menuItemId, -1)} style={{ padding: '4px 8px', border: 'none', background: '#F1F5F9', cursor: 'pointer' }}>-</button>
                                <span style={{ padding: '0 12px', fontSize: '14px', fontWeight: '600' }}>{item.quantity}</span>
                                <button onClick={() => handleUpdateCartItem(item.menuItemId, 1)} style={{ padding: '4px 8px', border: 'none', background: '#F1F5F9', cursor: 'pointer' }}>+</button>
                              </div>
                              <button onClick={() => handleRemoveFromCart(item.menuItemId)} style={{ background: 'transparent', border: 'none', color: '#EF4444', cursor: 'pointer', padding: 0 }}><X size={18} /></button>
                            </div>
                          </div>
                          {/* Note input for each item */}
                          <input 
                            type="text" 
                            placeholder="Ghi chú món (ví dụ: ít cay...)" 
                            value={item.note || ''}
                            onChange={(e) => handleUpdateCartItem(item.menuItemId, 0, e.target.value)}
                            style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #E2E8F0', fontSize: '12px', outline: 'none' }}
                          />
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div style={{ padding: '20px', borderTop: '1px solid #E2E8F0', backgroundColor: '#F8FAFC' }}>
                  <textarea 
                    placeholder="Ghi chú chung cho đơn hàng..." 
                    value={orderNote}
                    onChange={(e) => setOrderNote(e.target.value)}
                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '13px', marginBottom: '16px', resize: 'none', outline: 'none' }}
                    rows={2}
                  />
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                    <span style={{ fontSize: '16px', fontWeight: '700', color: '#475569' }}>TỔNG CỘNG:</span>
                    <span style={{ fontSize: '24px', fontWeight: '800', color: '#10B981' }}>
                      {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(cartItems.reduce((acc, i) => acc + (i.unitPrice * i.quantity), 0))}
                    </span>
                  </div>
                  <button 
                    onClick={handleSubmitOrder}
                    disabled={cartItems.length === 0 || !selectedTableId}
                    style={{ width: '100%', padding: '16px', borderRadius: '12px', border: 'none', backgroundColor: (cartItems.length === 0 || !selectedTableId) ? '#94A3B8' : '#10B981', color: '#FFF', fontSize: '16px', fontWeight: '800', cursor: (cartItems.length === 0 || !selectedTableId) ? 'not-allowed' : 'pointer', transition: 'background 0.2s' }}
                  >
                    XÁC NHẬN GỌI MÓN
                  </button>
                </div>

              </div>
            </div>
            
          </motion.div>
        </div>
      )}

      {/* INVOICE MODAL */}
      {isInvoiceModalOpen && selectedInvoice && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ background: '#FFF', borderRadius: '16px', width: '100%', maxWidth: '450px', maxHeight: '90vh', overflow: 'hidden', display: 'flex', flexDirection: 'column', boxShadow: '0 24px 48px rgba(0,0,0,0.2)' }}>
            
            {/* Header */}
            <div style={{ padding: '24px', backgroundColor: '#F8FAFC', borderBottom: '1px dashed #CBD5E1', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <h3 style={{ margin: 0, color: '#0F172A', fontSize: '20px', fontWeight: '800' }}>HÓA ĐƠN CHI TIẾT</h3>
                <p style={{ margin: '4px 0 0', color: 'var(--primary)', fontSize: '14px', fontWeight: '800', fontFamily: 'monospace' }}>ORD-{String(selectedInvoice.id).slice(0,8).toUpperCase()}</p>
              </div>
              <button onClick={() => setIsInvoiceModalOpen(false)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#94A3B8' }}><X size={24} /></button>
            </div>

            {/* Content (Scrollable) */}
            <div style={{ padding: '24px', flex: 1, overflowY: 'auto' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
                <div>
                  <p style={{ margin: 0, fontSize: '12px', color: '#64748B', textTransform: 'uppercase', fontWeight: '700' }}>Vị trí bàn</p>
                  <p style={{ margin: '4px 0 0', fontSize: '16px', color: '#0F172A', fontWeight: '700' }}>{selectedInvoice.tableNumber || selectedInvoice.tableId}</p>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <p style={{ margin: 0, fontSize: '12px', color: '#64748B', textTransform: 'uppercase', fontWeight: '700' }}>Thời gian</p>
                  <p style={{ margin: '4px 0 0', fontSize: '14px', color: '#0F172A', fontWeight: '600' }}>
                    {new Date(selectedInvoice.updatedAt || selectedInvoice.createdAt).toLocaleString('vi-VN')}
                  </p>
                </div>
              </div>

              <div style={{ borderTop: '1px solid #EEF2F6', borderBottom: '1px solid #EEF2F6', padding: '16px 0', marginBottom: '24px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                  <thead>
                    <tr style={{ color: '#64748B' }}>
                      <th style={{ textAlign: 'left', paddingBottom: '12px', fontWeight: '600' }}>Món ăn</th>
                      <th style={{ textAlign: 'center', paddingBottom: '12px', fontWeight: '600' }}>SL</th>
                      <th style={{ textAlign: 'right', paddingBottom: '12px', fontWeight: '600' }}>Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(selectedInvoice.items || []).map((item, idx) => (
                      <tr key={idx}>
                        <td style={{ padding: '8px 0', color: '#1E293B', fontWeight: '500' }}>{item.foodName}</td>
                        <td style={{ padding: '8px 0', textAlign: 'center', color: '#475569' }}>{item.quantity}</td>
                        <td style={{ padding: '8px 0', textAlign: 'right', color: '#1E293B', fontWeight: '600' }}>
                          {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.unitPrice * item.quantity)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748B', fontSize: '14px' }}>
                  <span>Tạm tính</span>
                  <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedInvoice.totalPrice || selectedInvoice.totalAmount || 0)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748B', fontSize: '14px' }}>
                  <span>Phí dịch vụ</span>
                  <span>đ 0</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '8px', paddingTop: '16px', borderTop: '1px dashed #CBD5E1', fontSize: '18px', fontWeight: '800', color: '#0F172A' }}>
                  <span>TỔNG CỘNG</span>
                  <span style={{ color: 'var(--primary)' }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedInvoice.totalPrice || selectedInvoice.totalAmount || 0)}</span>
                </div>
              </div>
            </div>

            {/* Footer */}
            <div style={{ padding: '24px', backgroundColor: '#F8FAFC', display: 'flex', gap: '12px' }}>
              <button className="btn btn-outline" style={{ flex: 1, padding: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                <Download size={18} /> Tải PDF
              </button>
              <button onClick={() => setIsInvoiceModalOpen(false)} className="btn btn-primary" style={{ flex: 1, padding: '12px' }}>
                Đóng Hóa Đơn
              </button>
            </div>
            
          </motion.div>
        </div>
      )}

      {/* CHECKOUT MODAL (PAYMENT) */}
      {isCheckoutModalOpen && checkoutOrder && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ background: '#FFF', borderRadius: '16px', width: '100%', maxWidth: '600px', overflow: 'hidden', display: 'flex', flexDirection: 'column', boxShadow: '0 24px 48px rgba(0,0,0,0.3)' }}>
            
            {/* Header */}
            <div style={{ padding: '20px 24px', backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: 0, color: '#0F172A', fontSize: '20px', fontWeight: '800' }}>THANH TOÁN ĐƠN HÀNG</h3>
              <button onClick={() => setIsCheckoutModalOpen(false)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#64748B' }}><X size={24} /></button>
            </div>

            {/* Content */}
            <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
              
              {/* Order Summary Summary */}
              <div style={{ backgroundColor: '#F1F5F9', padding: '16px', borderRadius: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <p style={{ margin: 0, fontSize: '13px', color: '#64748B', fontWeight: '600' }}>Đơn hàng: #{String(checkoutOrder.id).slice(0,8).toUpperCase()}</p>
                  <p style={{ margin: '4px 0 0', fontSize: '16px', color: '#0F172A', fontWeight: '700' }}>{checkoutOrder.tableNumber || checkoutOrder.tableId}</p>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <p style={{ margin: 0, fontSize: '13px', color: '#64748B', fontWeight: '600' }}>TỔNG THANH TOÁN</p>
                  <p style={{ margin: '4px 0 0', fontSize: '24px', color: '#10B981', fontWeight: '800' }}>
                    {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(checkoutOrder.totalAmount || checkoutOrder.totalPrice || 0)}
                  </p>
                </div>
              </div>

              {/* Payment Methods */}
              <div>
                <label style={{ fontSize: '14px', fontWeight: '700', color: '#0F172A', marginBottom: '12px', display: 'block' }}>Phương thức thanh toán</label>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button onClick={() => setPaymentMethod('CASH')} style={{ flex: 1, padding: '12px', borderRadius: '8px', border: paymentMethod === 'CASH' ? '2px solid #3B82F6' : '1px solid #CBD5E1', backgroundColor: paymentMethod === 'CASH' ? '#EFF6FF' : '#FFF', color: paymentMethod === 'CASH' ? '#1D4ED8' : '#475569', fontWeight: '700', cursor: 'pointer', transition: 'all 0.2s' }}>
                    💵 Tiền mặt
                  </button>
                  <button onClick={() => { setPaymentMethod('TRANSFER'); setIsQRModalOpen(true); }} style={{ flex: 1, padding: '12px', borderRadius: '8px', border: paymentMethod === 'TRANSFER' ? '2px solid #3B82F6' : '1px solid #CBD5E1', backgroundColor: paymentMethod === 'TRANSFER' ? '#EFF6FF' : '#FFF', color: paymentMethod === 'TRANSFER' ? '#1D4ED8' : '#475569', fontWeight: '700', cursor: 'pointer', transition: 'all 0.2s' }}>
                    🏦 Chuyển khoản (QR)
                  </button>
                </div>
              </div>

              {/* Dynamic Payment Body */}
              <div style={{ minHeight: '180px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                {paymentMethod === 'CASH' ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#475569', marginBottom: '6px', display: 'block' }}>Tiền khách đưa (VNĐ)</label>
                      <input 
                        type="text" 
                        placeholder="Nhập số tiền..."
                        value={customerCash}
                        onChange={(e) => {
                          const val = e.target.value.replace(/\D/g, '');
                          setCustomerCash(val ? new Intl.NumberFormat('vi-VN').format(val) : '');
                        }}
                        style={{ width: '100%', padding: '16px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '20px', fontWeight: '700', outline: 'none' }}
                      />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', backgroundColor: '#F8FAFC', border: '1px dashed #94A3B8', borderRadius: '8px' }}>
                      <span style={{ fontSize: '15px', fontWeight: '600', color: '#475569' }}>Tiền thừa trả khách:</span>
                      <span style={{ fontSize: '20px', fontWeight: '800', color: '#EF4444' }}>
                        {(() => {
                          const total = Number(checkoutOrder.totalAmount || checkoutOrder.totalPrice || 0);
                          const cash = Number(customerCash.replace(/\D/g, '') || 0);
                          if (cash < total || !cash) return '0 đ';
                          return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(cash - total);
                        })()}
                      </span>
                    </div>
                  </div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', backgroundColor: '#F0F9FF', padding: '32px', borderRadius: '12px', border: '1px solid #BAE6FD' }}>
                    <div style={{ padding: '16px', backgroundColor: '#3B82F6', borderRadius: '50%', color: '#FFF', marginBottom: '16px' }}>
                      <QrCode size={32} />
                    </div>
                    <p style={{ margin: 0, fontSize: '16px', color: '#0369A1', fontWeight: '700' }}>Phương thức: Chuyển khoản</p>
                    <button 
                      onClick={() => setIsQRModalOpen(true)}
                      style={{ marginTop: '12px', padding: '8px 16px', background: '#3B82F6', color: '#FFF', border: 'none', borderRadius: '6px', fontWeight: '600', cursor: 'pointer' }}
                    >
                      Xem lại mã QR
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Footer */}
            <div style={{ padding: '20px 24px', backgroundColor: '#F8FAFC', borderTop: '1px solid #E2E8F0', display: 'flex', gap: '12px' }}>
              <button onClick={() => setIsCheckoutModalOpen(false)} className="btn btn-outline" style={{ flex: 1, padding: '14px' }}>
                Hủy
              </button>
              <button 
                onClick={handleConfirmPayment} 
                className="btn btn-primary" 
                style={{ flex: 2, padding: '14px', backgroundColor: '#3B82F6', borderColor: '#3B82F6', fontSize: '16px' }}
              >
                XÁC NHẬN ĐÃ THU TIỀN
              </button>
            </div>
            
          </motion.div>
        </div>
      )}

      {/* QR CODE POPUP MODAL */}
      {isQRModalOpen && checkoutOrder && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', zIndex: 1100, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} style={{ background: '#FFF', borderRadius: '24px', width: '100%', maxWidth: '420px', overflow: 'hidden', display: 'flex', flexDirection: 'column', boxShadow: '0 25px 50px -12px rgba(0,0,0,0.5)' }}>
            
            {/* Header with Bank Color */}
            <div style={{ padding: '20px', background: '#E01020', color: '#FFF', textAlign: 'center', position: 'relative' }}>
              <h3 style={{ margin: 0, fontSize: '18px', fontWeight: '800' }}>TECHCOMBANK</h3>
              <button onClick={() => setIsQRModalOpen(false)} style={{ position: 'absolute', right: '16px', top: '50%', transform: 'translateY(-50%)', background: 'rgba(255,255,255,0.2)', border: 'none', borderRadius: '50%', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: '#FFF' }}>
                <X size={20} />
              </button>
            </div>

            <div style={{ padding: '32px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '20px' }}>
              <div style={{ textAlign: 'center' }}>
                <p style={{ margin: 0, fontSize: '14px', color: '#64748B', fontWeight: '600', textTransform: 'uppercase' }}>Chủ tài khoản</p>
                <p style={{ margin: '4px 0 0', fontSize: '20px', color: '#0F172A', fontWeight: '800' }}>NGUYEN LAN VIET</p>
                <p style={{ margin: '8px 0 0', fontSize: '18px', color: '#11117F', fontWeight: '700', letterSpacing: '1px' }}>1903 7974 1810 12</p>
              </div>

              {/* QR Image */}
              <div style={{ padding: '16px', backgroundColor: '#FFF', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.08)', border: '1px solid #F1F5F9' }}>
                <img 
                  src={`https://img.vietqr.io/image/970407-19037974181012-print.jpg?amount=${checkoutOrder.totalAmount || checkoutOrder.totalPrice || 0}&addInfo=Thanh toan don ${String(checkoutOrder.id).slice(0,8)}&accountName=NGUYEN LAN VIET`} 
                  alt="QR Code" 
                  style={{ width: '280px', height: '280px', objectFit: 'contain' }}
                />
              </div>

              <div style={{ textAlign: 'center', backgroundColor: '#F8FAFC', padding: '16px', borderRadius: '12px', width: '100%' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B', fontWeight: '600' }}>SỐ TIỀN CẦN CHUYỂN</p>
                <p style={{ margin: '4px 0 0', fontSize: '26px', color: '#10B981', fontWeight: '900' }}>
                  {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(checkoutOrder.totalAmount || checkoutOrder.totalPrice || 0)}
                </p>
              </div>

              <button 
                onClick={() => setIsQRModalOpen(false)}
                style={{ width: '100%', padding: '16px', borderRadius: '12px', background: '#0F172A', color: '#FFF', border: 'none', fontSize: '16px', fontWeight: '700', cursor: 'pointer', transition: 'opacity 0.2s' }}
                onMouseEnter={(e) => e.target.style.opacity = '0.9'}
                onMouseLeave={(e) => e.target.style.opacity = '1'}
              >
                ĐÃ QUÉT XONG
              </button>
            </div>
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
        setCurrentUser({ 
          id: payload.sub || payload.uid, 
          role: payload.role || 'GUEST', 
          server: 'local',
          fullName: payload.fullName || payload.sub
        });
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
