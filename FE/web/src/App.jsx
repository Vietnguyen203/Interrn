import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { LayoutDashboard, Users, Utensils, ClipboardList, PieChart, LogOut, Settings, Search, Bell, Menu, User as UserIcon, Filter, Download, Plus, AlertCircle, Calendar } from 'lucide-react';
import { apiService } from './services/api';
import './index.css';

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
      const response = await apiService.auth.login(empId, password, server);
      const token = response.data.token;
      sessionStorage.setItem('token', token);
      const userPayload = parseJwt(token);
      onLoginSuccess({
        id: userPayload?.uid || empId,
        role: userPayload?.role || 'USER',
        server: userPayload?.server || server,
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
  const [activeTab, setActiveTab] = useState(user?.role === 'KITCHEN' ? 'Kitchen' : 'Overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  // Data States
  const [tables, setTables] = useState([]);
  const [recentOrders, setRecentOrders] = useState([]);
  const [allOrders, setAllOrders] = useState([]);
  const [foods, setFoods] = useState([]);
  const [staff, setStaff] = useState([]);

  // Modal States
  const [isFoodModalOpen, setIsFoodModalOpen] = useState(false);
  const [editingFood, setEditingFood] = useState(null);
  const [foodFormData, setFoodFormData] = useState({
    foodName: '', price: '', unit: '', category: 'MAIN_COURSE', image: '', description: ''
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
      const res = await apiService.kitchen.getPendingItems();
      if (res.data) setKitchenItems(res.data);
    } catch (error) { console.error('Error fetching kitchen data:', error); }
    finally { setLoadingConfig(prev => ({ ...prev, kitchen: false })); }
  };

  const handleUpdateItemStatus = async (itemId, newStatus) => {
    try {
      await apiService.kitchen.updateItemStatus(itemId, newStatus);
      fetchKitchenData();
    } catch (error) { alert("Error updating status: " + error.message); }
  };

  const fetchOverviewData = async () => {
    setLoadingConfig(prev => ({ ...prev, overview: true }));
    try {
      const [tablesRes, ordersRes] = await Promise.all([
        apiService.dashboard.getTables(),
        apiService.dashboard.getRecentOrders()
      ]);
      if (tablesRes.data) setTables(tablesRes.data);
      if (ordersRes.data) setRecentOrders(ordersRes.data);
    } catch (error) { console.error('Error fetching overview:', error); }
    finally { setLoadingConfig(prev => ({ ...prev, overview: false })); }
  };

  const fetchOrdersData = async () => {
    setLoadingConfig(prev => ({ ...prev, orders: true }));
    try {
      const res = await apiService.dashboard.getAllOrders(0, 50);
      if (res.data) setAllOrders(res.data);
    } catch (error) { console.error('Error fetching orders:', error); }
    finally { setLoadingConfig(prev => ({ ...prev, orders: false })); }
  };

  const fetchFoodsData = async () => {
    setLoadingConfig(prev => ({ ...prev, foods: true }));
    try {
      const res = await apiService.dashboard.getFoods();
      if (res.data) setFoods(res.data);
    } catch (error) { console.error('Error fetching foods:', error); }
    finally { setLoadingConfig(prev => ({ ...prev, foods: false })); }
  };

  const fetchStaffData = async () => {
    setLoadingConfig(prev => ({ ...prev, staff: true }));
    try {
      const server = user?.server || 'HCM';
      console.log(`[DEBUG] Fetching staff for server: ${server}`);
      const res = await apiService.dashboard.getStaff(server);
      console.log('[DEBUG] Staff response:', res);
      if (res.data) {
        setStaff(res.data);
        console.log(`[DEBUG] Staff state updated with ${res.data.length} items.`);
      }
    } catch (error) {
      console.error('Error fetching staff:', error);
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
      case 'ORDERING': return 'var(--status-ordering)';
      case 'CANCELLED': return 'var(--status-cancelled)';
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
      setFoodFormData(food);
    } else {
      setEditingFood(null);
      setFoodFormData({ foodName: '', price: '', unit: '', category: 'MAIN_COURSE', image: '', description: '' });
    }
    setIsFoodModalOpen(true);
  };

  const handleSaveFood = async (e) => {
    e.preventDefault();
    try {
      if (editingFood) {
        await apiService.dashboard.updateFood(editingFood.id, foodFormData);
      } else {
        await apiService.dashboard.createFood(foodFormData);
      }
      setIsFoodModalOpen(false);
      fetchFoodsData(); // Reload
    } catch (err) {
      alert("Error saving food: " + err.message);
    }
  };

  const handleDeleteFood = async (id) => {
    if (!window.confirm("Are you sure you want to delete this food item?")) return;
    try {
      await apiService.dashboard.deleteFood(id);
      fetchFoodsData();
    } catch (err) {
      alert("Error deleting food: " + err.message);
    }
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
        delete payload.password; // Don't send empty password on update
      }
      
      if (editingStaff) {
        await apiService.dashboard.updateStaff(editingStaff.server, editingStaff.uid, payload);
      } else {
        await apiService.dashboard.createStaff(payload);
      }
      setIsStaffModalOpen(false);
      fetchStaffData();
    } catch (err) {
      alert("Error saving staff: " + err.message);
    }
  };

  const handleDeleteStaff = async (server, employeeId) => {
    if (!window.confirm("Are you sure you want to remove this staff member?")) return;
    try {
      await apiService.dashboard.deleteStaff(server, employeeId);
      fetchStaffData();
    } catch (err) {
      alert("Error deleting staff: " + err.message);
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', width: '100%', overflow: 'hidden', backgroundColor: 'var(--bg-app)' }}>
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h3 style={{ fontSize: '20px', fontWeight: '600' }}>In-Progress Orders</h3>
                <button onClick={fetchKitchenData} className="btn-ghost" style={{ padding: '8px 16px', fontSize: '14px' }}>Refresh</button>
              </div>

              {loadingConfig.kitchen ? (
                <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Loading kitchen items...</p>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '24px' }}>
                  {kitchenItems.map(item => (
                    <div key={item.id} className="card" style={{ padding: '20px', borderLeft: `4px solid ${item.status === 'PENDING' ? 'var(--status-cancelled)' : 'var(--status-ordering)'}` }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                        <h4 style={{ fontSize: '18px', fontWeight: '700' }}>{item.foodName}</h4>
                        <span style={{ fontSize: '12px', fontWeight: 'bold', color: item.status === 'PENDING' ? 'var(--status-cancelled)' : 'var(--status-ordering)' }}>{item.status}</span>
                      </div>
                      <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '8px' }}>Quantity: <strong>{item.quantity}</strong></p>
                      {item.note && <p style={{ fontSize: '13px', fontStyle: 'italic', backgroundColor: '#f9f9f9', padding: '8px', borderRadius: '4px', marginBottom: '16px' }}>Note: {item.note}</p>}
                      
                      <div style={{ display: 'flex', gap: '12px' }}>
                        {item.status === 'PENDING' && (
                          <button onClick={() => handleUpdateItemStatus(item.orderItemId, 'PREPARING')} className="btn btn-primary" style={{ flex: 1, padding: '8px' }}>Accept Order</button>
                        )}
                        {item.status === 'PREPARING' && (
                          <button onClick={() => handleUpdateItemStatus(item.orderItemId, 'READY')} className="btn" style={{ flex: 1, padding: '8px', backgroundColor: 'var(--status-completed)', color: 'white', border: 'none' }}>Mark Completed</button>
                        )}
                      </div>
                    </div>
                  ))}
                  {kitchenItems.length === 0 && (
                    <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '60px', color: 'var(--text-secondary)' }}>
                      <Utensils size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                      <p>Currently no pending orders in the kitchen.</p>
                    </div>
                  )}
                </div>
              )}
            </motion.div>
          )}

          {/* ORDERS TAB */}
          {activeTab === 'Orders' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card" style={{ padding: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h3 style={{ fontSize: '20px', fontWeight: '600' }}>All Orders</h3>
                <button onClick={fetchOrdersData} className="btn btn-outline" style={{ padding: '8px 16px', fontSize: '14px' }}>Refresh</button>
              </div>
              {loadingConfig.orders ? (
                <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Loading orders...</p>
              ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-app)' }}>
                      <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Order ID</th>
                      <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Table</th>
                      <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Total Amount</th>
                      <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Created By</th>
                      <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: '500' }}>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allOrders.map((order) => (
                      <tr key={order.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ padding: '16px', fontWeight: '500' }}>{order.id}</td>
                        <td style={{ padding: '16px', color: 'var(--text-secondary)' }}>{order.tableId}</td>
                        <td style={{ padding: '16px', fontWeight: '600' }}>{order.totalAmount}</td>
                        <td style={{ padding: '16px', color: 'var(--text-secondary)' }}>{order.createdBy}</td>
                        <td style={{ padding: '16px' }}>
                          <span style={{ padding: '6px 12px', borderRadius: '4px', fontSize: '13px', fontWeight: '600', backgroundColor: `${getStatusColor(order.status)}15`, color: getStatusColor(order.status) }}>
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                    {allOrders.length === 0 && (
                      <tr><td colSpan="5" style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)' }}>No orders found.</td></tr>
                    )}
                  </tbody>
                </table>
              )}
            </motion.div>
          )}

          {/* MENU & FOOD TAB */}
          {activeTab === 'Menu & Food' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h3 style={{ fontSize: '20px', fontWeight: '600' }}>Menu Management</h3>
                <button onClick={() => handleOpenFoodModal()} className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '14px' }}>
                  <Plus size={16} /> Add Food
                </button>
              </div>

              {loadingConfig.foods ? (
                <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Loading menu items...</p>
              ) : foods.length === 0 ? (
                <div className="card" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>No food items available. Connect database or add items.</div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '24px' }}>
                  {foods.map(food => (
                    <div key={food.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                      <div style={{ height: '160px', backgroundColor: 'var(--bg-app)', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
                        {food.image ? (
                          <img src={food.image} alt={food.foodName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        ) : (
                          <Utensils size={40} color="var(--border-color)" />
                        )}
                      </div>
                      <div style={{ padding: '16px', flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                          <h4 style={{ fontSize: '16px', fontWeight: '600', color: 'var(--text-primary)', margin: 0 }}>{food.foodName}</h4>
                          <span style={{ fontSize: '12px', padding: '2px 8px', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', borderRadius: '12px', fontWeight: '600' }}>{food.category}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
                          <p style={{ fontSize: '18px', fontWeight: '700', color: 'var(--status-ordering)', margin: 0 }}>
                            {food.price} <span style={{ fontSize: '14px', color: 'var(--text-secondary)', fontWeight: '400' }}>{food.unit}</span>
                          </p>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button onClick={() => handleOpenFoodModal(food)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}><Settings size={16} /></button>
                            <button onClick={() => handleDeleteFood(food.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--status-cancelled)' }}><LogOut size={16} /></button>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Food Modal overlay */}
              {isFoodModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="card" style={{ width: '100%', maxWidth: '500px', padding: '32px', maxHeight: '90vh', overflowY: 'auto' }}>
                    <h2 style={{ fontSize: '24px', fontWeight: '600', marginBottom: '24px' }}>{editingFood ? 'Edit Food' : 'Add Food Item'}</h2>

                    <form onSubmit={handleSaveFood} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                      <div className="input-group">
                        <label>Food Name</label>
                        <input type="text" required value={foodFormData.foodName} onChange={e => setFoodFormData({ ...foodFormData, foodName: e.target.value })} className="input-field" placeholder="E.g. Spicy Chicken Burger" />
                      </div>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                        <div className="input-group">
                          <label>Price</label>
                          <input type="number" required value={foodFormData.price} onChange={e => setFoodFormData({ ...foodFormData, price: e.target.value })} className="input-field" placeholder="100000" />
                        </div>
                        <div className="input-group">
                          <label>Unit</label>
                          <input type="text" required value={foodFormData.unit} onChange={e => setFoodFormData({ ...foodFormData, unit: e.target.value })} className="input-field" placeholder="VND" />
                        </div>
                      </div>
                      <div className="input-group">
                        <label>Category</label>
                        <select value={foodFormData.category} onChange={e => setFoodFormData({ ...foodFormData, category: e.target.value })} className="input-field">
                          <option value="APPETIZER">Appetizer</option>
                          <option value="MAIN_COURSE">Main Course</option>
                          <option value="DESSERT">Dessert</option>
                          <option value="DRINK">Drink</option>
                          <option value="OTHER">Other</option>
                        </select>
                      </div>
                      <div className="input-group">
                        <label>Image URL (Optional)</label>
                        <input type="url" value={foodFormData.image || ''} onChange={e => setFoodFormData({ ...foodFormData, image: e.target.value })} className="input-field" placeholder="https://example.com/image.jpg" />
                      </div>
                      <div className="input-group">
                        <label>Description (Optional)</label>
                        <textarea value={foodFormData.description || ''} onChange={e => setFoodFormData({ ...foodFormData, description: e.target.value })} className="input-field" placeholder="A short description..." style={{ minHeight: '80px', resize: 'vertical' }}></textarea>
                      </div>

                      <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                        <button type="button" onClick={() => setIsFoodModalOpen(false)} className="btn btn-outline" style={{ flex: 1 }}>Cancel</button>
                        <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Save Food</button>
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
